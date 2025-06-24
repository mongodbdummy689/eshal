package com.hajjumrah.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "images")
public class Image {
    @Id
    private String id;
    private String title;
    private String description;
    private String url;
    private String cloudinaryPublicId;
    private String productId;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
} 