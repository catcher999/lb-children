package com.platform.lbchildren.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 聊天消息请求（WebSocket 消息体）
 */
@Data
public class MessageRequest {

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    private String content;

    private String mediaUrl;

    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    @NotBlank(message = "发送方类型不能为空")
    private String senderType;
}
