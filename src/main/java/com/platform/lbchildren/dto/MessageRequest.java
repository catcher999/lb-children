package com.platform.lbchildren.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private Long receiverId;
    private String content;
    private String mediaUrl;
    private String messageType;
    private String senderType;
}
