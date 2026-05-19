package com.platform.lbchildren.service;

import com.platform.lbchildren.entity.Message;
import com.platform.lbchildren.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void handleChatMessage(Message msg) {
        msg.setCreatedAt(LocalDateTime.now());
        messageRepository.save(msg);
        String destination = "/user/" + msg.getReceiverId() + "/queue/messages";
        messagingTemplate.convertAndSend(destination, msg);
    }

    public List<Message> getConversation(Long user1Id, Long user2Id) {
        return messageRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
                        user1Id, user2Id, user2Id, user1Id);
    }
}