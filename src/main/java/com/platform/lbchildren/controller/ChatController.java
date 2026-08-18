package com.platform.lbchildren.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.dto.MessageRequest;
import com.platform.lbchildren.entity.Message;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实时聊天接口（WebSocket 消息处理 + HTTP 历史查询）
 */
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 通过 WebSocket 接收并转发聊天消息
     * 客户端订阅 /app/chat 发送，服务端推送到 /user/{receiverId}/queue/messages
     */
    @MessageMapping("/chat")
    public void processMessage(@Payload MessageRequest request,
                               @AuthenticationPrincipal UserPrincipal user) {
        Message msg = new Message();
        msg.setSenderType(Message.SenderType.valueOf(request.getSenderType()));
        msg.setSenderId(user.getUserId());
        msg.setReceiverId(request.getReceiverId());
        msg.setContent(request.getContent());
        msg.setMediaUrl(request.getMediaUrl());
        msg.setMessageType(Message.MessageType.valueOf(request.getMessageType()));
        chatService.handleChatMessage(msg);
    }

    /** 查询与某用户的聊天记录 */
    @GetMapping("/api/chat/history")
    public Result<List<Message>> getChatHistory(@RequestParam Long otherUserId,
                                                @AuthenticationPrincipal UserPrincipal user) {
        return Result.ok(chatService.getConversation(user.getUserId(), otherUserId));
    }
}