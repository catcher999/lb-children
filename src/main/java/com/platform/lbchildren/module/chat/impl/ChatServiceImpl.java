package com.platform.lbchildren.module.chat.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.common.BusinessException;
import com.platform.lbchildren.common.ResultCode;
import com.platform.lbchildren.domain.entity.Child;
import com.platform.lbchildren.domain.entity.Message;
import com.platform.lbchildren.domain.mapper.ChildMapper;
import com.platform.lbchildren.domain.mapper.MessageMapper;
import com.platform.lbchildren.module.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实时聊天业务实现
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final MessageMapper messageMapper;
    private final ChildMapper childMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void handleChatMessage(Message msg) {
        // 校验发送方与接收方是否构成亲子关系，防止向非家庭成员发送消息
        checkFamilyRelation(msg);

        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);
        // 通过 WebSocket 推送给接收者
        String destination = "/user/" + msg.getReceiverId() + "/queue/messages";
        messagingTemplate.convertAndSend(destination, msg);
    }

    /**
     * 亲子关系校验：
     * - 家长（senderType=PARENT）只能给自己的孩子（receiver.parentId == senderId）发消息
     * - 儿童（senderType=CHILD）只能给自己的家长（receiverId == sender.parentId）发消息
     */
    private void checkFamilyRelation(Message msg) {
        if (msg.getSenderType() == Message.SenderType.PARENT) {
            Child receiver = childMapper.selectById(msg.getReceiverId());
            if (receiver == null || !receiver.getParentId().equals(msg.getSenderId())) {
                throw new BusinessException(ResultCode.NOT_FAMILY);
            }
        } else {
            Child sender = childMapper.selectById(msg.getSenderId());
            if (sender == null || !sender.getParentId().equals(msg.getReceiverId())) {
                throw new BusinessException(ResultCode.NOT_FAMILY);
            }
        }
    }

    @Override
    public List<Message> getConversation(Long user1Id, Long user2Id) {
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getSenderId, user1Id).eq(Message::getReceiverId, user2Id)
                        .or().eq(Message::getSenderId, user2Id).eq(Message::getReceiverId, user1Id))
                .orderByAsc(Message::getCreatedAt));
    }
}
