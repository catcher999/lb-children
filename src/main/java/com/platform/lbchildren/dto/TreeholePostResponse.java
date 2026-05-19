package com.platform.lbchildren.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TreeholePostResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private int replyCount;
    private List<TreeholeReplyResponse> replies;   // 仅在详情时填充
}