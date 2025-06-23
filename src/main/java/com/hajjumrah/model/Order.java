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
    private String id; // MongoDB ObjectId
    
    private String orderId; // 6-digit system generated order ID
    
    private String userId;
    private List<OrderItem> items;
    private double totalAmount;
    private String status; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    private LocalDateTime orderDate;
    
    // Enhanced address fields
    private String fullName;
    private String flatNo;
    private String apartmentName;
    private String floor;
    private String streetName;
    private String nearbyLandmark;
    private String city;
    private String pincode;
    private String state;
    private String country;
    
    // Legacy field for backward compatibility
    private String shippingAddress;
    private String contactNumber;
    private String email;
    private String paymentStatus; // PENDING, PAID, FAILED
    private String paymentMethod;
    private String transactionId; // Payment transaction ID
} 