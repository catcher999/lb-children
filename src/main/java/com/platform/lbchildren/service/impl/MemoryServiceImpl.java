package com.platform.lbchildren.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.entity.AIChatHistory;
import com.platform.lbchildren.entity.Album;
import com.platform.lbchildren.entity.Diary;
import com.platform.lbchildren.entity.TreeholePost;
import com.platform.lbchildren.entity.UserMemory;
import com.platform.lbchildren.entity.UserProfile;
import com.platform.lbchildren.mapper.AIChatHistoryMapper;
import com.platform.lbchildren.mapper.AlbumMapper;
import com.platform.lbchildren.mapper.DiaryMapper;
import com.platform.lbchildren.mapper.TreeholePostMapper;
import com.platform.lbchildren.mapper.UserMemoryMapper;
import com.platform.lbchildren.mapper.UserProfileMapper;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.MemoryDecay;
import com.platform.lbchildren.service.MemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 记忆服务实现
 * <p>
 * 阶段一：工作记忆（近况注入）——直接查原表组装「用户近况」注入 system prompt。
 * 阶段二：短期记忆——对话提炼为 user_memory 条目，按综合得分（衰减后重要性×0.5 +
 * 时间新鲜度×0.3 + 情感权重×0.2 + 关键词命中×0.3）排序 Top-K 注入，命中条目引用强化
 * （importance ×1.2 封顶 1.0，重置 last_accessed），并受 token 预算约束。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryServiceImpl implements MemoryService {

    private final DiaryMapper diaryMapper;
    private final AlbumMapper albumMapper;
    private final TreeholePostMapper treeholePostMapper;
    private final AIChatHistoryMapper aiChatHistoryMapper;
    private final UserMemoryMapper userMemoryMapper;
    private final UserProfileMapper userProfileMapper;

    /** 功能开关，false 时记忆相关能力全部回退 */
    @Value("${ai.memory.enabled:true}")
    private boolean enabled;

    /** 每类数据源最多取几条（近况） */
    @Value("${ai.memory.max-items:5}")
    private int maxItems;

    /** 单条内容最大长度，防止 prompt 过长 */
    @Value("${ai.memory.max-content-length:60}")
    private int maxContentLength;

    /** 相关记忆注入条数上限 */
    @Value("${ai.memory.top-k:5}")
    private int topK;

    /** 指数衰减系数（/天） */
    @Value("${ai.memory.decay-lambda:0.05}")
    private double decayLambda;

    /** 相关记忆段落的 token 预算（中文粗略按 1 token ≈ 2 字符估算） */
    @Value("${ai.memory.token-budget:500}")
    private int tokenBudget;

    /** 对话记忆初始重要性（阶段二：AI 对话 0.7） */
    private static final double CHAT_MEMORY_IMPORTANCE = 0.7;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M月d日");

    /** 情感词典：规则匹配，不调 LLM */
    private static final Map<String, String[]> EMOTION_WORDS = new HashMap<>();

    static {
        EMOTION_WORDS.put("HAPPY", new String[]{"开心", "高兴", "快乐", "喜欢", "好棒", "兴奋", "哈哈", "嘻嘻", "太好了", "好玩", "真棒"});
        EMOTION_WORDS.put("SAD", new String[]{"难过", "伤心", "哭", "委屈", "想哭", "不开心", "失落", "孤单", "想家", "想爸爸妈妈", "想妈妈", "想爸爸"});
        EMOTION_WORDS.put("ANGRY", new String[]{"生气", "讨厌", "烦", "气死", "愤怒", "可恶", "烦死", "吵架"});
        EMOTION_WORDS.put("ANXIOUS", new String[]{"担心", "害怕", "焦虑", "紧张", "不安", "恐惧", "睡不着", "压力"});
    }

    /** 情感权重（综合得分中的情感项） */
    private static final Map<String, Double> EMOTION_WEIGHT = Map.of(
            "ANXIOUS", 0.2, "SAD", 0.15, "ANGRY", 0.15, "HAPPY", 0.1, "NONE", 0.0);

    /** 中文常见停用词（关键词命中加分时剔除） */
    private static final Set<String> STOP_WORDS = Set.of(
            "的", "了", "吗", "呢", "啊", "吧", "我", "你", "他", "她", "它", "我们", "你们",
            "是", "有", "在", "和", "就", "都", "也", "很", "怎么", "什么", "为什么", "可以", "能",
            "不", "没", "这", "那", "一下", "一点", "一个", "告诉", "说说", "觉得", "感觉", "想", "知道");

    @Override
    public String getContext(UserPrincipal user) {
        if (!enabled) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        boolean isChild = "CHILD".equalsIgnoreCase(user.getRole());
        Long userId = user.getUserId();
        String role = user.getRole();

        // 1. 日记（仅儿童有）
        if (isChild) {
            diaryMapper.selectList(new LambdaQueryWrapper<Diary>()
                            .eq(Diary::getChildId, userId)
                            .orderByDesc(Diary::getCreatedAt)
                            .last("LIMIT " + maxItems))
                    .forEach(d -> lines.add(dateText(d.getCreatedAt()) + "：日记里写「" + truncate(d.getContent()) + "」"));

            // 2. 相册描述（仅儿童有）
            albumMapper.selectList(new LambdaQueryWrapper<Album>()
                            .eq(Album::getChildId, userId)
                            .orderByDesc(Album::getCreatedAt)
                            .last("LIMIT " + maxItems))
                    .forEach(a -> lines.add(dateText(a.getCreatedAt()) + "：相册新增照片" + albumDesc(a)));
        }

        // 3. AI 对话（本人提问）
        aiChatHistoryMapper.selectList(new LambdaQueryWrapper<AIChatHistory>()
                        .eq(AIChatHistory::getUserId, userId)
                        .eq(AIChatHistory::getUserRole, role)
                        .orderByDesc(AIChatHistory::getCreatedAt)
                        .last("LIMIT " + maxItems))
                .forEach(h -> lines.add(dateText(h.getCreatedAt()) + "：和AI聊过「" + truncate(h.getQuestion()) + "」"));

        // 4. 树洞自帖（口径：只取本人帖子，仅注入本人 prompt，不写入 user_memory、不进 L3 画像，
        //    因此不会流向家长/其他用户；措辞显式标注「匿名」，维持树洞匿名语义）
        treeholePostMapper.selectList(new LambdaQueryWrapper<TreeholePost>()
                        .eq(TreeholePost::getAuthorUserId, userId)
                        .eq(TreeholePost::getAuthorRole, role)
                        .orderByDesc(TreeholePost::getCreatedAt)
                        .last("LIMIT " + maxItems))
                .forEach(p -> lines.add(dateText(p.getCreatedAt()) + "：在树洞匿名倾诉过「" + truncate(p.getContent()) + "」"));

        if (lines.isEmpty()) {
            return "";
        }
        return "【用户近况】\n" + String.join("\n", lines);
    }

    @Override
    public void saveChatMemory(UserPrincipal user, String question, Long historyId) {
        if (!enabled || question == null || question.isBlank()) {
            return;
        }
        // 阶段三去重强化：同主体已存在内容相近的 active 记忆时，强化旧条目而非重复插入，
        // 避免同一话题反复产生冗余记忆（最短 4 字才参与匹配，防止短词误合并）
        String q = question.trim();
        List<UserMemory> existing = userMemoryMapper.selectList(new LambdaQueryWrapper<UserMemory>()
                .eq(UserMemory::getUserId, user.getUserId())
                .eq(UserMemory::getUserRole, user.getRole())
                .eq(UserMemory::getCategory, "CHAT")
                .eq(UserMemory::getStatus, "active"));
        for (UserMemory m : existing) {
            String c = m.getContent();
            if (c != null && c.length() >= 4 && (q.contains(c) || c.contains(q))) {
                reinforce(m, LocalDateTime.now());
                return;
            }
        }

        UserMemory memory = new UserMemory();
        memory.setUserId(user.getUserId());
        memory.setUserRole(user.getRole());
        memory.setCategory("CHAT");
        memory.setContent(q);
        memory.setEmotion(detectEmotion(question));
        memory.setImportance(CHAT_MEMORY_IMPORTANCE);
        memory.setLastAccessed(LocalDateTime.now());
        memory.setLevel("L2");
        memory.setStatus("active");
        memory.setSourceId(historyId);
        userMemoryMapper.insert(memory);
    }

    @Override
    public String getProfile(UserPrincipal user) {
        if (!enabled) {
            return "";
        }
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, user.getUserId())
                .eq(UserProfile::getUserRole, user.getRole())
                .last("LIMIT 1"));
        if (profile == null || profile.getProfileSummary() == null || profile.getProfileSummary().isBlank()) {
            return "";
        }
        return "【用户画像】\n" + profile.getProfileSummary().trim();
    }

    @Override
    public String getRelevantMemories(UserPrincipal user, String question) {
        if (!enabled) {
            return "";
        }
        List<UserMemory> candidates = userMemoryMapper.selectList(new LambdaQueryWrapper<UserMemory>()
                .eq(UserMemory::getUserId, user.getUserId())
                .eq(UserMemory::getUserRole, user.getRole())
                .eq(UserMemory::getStatus, "active"));
        if (candidates.isEmpty()) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        Set<String> keywords = extractKeywords(question);

        // 综合得分排序
        List<ScoredMemory> scored = new ArrayList<>();
        for (UserMemory m : candidates) {
            scored.add(new ScoredMemory(m, compositeScore(m, now, keywords)));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        List<ScoredMemory> top = scored.size() <= topK ? scored : scored.subList(0, topK);

        // 组装段落（受 token 预算约束），并收集命中项做引用强化
        StringBuilder sb = new StringBuilder();
        List<ScoredMemory> kept = new ArrayList<>();
        int budgetChars = tokenBudget * 2;
        for (ScoredMemory sm : top) {
            StringBuilder line = new StringBuilder("- ")
                    .append(dateText(sm.memory.getCreatedAt()))
                    .append("：孩子说「").append(truncate(sm.memory.getContent())).append("」");
            String emotion = emotionCn(sm.memory.getEmotion());
            if (!emotion.isEmpty()) {
                line.append("（").append(emotion).append("）");
            }
            line.append("\n");
            if (sb.length() + line.length() > budgetChars) {
                break;
            }
            sb.append(line);
            kept.add(sm);
        }
        if (sb.length() == 0) {
            return "";
        }

        // 引用强化：命中条目 importance ×1.2（封顶 1.0），重置 last_accessed；到 1.0 升级 L3
        for (ScoredMemory sm : kept) {
            reinforce(sm.memory, now);
        }

        return "【相关记忆】\n" + sb.toString().trim();
    }

    /**
     * 引用强化：importance ×1.2（封顶 1.0），刷新 last_accessed；
     * 达到 1.0 视为高频核心记忆，升级为 L3（永不归档，画像压缩时优先纳入）。
     */
    private void reinforce(UserMemory m, LocalDateTime now) {
        double boosted = Math.min(1.0, m.getImportance() * 1.2);
        UserMemory upd = new UserMemory();
        upd.setId(m.getId());
        upd.setImportance(boosted);
        upd.setLastAccessed(now);
        if (boosted >= 1.0) {
            upd.setLevel("L3");
        }
        userMemoryMapper.updateById(upd);
    }

    /** 综合得分 = 衰减后重要性×0.5 + 时间新鲜度×0.3 + 情感权重×0.2 + 关键词命中×0.3 */
    private double compositeScore(UserMemory m, LocalDateTime now, Set<String> keywords) {
        LocalDateTime created = m.getCreatedAt() == null ? now : m.getCreatedAt();
        double days = Duration.between(created, now).toDays();
        // 阶段五：情感加权衰减——高情绪记忆遗忘更慢
        double decayedImportance = MemoryDecay.decayedImportance(m.getImportance(), m.getEmotion(), days, decayLambda);

        LocalDateTime accessed = m.getLastAccessed() == null ? created : m.getLastAccessed();
        double accessDays = Duration.between(accessed, now).toDays();
        double freshness = Math.exp(-decayLambda * Math.max(0, accessDays));

        double emotion = EMOTION_WEIGHT.getOrDefault(m.getEmotion() == null ? "NONE" : m.getEmotion(), 0.0);

        double keywordHit = 0.0;
        if (!keywords.isEmpty() && m.getContent() != null) {
            for (String kw : keywords) {
                if (m.getContent().contains(kw)) {
                    keywordHit = 0.3;
                    break;
                }
            }
        }
        return decayedImportance * 0.5 + freshness * 0.3 + emotion * 0.2 + keywordHit;
    }

    /** 规则情感识别：命中词数最多的情感标签 */
    private String detectEmotion(String text) {
        String best = "NONE";
        int bestCount = 0;
        for (Map.Entry<String, String[]> e : EMOTION_WORDS.entrySet()) {
            int count = 0;
            for (String w : e.getValue()) {
                if (text.contains(w)) {
                    count++;
                }
            }
            if (count > bestCount) {
                bestCount = count;
                best = e.getKey();
            }
        }
        return best;
    }

    /** 简单中文关键词提取：按分隔符切分 + 剔除停用词（不引入分词库） */
    private Set<String> extractKeywords(String question) {
        if (question == null || question.isBlank()) {
            return Set.of();
        }
        String[] parts = question.split("[，。！？、,.!?\\s:：;；()（）\"“”'']+");
        Set<String> result = new HashSet<>();
        for (String p : parts) {
            String t = p.trim();
            if (t.isEmpty() || t.length() > 10 || STOP_WORDS.contains(t)) {
                continue;
            }
            result.add(t);
        }
        return result;
    }

    /** 相册描述：无描述时退化为一句占位 */
    private String albumDesc(Album album) {
        String desc = album.getDescription();
        return (desc == null || desc.isBlank()) ? "" : "（描述：" + truncate(desc) + "）";
    }

    private String dateText(LocalDateTime time) {
        return time == null ? "近期" : time.toLocalDate().format(DATE_FMT);
    }

    private String emotionCn(String emotion) {
        if (emotion == null) {
            return "";
        }
        switch (emotion) {
            case "HAPPY": return "开心";
            case "SAD": return "难过";
            case "ANGRY": return "生气";
            case "ANXIOUS": return "焦虑";
            default: return "";
        }
    }

    /** 截断长内容并压缩换行，避免注入 prompt 过长 */
    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim().replace('\n', ' ').replace('\r', ' ');
        return trimmed.length() <= maxContentLength ? trimmed : trimmed.substring(0, maxContentLength) + "…";
    }

    /** 带得分的内存条目 */
    private static class ScoredMemory {
        final UserMemory memory;
        final double score;

        ScoredMemory(UserMemory memory, double score) {
            this.memory = memory;
            this.score = score;
        }
    }
}
