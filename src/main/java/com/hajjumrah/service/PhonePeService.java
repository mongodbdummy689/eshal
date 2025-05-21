package com.hajjumrah.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PhonePeService {

    @Value("${phonepe.merchant.id}")
    private String merchantId;

    @Value("${phonepe.salt.key}")
    private String saltKey;

    @Value("${phonepe.salt.index}")
    private int saltIndex;

    @Value("${phonepe.api.url}")
    private String apiUrl;

    @Value("${phonepe.callback.url}")
    private String callbackUrl;

    private final RestTemplate restTemplate;

    public PhonePeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String createPayment(String orderId, double amount, String customerPhone) {
        try {
            System.out.println("PhonePeService.createPayment called with - OrderId: " + orderId + 
                ", Amount: " + amount + ", Phone: " + customerPhone);

            // Create payment request payload
            Map<String, Object> paymentInstrument = new HashMap<>();
            paymentInstrument.put("type", "PAY_PAGE");

            Map<String, Object> context = new HashMap<>();
            context.put("transactionId", orderId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("merchantId", merchantId);
            payload.put("merchantTransactionId", orderId);
            payload.put("merchantUserId", "MUID_" + orderId);
            payload.put("amount", (long)(amount * 100)); // Convert to paise
            payload.put("redirectUrl", callbackUrl.trim()); // Remove any whitespace
            payload.put("redirectMode", "POST");
            payload.put("callbackUrl", callbackUrl.trim()); // Remove any whitespace
            payload.put("mobileNumber", customerPhone);
            payload.put("paymentInstrument", paymentInstrument);
            payload.put("context", context);

            System.out.println("PhonePe Configuration - MerchantId: " + merchantId + 
                ", SaltKey: " + saltKey + ", SaltIndex: " + saltIndex + 
                ", ApiUrl: " + apiUrl + ", CallbackUrl: " + callbackUrl);

            // Convert payload to base64
            String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            String payloadBase64 = Base64.getEncoder().encodeToString(jsonPayload.getBytes(StandardCharsets.UTF_8));

            System.out.println("PhonePe Request Payload (JSON): " + jsonPayload);
            System.out.println("PhonePe Request Payload (Base64): " + payloadBase64);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String xVerify = generateXVerifyHeader(payloadBase64, "/pg/v1/pay");
            headers.set("X-VERIFY", xVerify);
            headers.set("X-MERCHANT-ID", merchantId);

            System.out.println("PhonePe Request Headers: " + headers);

            // Make API call
            String requestUrl = apiUrl + "/pg/v1/pay";
            System.out.println("Making PhonePe API call to: " + requestUrl);

            // Create request body with base64 encoded payload
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("request", payloadBase64);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            try {
                // First, make a POST request to initiate the payment
                ResponseEntity<Map> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
                );

                System.out.println("PhonePe API Response: " + response.getBody());

                if (response.getBody() != null && response.getBody().containsKey("success") && 
                    (Boolean)response.getBody().get("success")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                    if (data != null && data.containsKey("instrumentResponse")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> instrumentResponse = (Map<String, Object>) data.get("instrumentResponse");
                        if (instrumentResponse.containsKey("redirectInfo")) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> redirectInfo = (Map<String, Object>) instrumentResponse.get("redirectInfo");
                            if (redirectInfo.containsKey("url")) {
                                return (String) redirectInfo.get("url");
                            }
                        }
                    }
                }

                throw new RuntimeException("Failed to create payment: Invalid response format");

            } catch (org.springframework.web.client.HttpClientErrorException e) {
                System.err.println("PhonePe API Error Response: " + e.getResponseBodyAsString());
                throw new RuntimeException("PhonePe API Error: " + e.getResponseBodyAsString());
            }

        } catch (Exception e) {
            System.err.println("PhonePe Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating PhonePe payment: " + e.getMessage());
        }
    }

    public boolean verifyPayment(String orderId, String status, String transactionId) {
        try {
            System.out.println("=== PhonePe Payment Verification Started ===");
            System.out.println("Input parameters:");
            System.out.println("- OrderId: " + orderId);
            System.out.println("- Status: " + status);
            System.out.println("- TransactionId: " + transactionId);

            // Create verification request payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("merchantId", merchantId);
            payload.put("merchantTransactionId", orderId);
            payload.put("transactionId", transactionId);

            System.out.println("Configuration:");
            System.out.println("- MerchantId: " + merchantId);
            System.out.println("- SaltKey: " + saltKey);
            System.out.println("- SaltIndex: " + saltIndex);
            System.out.println("- ApiUrl: " + apiUrl);

            // Convert payload to JSON string first, then to base64
            String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            String payloadBase64 = Base64.getEncoder().encodeToString(jsonPayload.getBytes(StandardCharsets.UTF_8));

            System.out.println("Request details:");
            System.out.println("- JSON Payload: " + jsonPayload);
            System.out.println("- Base64 Payload: " + payloadBase64);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Fix the endpoint path for X-VERIFY header
            String apiEndpoint = "/pg/v1/status/" + merchantId + "/" + orderId;
            String xVerify = generateXVerifyHeader(payloadBase64, apiEndpoint);
            headers.set("X-VERIFY", xVerify);
            headers.set("X-MERCHANT-ID", merchantId);

            System.out.println("Request headers:");
            System.out.println("- Content-Type: " + headers.getContentType());
            System.out.println("- X-VERIFY: " + xVerify);
            System.out.println("- X-MERCHANT-ID: " + merchantId);

            // Make API call with correct endpoint
            String requestUrl = apiUrl + apiEndpoint;
            System.out.println("Making API call to: " + requestUrl);

            // For GET request, we don't need a request body
            HttpEntity<Void> request = new HttpEntity<>(headers);
            try {
                System.out.println("Sending request to PhonePe...");
                ResponseEntity<Map> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    request,
                    Map.class
                );

                System.out.println("Received response from PhonePe:");
                System.out.println("- Status code: " + response.getStatusCode());
                System.out.println("- Response body: " + response.getBody());

                if (response.getBody() != null && response.getBody().containsKey("success") && 
                    (Boolean)response.getBody().get("success")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                    if (data != null) {
                        String responseStatus = (String) data.get("state");
                        boolean isCompleted = "COMPLETED".equalsIgnoreCase(responseStatus);
                        System.out.println("Payment status: " + responseStatus + ", Is completed: " + isCompleted);
                        return isCompleted;
                    }
                }

                System.out.println("Payment verification failed - Invalid response format");
                return false;

            } catch (HttpClientErrorException e) {
                System.err.println("=== PhonePe API Error ===");
                System.err.println("Status code: " + e.getStatusCode());
                System.err.println("Response body: " + e.getResponseBodyAsString());
                throw new RuntimeException("PhonePe API Error: " + e.getResponseBodyAsString());
            }

        } catch (Exception e) {
            System.err.println("=== PhonePe Verification Error ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error verifying PhonePe payment: " + e.getMessage());
        } finally {
            System.out.println("=== PhonePe Payment Verification Completed ===");
        }
    }

    private String generateXVerifyHeader(String payloadBase64, String apiEndpoint) {
        try {
            // Create string to hash - Note the order: payload + endpoint + saltKey
            String stringToHash = payloadBase64 + apiEndpoint + saltKey;
            System.out.println("String to hash: " + stringToHash);

            // Generate SHA256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));

            // Convert hash to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String xVerify = hexString.toString() + "###" + saltIndex;
            System.out.println("Generated X-VERIFY: " + xVerify);
            return xVerify;
        } catch (Exception e) {
            throw new RuntimeException("Error generating X-VERIFY header: " + e.getMessage());
        }
    }
} 