package com.hajjumrah.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to initialize Razorpay client", e);
        }
    }

    public Map<String, Object> createOrder(double amount, String currency, String receipt) {
        try {
            // Debug logging
            System.out.println("[RazorpayDebug] ===== RAZORPAY ORDER CREATION =====");
            System.out.printf("[RazorpayDebug] Amount in Rupees: ₹%.2f\n", amount);
            System.out.printf("[RazorpayDebug] Amount in Paise: %d\n", (long)(amount * 100));
            System.out.printf("[RazorpayDebug] Currency: %s\n", currency);
            System.out.printf("[RazorpayDebug] Receipt: %s\n", receipt);
            System.out.println("[RazorpayDebug] ===== END DEBUG =====");
            
            JSONObject options = new JSONObject();
            options.put("amount", (long)(amount * 100)); // Convert to paise
            options.put("currency", currency);
            options.put("receipt", receipt);

            Order order = razorpayClient.orders.create(options);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("receipt", order.get("receipt"));
            response.put("keyId", razorpayKeyId);

            return response;
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        try {
            String data = orderId + "|" + paymentId;
            String generatedSignature = generateSignature(data, razorpayKeySecret);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify payment signature: " + e.getMessage(), e);
        }
    }

    private String generateSignature(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature);
    }

    public String getKeyId() {
        return razorpayKeyId;
    }
} 