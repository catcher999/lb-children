package com.platform.lbchildren.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 实时聊天消息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送方角色 PARENT / CHILD */
    private SenderType senderType;

    private Long senderId;

    private Long receiverId;

    private String content;

    private String mediaUrl;

    /** 消息类型 CHAT / VOICE / PHOTO */
    private MessageType messageType;

    private LocalDateTime createdAt;

    public enum SenderType {
        PARENT, CHILD
    }

    public enum MessageType {
        CHAT, VOICE, PHOTO
    }
}
