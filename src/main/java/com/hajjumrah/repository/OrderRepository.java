package com.hajjumrah.repository;

import com.hajjumrah.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByUserIdOrderByOrderDateDesc(String userId);
    
    // Find order by the 6-digit orderId
    Optional<Order> findByOrderId(String orderId);
    
    // Check if orderId exists
    boolean existsByOrderId(String orderId);
} 