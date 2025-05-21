package com.hajjumrah.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    
    private String userId;
    private List<OrderItem> items;
    private double totalAmount;
    private String status; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String contactNumber;
    private String email;
    private String paymentStatus; // PENDING, PAID, FAILED
    private String paymentMethod;
} 