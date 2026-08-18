package com.platform.lbchildren.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.entity.AIChatHistory;
import com.platform.lbchildren.entity.Album;
import com.platform.lbchildren.entity.Diary;
import com.platform.lbchildren.entity.TreeholePost;
import com.platform.lbchildren.mapper.AIChatHistoryMapper;
import com.platform.lbchildren.mapper.AlbumMapper;
import com.platform.lbchildren.mapper.DiaryMapper;
import com.platform.lbchildren.mapper.TreeholePostMapper;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.MemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 记忆服务实现（阶段一：工作记忆 / 近况注入）
 * <p>
 * 直接查询原表（日记 / 相册 / AI 对话 / 树洞自帖），组装「用户近况」段落注入 system prompt。
 * 记忆主体 = (userId, role)；日记/相册按 child_id 关联（仅儿童有）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryServiceImpl implements MemoryService {

    private final DiaryMapper diaryMapper;
    private final AlbumMapper albumMapper;
    private final TreeholePostMapper treeholePostMapper;
    private final AIChatHistoryMapper aiChatHistoryMapper;

    /** 功能开关，false 时直接返回空串（回退固定 prompt） */
    @Value("${ai.memory.enabled:true}")
    private boolean enabled;

    /** 每类数据源最多取几条 */
    @Value("${ai.memory.max-items:5}")
    private int maxItems;

    /** 单条内容最大长度，防止 prompt 过长 */
    @Value("${ai.memory.max-content-length:60}")
    private int maxContentLength;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M月d日");

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

        // 4. 树洞自帖（只取本人；对外保持匿名语义，此处仅供本人记忆注入，不暴露身份）
        treeholePostMapper.selectList(new LambdaQueryWrapper<TreeholePost>()
                        .eq(TreeholePost::getAuthorUserId, userId)
                        .eq(TreeholePost::getAuthorRole, role)
                        .orderByDesc(TreeholePost::getCreatedAt)
                        .last("LIMIT " + maxItems))
                .forEach(p -> lines.add(dateText(p.getCreatedAt()) + "：在树洞倾诉过「" + truncate(p.getContent()) + "」"));

        if (lines.isEmpty()) {
            return "";
        }
        return "【用户近况】\n" + String.join("\n", lines);
    }

    /** 相册描述：无描述时退化为一句占位 */
    private String albumDesc(Album album) {
        String desc = album.getDescription();
        return (desc == null || desc.isBlank()) ? "" : "（描述：" + truncate(desc) + "）";
    }

    private String dateText(LocalDateTime time) {
        return time == null ? "近期" : time.toLocalDate().format(DATE_FMT);
    }

    /** 截断长内容并压缩换行，避免注入 prompt 过长 */
    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim().replace('\n', ' ').replace('\r', ' ');
        return trimmed.length() <= maxContentLength ? trimmed : trimmed.substring(0, maxContentLength) + "…";
    }
}
