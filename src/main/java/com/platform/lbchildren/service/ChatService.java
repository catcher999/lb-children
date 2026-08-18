package com.platform.lbchildren.service;

import com.platform.lbchildren.entity.Message;

import java.util.List;

/**
 * 实时聊天业务接口
 */
public interface ChatService {

    /** 处理并转发 WebSocket 聊天消息 */
    void handleChatMessage(Message msg);

    /** 获取两人之间的聊天记录（排序） */
    List<Message> getConversation(Long user1Id, Long user2Id);
}
