package com.platform.lbchildren.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 教育资源实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("edu_resource")
public class EduResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** VIDEO / COURSE / ARTICLE */
    private String type;

    private String url;

    private String description;

    private String coverUrl;
}
