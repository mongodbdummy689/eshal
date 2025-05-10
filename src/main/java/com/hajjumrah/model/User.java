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
} 