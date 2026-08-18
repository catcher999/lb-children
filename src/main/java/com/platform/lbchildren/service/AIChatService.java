package com.platform.lbchildren.service;

import com.platform.lbchildren.dto.AIChatResponse;
import com.platform.lbchildren.entity.AIChatHistory;
import com.platform.lbchildren.security.UserPrincipal;

import java.util.List;

/**
 * AI 聊天业务接口
 */
public interface AIChatService {

    /** 提问：内容安全过滤 -> 儿童限流 -> 调用大模型 -> 保存历史 */
    AIChatResponse ask(UserPrincipal user, String question);

    /** 查询某用户的历史提问记录 */
    List<AIChatHistory> getHistory(Long userId);
}
