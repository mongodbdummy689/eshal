package com.hajjumrah.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "cart_items")
public class CartItem {
    @Id
    private String id;

    private String userId;
    private String productId;
    private int quantity;
    private double price;
} 