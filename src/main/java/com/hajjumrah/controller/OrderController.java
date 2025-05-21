package com.hajjumrah.controller;

import com.hajjumrah.model.*;
import com.hajjumrah.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{orderId}")
    public Object viewOrder(@PathVariable String orderId,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false) String phone,
                          Authentication authentication,
                          Model model,
                          @RequestHeader(value = "Accept", required = false) String accept) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // If order has no userId, it's a guest order - allow access with email or phone verification
            if (order.getUserId() == null) {
                // Check if either email or phone matches
                boolean emailMatches = email != null && email.equals(order.getEmail());
                boolean phoneMatches = phone != null && phone.equals(order.getContactNumber());
                
                if (!emailMatches && !phoneMatches) {
                    if (accept != null && accept.contains("application/json")) {
                        return ResponseEntity.status(403).body(Map.of("error", "Access denied. Please provide valid email or phone number."));
                    }
                    return "error/403";
                }
            } else {
                // For authenticated users, verify ownership
                if (authentication == null || !authentication.isAuthenticated()) {
                    if (accept != null && accept.contains("application/json")) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
                    }
                    return "error/401";
                }

                User user = userRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (!order.getUserId().equals(user.getId())) {
                    if (accept != null && accept.contains("application/json")) {
                        return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
                    }
                    return "error/403";
                }
            }

            // If it's an API request, return JSON
            if (accept != null && accept.contains("application/json")) {
                return ResponseEntity.ok(order);
            }

            // For view requests, return the template
            model.addAttribute("orderId", orderId);
            model.addAttribute("order", order);
            return "order-confirmation";

        } catch (RuntimeException e) {
            if (accept != null && accept.contains("application/json")) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            model.addAttribute("error", e.getMessage());
            return "error/404";
        }
    }

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> orderDetails) {
        try {
            // Generate a unique order ID
            String orderId = java.util.UUID.randomUUID().toString();
            
            // Create new order
            Order order = new Order();
            order.setId(orderId);
            order.setUserId((String) orderDetails.get("userId"));
            order.setTotalAmount(Double.parseDouble(orderDetails.get("totalAmount").toString()));
            order.setStatus("PENDING");
            order.setOrderDate(LocalDateTime.now());
            order.setShippingAddress((String) orderDetails.get("shippingAddress"));
            order.setContactNumber((String) orderDetails.get("contactNumber"));
            order.setEmail((String) orderDetails.get("email"));
            order.setPaymentStatus("PENDING");
            order.setPaymentMethod((String) orderDetails.get("paymentMethod"));
            
            // Save order to database
            orderRepository.save(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getOrders(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
            }

            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());
            return ResponseEntity.ok(orders);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 