package com.hajjumrah.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String fullName;
    private String email;
    private String mobileNumber;
    private String password;
    private String role = "USER";
    
    // Shipping Address fields
    private String flatNo;
    private String apartmentName;
    private String floor;
    private String streetName;
    private String nearbyLandmark;
    private String city;
    private String state;
    private String country;
    private String pincode;
} 