package com.platform.lbchildren.controller;

import com.platform.lbchildren.dto.MessageRequest;
import com.platform.lbchildren.entity.Message;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChatController {

    @Autowired
    private ChatService chatService;

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

    @GetMapping("/api/chat/history")
    public ResponseEntity<?> getChatHistory(@RequestParam Long otherUserId,
                                            @AuthenticationPrincipal UserPrincipal user) {
        List<Message> messages = chatService.getConversation(user.getUserId(), otherUserId);
        return ResponseEntity.ok(messages);
    }
}
