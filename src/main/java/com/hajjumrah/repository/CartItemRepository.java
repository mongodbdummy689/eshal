package com.hajjumrah.repository;

import com.hajjumrah.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends MongoRepository<CartItem, String> {
    List<CartItem> findByUserId(String userId);
    void deleteByUserId(String userId);
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);
} 