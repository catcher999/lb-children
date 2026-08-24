package com.platform.lbchildren.module.ai.impl;

import com.platform.lbchildren.domain.entity.Literature;
import com.platform.lbchildren.domain.mapper.LiteratureMapper;
import com.platform.lbchildren.module.ai.service.RagService;
import com.platform.lbchildren.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RAG 知识检索实现（阶段四）
 * <p>
 * 检索策略：加载文献库（当前数据量小），对提问提取关键词，在标题/要点/关键词上计数打分，
 * 取 Top-K 组装【权威参考】段落。危机关键词命中时固定输出安全引导，不机械引用文献。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    /** 危机关键词：命中即走安全引导红线 */
    private static final Set<String> CRISIS_WORDS = Set.of(
            "自杀", "自伤", "自残", "轻生", "不想活", "想死", "活不下去", "伤害自己",
            "割腕", "跳楼", "结束生命", "遗书");

    /** 危机安全引导段落（固定文案，保证任何情况下输出安全引导） */
    private static final String CRISIS_SAFE_TEXT =
            "如果你现在有伤害自己或结束生命的念头，请一定不要独自承受：" +
            "① 立刻拨打 12355（共青团青少年心理援助热线，7×24 小时）或 12356（全国心理援助热线）；" +
            "② 马上告诉身边信任的成年人（家长、老师），或拨打 120 寻求紧急帮助；" +
            "③ 你并不孤单，有人愿意听你说，你的安全是最重要的。";

    /** 中文常见停用词（关键词提取时剔除） */
    private static final Set<String> STOP_WORDS = Set.of(
            "的", "了", "吗", "呢", "啊", "吧", "我", "你", "他", "她", "它", "我们", "你们",
            "是", "有", "在", "和", "就", "都", "也", "很", "怎么", "什么", "为什么", "可以", "能",
            "不", "没", "这", "那", "一下", "一点", "一个", "告诉", "说说", "觉得", "感觉", "想", "知道",
            "老师", "家长", "孩子", "应该", "如何");

    private final LiteratureMapper literatureMapper;

    /** RAG 通道开关 */
    @Value("${ai.rag.enabled:true}")
    private boolean enabled;

    /** 注入文献条数上限 */
    @Value("${ai.rag.top-k:3}")
    private int topK;

    @Override
    public String getReference(UserPrincipal user, String question) {
        if (!enabled || question == null || question.isBlank()) {
            return "";
        }
        // 危机安全线：命中危机词，直接输出安全引导，不机械引用文献
        if (containsAny(question, CRISIS_WORDS)) {
            return "【安全求助】\n" + CRISIS_SAFE_TEXT;
        }

        List<Literature> all = literatureMapper.selectList(null);
        if (all.isEmpty()) {
            return "";
        }
        Set<String> keywords = extractKeywords(question);
        if (keywords.isEmpty()) {
            return "";
        }

        // 受众过滤：儿童提问只取适用儿童或通用条目，家长提问取家长或通用条目
        boolean isChild = "CHILD".equalsIgnoreCase(user.getRole());

        List<Scored> scored = new ArrayList<>();
        for (Literature lit : all) {
            if (isChild && "PARENT".equals(lit.getAudience())) {
                continue;
            }
            if (!isChild && "CHILD".equals(lit.getAudience())) {
                continue;
            }
            int hit = score(lit, keywords, question);
            if (hit > 0) {
                scored.add(new Scored(lit, hit));
            }
        }
        if (scored.isEmpty()) {
            return "";
        }
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        List<Scored> top = scored.size() <= topK ? scored : scored.subList(0, topK);
        log.info("RAG 命中 {} 条，注入 Top-{}", scored.size(), top.size());

        StringBuilder sb = new StringBuilder("【权威参考】\n");
        for (Scored s : top) {
            sb.append("- ").append(s.lit.getTitle())
              .append("（").append(s.lit.getSource()).append("）：")
              .append(s.lit.getSummary()).append("\n");
        }
        return sb.toString().trim();
    }

    /** 关键词计数打分：keywords 字段命中 +2，标题命中 +1，要点正文命中 +1 */
    private int score(Literature lit, Set<String> keywords, String question) {
        int hit = 0;
        for (String kw : keywords) {
            boolean inKw = lit.getKeywords() != null && lit.getKeywords().contains(kw);
            boolean inTitle = lit.getTitle() != null && lit.getTitle().contains(kw);
            boolean inSummary = lit.getSummary() != null && lit.getSummary().contains(kw);
            if (inKw) {
                hit += 2;
            }
            if (inTitle) {
                hit += 1;
            }
            if (inSummary) {
                hit += 1;
            }
        }
        // 文献关键词 token 直接命中提问：解决"沉迷网络游戏"等复合词无法切分、整串匹配落空的问题
        if (lit.getKeywords() != null) {
            for (String token : lit.getKeywords().split("\\s+")) {
                if (!token.isEmpty() && question.contains(token)) {
                    hit += 2;
                }
            }
        }
        return hit;
    }

    private boolean containsAny(String text, Set<String> words) {
        for (String w : words) {
            if (text.contains(w)) {
                return true;
            }
        }
        return false;
    }

    /** 简单中文关键词提取：分隔符切分 + 剔除停用词（与记忆通道同一套思路，不引入分词库） */
    private Set<String> extractKeywords(String question) {
        String[] parts = question.split("[，。！？、,.!?\\s:：;；()（）\"“”'']+");
        Set<String> result = new HashSet<>();
        for (String p : parts) {
            String t = p.trim();
            if (t.isEmpty() || t.length() > 8 || STOP_WORDS.contains(t)) {
                continue;
            }
            result.add(t);
        }
        return result;
    }

    private static class Scored {
        final Literature lit;
        final int score;

        Scored(Literature lit, int score) {
            this.lit = lit;
            this.score = score;
        }
    }
}
