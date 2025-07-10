package com.hajjumrah.repository;

import com.hajjumrah.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByUserIdOrderByOrderDateDesc(String userId);
    List<Order> findAllByOrderByOrderDateDesc();
    
    // Find order by the 6-digit orderId
    Optional<Order> findByOrderId(String orderId);
    
    // Check if orderId exists
    boolean existsByOrderId(String orderId);
    
    // Search orders with pagination
    @Query("{'$or': [{'orderId': {$regex: ?0, $options: 'i'}}, {'fullName': {$regex: ?0, $options: 'i'}}, {'email': {$regex: ?0, $options: 'i'}}, {'contactNumber': {$regex: ?0, $options: 'i'}}]}")
    Page<Order> searchOrders(String searchTerm, Pageable pageable);
    
    // Find orders by status with pagination
    Page<Order> findByStatusOrderByOrderDateDesc(String status, Pageable pageable);
    
    // Find all orders with pagination
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
} 