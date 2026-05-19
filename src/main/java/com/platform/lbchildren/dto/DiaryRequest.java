package com.platform.lbchildren.dto;

import lombok.Data;

@Data
public class DiaryRequest {
    private String content;
    private String imageUrl;
    private Boolean isAnonymous;
}