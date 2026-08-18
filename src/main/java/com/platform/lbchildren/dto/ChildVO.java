package com.platform.lbchildren.dto;

import lombok.Data;

/**
 * 儿童信息视图对象：返回给前端，隐藏 parentId 等内部字段
 */
@Data
public class ChildVO {
    private Long id;
    private String nickname;
    private Integer age;
    private String avatar;
    /** 家长是否已授权查看孩子画像（阶段五） */
    private Boolean profileConsent;
}
