package com.hajjumrah.repository;

import com.hajjumrah.model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImageRepository extends MongoRepository<Image, String> {
} 