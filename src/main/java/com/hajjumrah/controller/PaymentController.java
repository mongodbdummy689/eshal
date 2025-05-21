package com.hajjumrah.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.hajjumrah.service.NotificationService;
import com.hajjumrah.service.PhonePeService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private final NotificationService notificationService;
    private final PhonePeService phonePeService;

    public PaymentController(NotificationService notificationService, PhonePeService phonePeService) {
        this.notificationService = notificationService;
        this.phonePeService = phonePeService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject options = new JSONObject();
            options.put("amount", data.get("amount"));
            options.put("currency", "INR");
            options.put("receipt", "receipt_" + System.currentTimeMillis());

            Order order = razorpay.orders.create(options);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));

            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> data) {
        try {
            String orderId = data.get("razorpay_order_id");
            String paymentId = data.get("razorpay_payment_id");
            String signature = data.get("razorpay_signature");

            // Verify signature
            String generatedSignature = generateSignature(orderId + "|" + paymentId, razorpayKeySecret);
            
            if (generatedSignature.equals(signature)) {
                // Send SMS notification after successful payment verification
                notificationService.sendSMS("8369737670", "Your payment has been confirmed for order: " + orderId);
                return ResponseEntity.ok(Map.of("status", "success"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid signature"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String generateSignature(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature);
    }

    @PostMapping("/phonepe/initiate")
    public ResponseEntity<?> initiatePhonePePayment(@RequestBody Map<String, Object> paymentRequest) {
        try {
            System.out.println("Received PhonePe payment request: " + paymentRequest);
            
            String orderId = (String) paymentRequest.get("orderId");
            Double amount = Double.parseDouble(paymentRequest.get("amount").toString());
            String customerPhone = (String) paymentRequest.get("customerPhone");

            System.out.println("Parsed values - OrderId: " + orderId + ", Amount: " + amount + ", Phone: " + customerPhone);

            if (orderId == null || amount == null || customerPhone == null) {
                System.out.println("Missing required parameters - OrderId: " + (orderId == null) + 
                    ", Amount: " + (amount == null) + ", Phone: " + (customerPhone == null));
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "400",
                    "message", "Missing required parameters"
                ));
            }

            // Validate phone number format
            if (!customerPhone.matches("\\d{10}")) {
                System.out.println("Invalid phone number format: " + customerPhone);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "400",
                    "message", "Invalid phone number format"
                ));
            }

            // Validate amount
            if (amount <= 0) {
                System.out.println("Invalid amount: " + amount);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "400",
                    "message", "Invalid amount"
                ));
            }

            System.out.println("Initiating PhonePe payment with - OrderId: " + orderId + 
                ", Amount: " + amount + ", Phone: " + customerPhone);
            
            try {
                String paymentUrl = phonePeService.createPayment(orderId, amount, customerPhone);
                System.out.println("Received payment URL from PhonePe: " + paymentUrl);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "paymentUrl", paymentUrl
                ));
            } catch (RuntimeException e) {
                System.err.println("PhonePe payment initiation failed: " + e.getMessage());
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", "400",
                    "message", e.getMessage()
                ));
            }
        } catch (Exception e) {
            System.err.println("PhonePe Initiation Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "code", "400",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/phonepe/callback")
    public ResponseEntity<?> handlePhonePeCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            String orderId = (String) callbackData.get("orderId");
            String status = (String) callbackData.get("status");
            String transactionId = (String) callbackData.get("transactionId");

            if (orderId == null || status == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required parameters"));
            }

            boolean success = phonePeService.verifyPayment(orderId, status, transactionId);
            if (success) {
                return ResponseEntity.ok(Map.of("status", "success"));
            } else {
                return ResponseEntity.ok(Map.of("status", "failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 