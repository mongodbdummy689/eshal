package com.hajjumrah.repository;

import com.hajjumrah.model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ImageRepository extends MongoRepository<Image, String> {
    Optional<Image> findByCloudinaryPublicId(String cloudinaryPublicId);
} 