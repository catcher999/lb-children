package com.platform.lbchildren.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SenderType senderType;

    private Long senderId;
    private Long receiverId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum SenderType {
        PARENT, CHILD
    }

    public enum MessageType {
        CHAT, VOICE, PHOTO
    }
}