package com.platform.lbchildren.module.ai.service;

import com.platform.lbchildren.security.UserPrincipal;

/**
 * AI 记忆服务接口
 * <p>
 * 阶段一：工作记忆（近况注入）——直接查询原表组装「用户近况」，验证跨会话记忆效果。
 * 阶段二：短期记忆——对话提炼为记忆条目落 user_memory 表，按综合得分（重要性/时间新鲜度/
 * 情感权重/关键词命中）检索 Top-K 注入，命中条目做引用强化。
 */
public interface MemoryService {

    /**
     * 组装「用户近况」段落，供 system prompt 注入。
     *
     * @param user 当前登录用户（记忆主体 = userId + role）
     * @return 近况段落文本；无可用记忆时返回空字符串（冷启动回退到固定 prompt）
     */
    String getContext(UserPrincipal user);

    /**
     * 保存一次对话为短期记忆条目（阶段二）。
     *
     * @param user     当前登录用户
     * @param question 用户本次提问
     * @param historyId 对应 ai_chat_history 记录 id（溯源）
     */
    void saveChatMemory(UserPrincipal user, String question, Long historyId);

    /**
     * 检索与当前问题相关的短期记忆（阶段二），按综合得分排序 Top-K 注入。
     * 命中条目做引用强化（importance ×1.2 封顶 1.0，重置 last_accessed）。
     *
     * @param user     当前登录用户
     * @param question 用户本次提问（用于关键词命中加分）
     * @return 相关记忆段落文本；无命中时返回空字符串
     */
    String getRelevantMemories(UserPrincipal user, String question);

    /**
     * 取长期画像段落（阶段三：L3 核心记忆，由定时任务把短期记忆压缩生成）。
     * 注入优先级最高（L3 &gt; 相关记忆 L2 &gt; 近况 L1）。
     *
     * @param user 当前登录用户
     * @return 画像段落文本；尚无画像时返回空字符串（冷启动回退到固定 prompt）
     */
    String getProfile(UserPrincipal user);
}
