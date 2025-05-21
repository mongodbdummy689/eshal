package com.hajjumrah.model;

import lombok.Data;

@Data
public class OrderItem {
    private String productId;
    private String productName;
    private int quantity;
    private double price;
    private double subtotal;
} 