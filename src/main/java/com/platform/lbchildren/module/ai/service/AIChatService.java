package com.platform.lbchildren.module.ai.service;

import com.platform.lbchildren.domain.dto.AIChatResponse;
import com.platform.lbchildren.domain.dto.AiSessionVO;
import com.platform.lbchildren.domain.entity.AIChatHistory;
import com.platform.lbchildren.security.UserPrincipal;

import java.util.List;

/**
 * AI 聊天业务接口
 */
public interface AIChatService {

    /** 提问：内容安全过滤 -> 儿童限流 -> 调用大模型 -> 保存历史（B1：支持会话续写/新建 + 多轮上下文） */
    AIChatResponse ask(UserPrincipal user, String question, Long sessionId);

    /** 查询某用户的历史提问记录 */
    List<AIChatHistory> getHistory(Long userId);

    /** 会话列表（B1） */
    List<AiSessionVO> listSessions(UserPrincipal user);

    /** 某会话内记录（B1，已做属主校验） */
    List<AIChatHistory> getSessionMessages(UserPrincipal user, Long sessionId);
}