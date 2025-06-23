package com.hajjumrah.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;

@Service
public class NotificationService {
    
    @Value("${sms.api.key:}")
    private String smsApiKey;
    
    @Value("${sms.api.url:}")
    private String smsApiUrl;
    
    private final RestTemplate restTemplate;
    
    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public void sendSMS(String phoneNumber, String message) {
        try {
            // Check if SMS configuration is properly set up
            if (smsApiKey == null || smsApiKey.isEmpty() || smsApiKey.equals("YOUR_SMS_API_KEY") ||
                smsApiUrl == null || smsApiUrl.isEmpty() || smsApiUrl.equals("https://your-sms-api-endpoint.com/send")) {
                System.out.println("SMS notification skipped - API not configured. Message: " + message);
                return;
            }
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("apiKey", smsApiKey);
            requestBody.put("phoneNumber", phoneNumber);
            requestBody.put("message", message);
            
            restTemplate.postForEntity(smsApiUrl, requestBody, String.class);
            System.out.println("SMS notification sent successfully to " + phoneNumber);
        } catch (Exception e) {
            // Log the error but don't throw it to prevent disrupting the order flow
            System.err.println("Failed to send SMS notification: " + e.getMessage());
        }
    }
} 