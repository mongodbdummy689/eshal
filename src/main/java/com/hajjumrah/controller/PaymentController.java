package com.hajjumrah.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import com.hajjumrah.service.NotificationService;
import com.hajjumrah.service.RazorpayService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final NotificationService notificationService;
    private final RazorpayService razorpayService;

    public PaymentController(NotificationService notificationService, RazorpayService razorpayService) {
        this.notificationService = notificationService;
        this.razorpayService = razorpayService;
    }

    @PostMapping("/razorpay/create-order")
    public ResponseEntity<?> createRazorpayOrder(@RequestBody Map<String, Object> data) {
        try {
            System.out.println("Creating Razorpay order with data: " + data);
            
            Double amount = Double.parseDouble(data.get("amount").toString());
            String currency = (String) data.getOrDefault("currency", "INR");
            String receipt = "receipt_" + System.currentTimeMillis();

            if (amount <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid amount"
                ));
            }

            Map<String, Object> orderResponse = razorpayService.createOrder(amount, currency, receipt);
            
            System.out.println("Razorpay order created successfully: " + orderResponse);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", orderResponse
            ));
        } catch (Exception e) {
            System.err.println("Error creating Razorpay order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to create order: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<?> verifyRazorpayPayment(@RequestBody Map<String, String> data) {
        try {
            System.out.println("Verifying Razorpay payment with data: " + data);
            
            String orderId = data.get("razorpay_order_id");
            String paymentId = data.get("razorpay_payment_id");
            String signature = data.get("razorpay_signature");

            if (orderId == null || paymentId == null || signature == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing required parameters"
                ));
            }

            boolean isValid = razorpayService.verifyPayment(orderId, paymentId, signature);
            
            if (isValid) {
                System.out.println("Razorpay payment verified successfully for order: " + orderId);
                // Send SMS notification after successful payment verification
                notificationService.sendSMS("8369737670", "Your payment has been confirmed for order: " + orderId);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment verified successfully"
                ));
            } else {
                System.err.println("Razorpay payment verification failed for order: " + orderId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid signature"
                ));
            }
        } catch (Exception e) {
            System.err.println("Error verifying Razorpay payment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to verify payment: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/razorpay/config")
    public ResponseEntity<?> getRazorpayConfig() {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "keyId", razorpayService.getKeyId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Failed to get configuration"
            ));
        }
    }
} 