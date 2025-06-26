package com.hajjumrah.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.io.Serializable;

@Data
@Document(collection = "cart_items")
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String userId;
    private String productId;
    private Integer quantity;
    private Double price;

    // Variant information for products with variants
    private String selectedVariant; // e.g., "Wafa", "Royal", "46\"", "50\"", etc.
    private Double variantPrice; // Price of the selected variant

    // Source tracking for special handling
    private String source; // e.g., "tohfa-e-khulus" for items from that page

    @Transient
    private transient Product product;

    public void setProduct(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
} 