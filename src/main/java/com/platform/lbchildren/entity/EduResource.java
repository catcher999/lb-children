package com.platform.lbchildren.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "edu_resource")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EduResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type;
    private String url;
    private String description;
}