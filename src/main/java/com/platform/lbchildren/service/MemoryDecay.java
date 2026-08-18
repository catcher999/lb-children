package com.platform.lbchildren.service;

import java.util.Map;

/**
 * 记忆衰减工具（阶段五：情感加权）
 * <p>
 * 高情绪记忆（焦虑/难过/生气等）对儿童心理健康更重要，应遗忘得更慢，便于 AI 长期关注。
 * 情感加权通过调整有效衰减系数 λ 实现：λ 越小遗忘越慢；无情感标签（NONE）保持基准衰减。
 */
public final class MemoryDecay {

    private MemoryDecay() {
    }

    /** 情感标签 → 衰减系数倍率（&lt;1 表示遗忘更慢；NONE 保持基准） */
    private static final Map<String, Double> EMOTION_DECAY_MULTIPLIER = Map.of(
            "ANXIOUS", 0.5,  // 焦虑：最需长期关注，遗忘最慢
            "SAD", 0.5,      // 难过：慢遗忘
            "ANGRY", 0.6,    // 生气：较慢遗忘
            "HAPPY", 0.8,    // 开心：略慢，快乐回忆值得保留
            "NONE", 1.0);

    /**
     * 情感加权后的有效衰减系数（/天）。
     *
     * @param emotion    记忆情感标签（HAPPY/SAD/ANGRY/ANXIOUS/NONE，可为 null）
     * @param baseLambda 基础衰减系数
     * @return 有效衰减系数
     */
    public static double effectiveLambda(String emotion, double baseLambda) {
        double multiplier = EMOTION_DECAY_MULTIPLIER.getOrDefault(emotion == null ? "NONE" : emotion, 1.0);
        return baseLambda * multiplier;
    }

    /**
     * 情感加权衰减后的重要性：importance × exp(-有效λ × 天数)。
     *
     * @param importance 初始重要性
     * @param emotion    情感标签（可为 null）
     * @param days       距创建天数
     * @param baseLambda 基础衰减系数
     * @return 衰减后重要性
     */
    public static double decayedImportance(double importance, String emotion, double days, double baseLambda) {
        double lambda = effectiveLambda(emotion, baseLambda);
        return importance * Math.exp(-lambda * Math.max(0, days));
    }
}
