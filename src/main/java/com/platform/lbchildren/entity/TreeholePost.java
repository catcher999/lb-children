package com.platform.lbchildren.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "treehole_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeholePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String imageUrl;

    // 内部记录发布者，但不对外暴露
    private Long authorUserId;
    private String authorRole;   // PARENT 或 CHILD

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TreeholeReply> replies = new ArrayList<>();
}
