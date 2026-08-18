package com.platform.lbchildren.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.common.BusinessException;
import com.platform.lbchildren.entity.UserMemory;
import com.platform.lbchildren.entity.UserProfile;
import com.platform.lbchildren.mapper.UserMemoryMapper;
import com.platform.lbchildren.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 记忆维护定时任务（阶段三）
 * <p>
 * 每天凌晨执行两项维护：
 * 1. 遗忘归档：衰减后重要性低于阈值的 active 记忆标记为 archived（L3 核心记忆除外）。
 * 2. 画像压缩：把每个主体的短期记忆交给 LLM 压缩成长期画像，写入 user_profile 表，
 *    作为 L3 核心记忆供后续注入。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryMaintainTask {

    /** 衰减后重要性低于该值即遗忘归档 */
    private static final double ARCHIVE_THRESHOLD = 0.2;

    /** 画像压缩至少需要的记忆条数，过少不压缩避免噪声 */
    private static final int MIN_MEMORIES_FOR_PROFILE = 3;

    /** 画像重建最小间隔（天），控制 LLM 调用成本 */
    private static final int PROFILE_REBUILD_DAYS = 7;

    /** 画像压缩最多纳入的记忆条数 */
    private static final int MAX_PROFILE_INPUT = 20;

    /** LLM 压缩画像的系统提示词 */
    private static final String PROFILE_SYSTEM_PROMPT =
            "你是儿童心理成长记忆整理助手。请把下面列出的记忆条目压缩成一段不超过120字的中文" +
            "长期用户画像，用第三人称客观概括该用户的性格特点、近期关注点、情感倾向、成长需求。" +
            "只输出画像正文，不要任何前缀、编号或解释。若信息不足，给出保守简短描述。";

    private final UserMemoryMapper userMemoryMapper;
    private final UserProfileMapper userProfileMapper;
    private final AiCompletionClient aiCompletionClient;

    /** 功能开关，false 时跳过全部维护 */
    @Value("${ai.memory.enabled:true}")
    private boolean enabled;

    /** 指数衰减系数（/天），与检索保持一致 */
    @Value("${ai.memory.decay-lambda:0.05}")
    private double decayLambda;

    /** 每天凌晨 3 点执行 */
    @Scheduled(cron = "0 0 3 * * ?")
    public void maintain() {
        if (!enabled) {
            return;
        }
        archiveForgotten();
        compressProfiles();
    }

    /** 遗忘归档：衰减后重要性 < 阈值 的 active 记忆标记为 archived */
    private void archiveForgotten() {
        LocalDateTime now = LocalDateTime.now();
        List<UserMemory> actives = userMemoryMapper.selectList(new LambdaQueryWrapper<UserMemory>()
                .eq(UserMemory::getStatus, "active"));
        int archived = 0;
        for (UserMemory m : actives) {
            if ("L3".equals(m.getLevel())) {
                continue; // L3 核心记忆不归档
            }
            LocalDateTime created = m.getCreatedAt() == null ? now : m.getCreatedAt();
            double days = Duration.between(created, now).toDays();
            // 阶段五：情感加权衰减——高情绪记忆遗忘更慢，避免过早归档
            double decayed = MemoryDecay.decayedImportance(m.getImportance(), m.getEmotion(), days, decayLambda);
            if (decayed < ARCHIVE_THRESHOLD) {
                UserMemory upd = new UserMemory();
                upd.setId(m.getId());
                upd.setStatus("archived");
                userMemoryMapper.updateById(upd);
                archived++;
            }
        }
        if (archived > 0) {
            log.info("记忆遗忘归档完成，共归档 {} 条", archived);
        }
    }

    /** 画像压缩：按 (user_id, user_role) 分组，对满足条件的主体调用 LLM 压缩并 upsert 画像 */
    private void compressProfiles() {
        List<UserMemory> actives = userMemoryMapper.selectList(new LambdaQueryWrapper<UserMemory>()
                .eq(UserMemory::getStatus, "active"));
        Map<String, List<UserMemory>> byUser = new HashMap<>();
        for (UserMemory m : actives) {
            byUser.computeIfAbsent(m.getUserId() + ":" + m.getUserRole(), k -> new ArrayList<>()).add(m);
        }
        for (Map.Entry<String, List<UserMemory>> e : byUser.entrySet()) {
            String[] key = e.getKey().split(":");
            compressOne(Long.valueOf(key[0]), key[1], e.getValue());
        }
    }

    private void compressOne(Long userId, String role, List<UserMemory> memories) {
        if (memories.size() < MIN_MEMORIES_FOR_PROFILE) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        UserProfile existing = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId)
                .eq(UserProfile::getUserRole, role)
                .last("LIMIT 1"));
        if (existing != null && existing.getUpdatedAt() != null
                && Duration.between(existing.getUpdatedAt(), now).toDays() < PROFILE_REBUILD_DAYS) {
            return; // 画像还新鲜，不重复压缩
        }

        // 优先纳入 L3 核心记忆，其余按重要性降序
        memories.sort((a, b) -> {
            boolean aL3 = "L3".equals(a.getLevel());
            boolean bL3 = "L3".equals(b.getLevel());
            if (aL3 != bL3) {
                return aL3 ? -1 : 1;
            }
            return Double.compare(b.getImportance(), a.getImportance());
        });
        List<UserMemory> input = memories.size() <= MAX_PROFILE_INPUT
                ? memories : memories.subList(0, MAX_PROFILE_INPUT);

        StringBuilder sb = new StringBuilder();
        for (UserMemory m : input) {
            sb.append("- [").append(m.getEmotion() == null ? "NONE" : m.getEmotion())
              .append("] ").append(m.getContent()).append("\n");
        }

        String summary;
        try {
            summary = aiCompletionClient.complete(PROFILE_SYSTEM_PROMPT, sb.toString());
        } catch (BusinessException ex) {
            log.warn("画像压缩 LLM 调用失败，跳过 user={} role={}", userId, role);
            return;
        }
        String trimmed = summary == null ? "" : summary.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        if (existing == null) {
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            profile.setUserRole(role);
            profile.setProfileSummary(trimmed);
            userProfileMapper.insert(profile);
        } else {
            UserProfile upd = new UserProfile();
            upd.setId(existing.getId());
            upd.setProfileSummary(trimmed);
            upd.setUpdatedAt(now);
            userProfileMapper.updateById(upd);
        }
        log.info("画像压缩完成 user={} role={}", userId, role);
    }
}
