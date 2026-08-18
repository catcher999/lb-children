package com.platform.lbchildren.service;

import com.platform.lbchildren.security.UserPrincipal;

/**
 * RAG 知识检索服务（阶段四：权威文献检索注入）
 * <p>
 * 定位：只检索权威心理/教育/安全公开指南，绝不检索用户个人数据（个人数据走记忆通道）。
 * 数据量小，使用关键词命中检索（后续可平滑替换为 MySQL 全文索引），命中段落注入 system prompt。
 */
public interface RagService {

    /**
     * 检索与当前问题相关的权威文献段落。
     * 命中危机关键词时，输出固定安全引导（12355/12356/求助成人），不再机械引用文献。
     *
     * @param user     当前登录用户（用于受众过滤，仅取 audience 匹配的条目）
     * @param question 用户本次提问
     * @return 权威参考段落文本；无命中时返回空字符串
     */
    String getReference(UserPrincipal user, String question);
}
