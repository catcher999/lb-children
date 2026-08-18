package com.platform.lbchildren.service;

import com.platform.lbchildren.security.UserPrincipal;

/**
 * AI 记忆服务接口
 * <p>
 * 阶段一：工作记忆（近况注入）——直接查询原表组装「用户近况」，
 * 验证跨会话记忆效果。本阶段不新增记忆表。
 */
public interface MemoryService {

    /**
     * 组装「用户近况」段落，供 system prompt 注入。
     *
     * @param user 当前登录用户（记忆主体 = userId + role）
     * @return 近况段落文本；无可用记忆时返回空字符串（冷启动回退到固定 prompt）
     */
    String getContext(UserPrincipal user);
}
