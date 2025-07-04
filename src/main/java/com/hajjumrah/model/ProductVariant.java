package com.hajjumrah.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    private String size;
    private double price;
    private Double weight; // in kg
    private Double length; // in cm
    private Double width;  // in cm
    private Double height; // in cm

    // GST Configuration
    private double gstRate = 5.0; // Default 5% GST rate
    private boolean gstInclusive = false; // Whether the price includes GST or not

    // Helper methods for GST calculation
    public double getGstAmount() {
        return (price * gstRate) / 100.0;
    }

    public double getPriceWithGst() {
        return gstInclusive ? price : price + getGstAmount();
    }

    public double getPriceWithoutGst() {
        return gstInclusive ? price - getGstAmount() : price;
    }
} 