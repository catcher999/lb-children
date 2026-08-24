package com.platform.lbchildren.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TreeholeReplyResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
}