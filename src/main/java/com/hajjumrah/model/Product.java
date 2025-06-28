package com.hajjumrah.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.util.List;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private double price; // Default price, can be the base price or first variant's price
    private String imageUrl;
    private String category;
    private boolean inStock;
    private int stockQuantity;

    // For products with multiple variants
    private String priceType; // e.g., "single" or "variant"
    private List<ProductVariant> variants;

    // Additional fields for Janamaz products
    private Double pricePerPiece;
    private Double pricePerDozen;
    private String size;

    // Shipping and dimension fields
    private Double weight; // in kg (optional, for actual weight if needed)
    private Double length; // in cm
    private Double width;  // in cm
    private Double height; // in cm
} 