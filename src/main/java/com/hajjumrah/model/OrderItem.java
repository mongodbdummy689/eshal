package com.hajjumrah.model;

import java.math.BigDecimal;

public class OrderItem {

    private String productId;
    private String productName;
    private String imageUrl;
    private String selectedVariant;
    private BigDecimal price; // Total price for this item (not unit price)
    private BigDecimal gstAmount; // GST amount for this item
    private int quantity;
    private String source; // e.g., "tohfa-e-khulus" for items from that page

    // Constructors, Getters, and Setters

    public OrderItem() {
    }

    public OrderItem(String productId, String productName, String imageUrl, String selectedVariant, BigDecimal price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.selectedVariant = selectedVariant;
        this.price = price;
        this.quantity = quantity;
        this.gstAmount = BigDecimal.ZERO; // Initialize GST amount
    }

    public OrderItem(String productId, String productName, String imageUrl, String selectedVariant, BigDecimal price, int quantity, String source) {
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.selectedVariant = selectedVariant;
        this.price = price;
        this.quantity = quantity;
        this.source = source;
        this.gstAmount = BigDecimal.ZERO; // Initialize GST amount
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSelectedVariant() {
        return selectedVariant;
    }

    public void setSelectedVariant(String selectedVariant) {
        this.selectedVariant = selectedVariant;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
} 