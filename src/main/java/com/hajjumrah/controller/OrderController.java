package com.hajjumrah.controller;

import com.hajjumrah.model.*;
import com.hajjumrah.repository.*;
import com.hajjumrah.service.OrderIdGenerator;
import com.hajjumrah.service.EmailService;
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
import java.math.BigDecimal;

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

    @Autowired
    private OrderIdGenerator orderIdGenerator;

    @Autowired
    private EmailService emailService;

    @GetMapping("/{orderId}")
    public Object viewOrder(@PathVariable String orderId,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false) String phone,
                          Authentication authentication,
                          Model model,
                          @RequestHeader(value = "Accept", required = false) String accept) {
        try {
            // First try to find by the 6-digit orderId
            Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
            Order order;
            
            if (orderOpt.isPresent()) {
                order = orderOpt.get();
            } else {
                // Fallback to MongoDB ObjectId for backward compatibility
                order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found"));
            }

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
            model.addAttribute("orderId", order.getOrderId() != null ? order.getOrderId() : order.getId());
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
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> requestBody) {
        try {
            Map<String, String> customerDetails = (Map<String, String>) requestBody.get("customerDetails");
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) requestBody.get("cartItems");
            String paymentMethod = (String) requestBody.get("paymentMethod");

            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal subtotalAmount = BigDecimal.ZERO;

            for (Map<String, Object> itemMap : cartItems) {
                Product product = productRepository.findById((String) itemMap.get("productId"))
                        .orElseThrow(() -> new RuntimeException("Product not found for ID: " + itemMap.get("productId")));

                BigDecimal totalPrice = new BigDecimal(itemMap.get("price").toString());
                int quantity = (int) itemMap.get("quantity");
                String source = (String) itemMap.get("source"); // Get source from cart item
                
                OrderItem orderItem = new OrderItem(
                    product.getId(), 
                    product.getName(), 
                    product.getImageUrl(), 
                    (String) itemMap.get("selectedVariant"), 
                    totalPrice,
                    quantity,
                    source
                );
                orderItems.add(orderItem);
                subtotalAmount = subtotalAmount.add(totalPrice);
            }
            
            // Calculate shipping cost using actual weight and state
            double totalShipping = 0.0;
            double totalWeight = 0.0;
            double totalLength = 0.0;
            double totalWidth = 0.0;
            double totalHeight = 0.0;
            String state = customerDetails.getOrDefault("state", "");
            double rate = (state.equalsIgnoreCase("Maharashtra")) ? 80.0 : 120.0;
            
            // Count Tohfa-e-Khulus items
            int tohfaKhulusCount = 0;
            for (Map<String, Object> itemMap : cartItems) {
                String source = (String) itemMap.getOrDefault("source", "");
                if ("tohfa-e-khulus".equals(source)) {
                    tohfaKhulusCount += (int) itemMap.get("quantity");
                }
            }
            
            // First pass: calculate base shipping and collect total dimensions
            for (Map<String, Object> itemMap : cartItems) {
                Product product = productRepository.findById((String) itemMap.get("productId"))
                        .orElseThrow(() -> new RuntimeException("Product not found for ID: " + itemMap.get("productId")));
                int quantity = (int) itemMap.get("quantity");
                String source = (String) itemMap.getOrDefault("source", "");
                
                // Skip base shipping calculation for Tohfa-e-Khulus items if 8+ items
                if ("tohfa-e-khulus".equals(source) && tohfaKhulusCount >= 8) {
                    System.out.printf("[OrderShippingCalc] Tohfa-e-Khulus item (8+ items): Skipping individual base shipping for %s (qty: %d)\n", 
                        product.getName(), quantity);
                    continue;
                }
                
                Double length = product.getLength();
                Double width = product.getWidth();
                Double height = product.getHeight();
                Double actualWeight = product.getWeight();
                
                // Special handling for Tohfa-e-Khulus items (less than 8 items)
                if ("tohfa-e-khulus".equals(source) && tohfaKhulusCount < 8) {
                    // Less than 8 items: use null weight/dimensions (will get minimum 1 kg)
                    actualWeight = null;
                    length = null;
                    width = null;
                    height = null;
                    System.out.printf("[OrderShippingCalc] Tohfa-e-Khulus item (<8 items): Using null weight/dimensions (will get minimum 1 kg)\n");
                }
                
                // Calculate volumetric weight for all items (for logging purposes)
                double volumetricWeight = 0.0;
                if (length != null && width != null && height != null) {
                    volumetricWeight = (length * width * height) / 4500.0;
                    System.out.printf("[OrderShippingCalc] Volumetric calculation for %s: (%.1f x %.1f x %.1f) / 4500 = %.3f kg\n", 
                        product.getName(), length, width, height, volumetricWeight);
                }
                
                // Use actual weight if available, otherwise use volumetric weight
                double usedWeight;
                if (actualWeight != null && actualWeight > 0) {
                    usedWeight = actualWeight;
                } else {
                    usedWeight = volumetricWeight;
                }
                
                // Calculate base shipping cost for this item
                double baseShipping = usedWeight * rate * quantity;
                totalShipping += baseShipping;
                totalWeight += usedWeight * quantity;
                
                // Accumulate dimensions (assuming items are stacked/arranged)
                if (length != null && width != null && height != null) {
                    totalLength = Math.max(totalLength, length);
                    totalWidth = Math.max(totalWidth, width);
                    totalHeight += height * quantity;
                    System.out.printf("[OrderShippingCalc] Item %s contributes to total dimensions: max(L:%.1f), max(W:%.1f), add(H:%.1f x %d qty = %.1f)\n", 
                        product.getName(), length, width, height, quantity, height * quantity);
                }
                
                System.out.printf("[OrderShippingCalc] Product: %s, Qty: %d, LxWxH: %.1fx%.1fx%.1f, Actual Weight: %.3f, Volumetric Weight: %.3f, Used Weight: %.3f, Rate: %.2f, Base Shipping: %.2f\n",
                    product.getName(), quantity,
                    length != null ? length : 0.0, width != null ? width : 0.0, height != null ? height : 0.0,
                    actualWeight != null ? actualWeight : 0.0, volumetricWeight, usedWeight, rate, baseShipping);
            }
            
            // Add Tohfa-e-Khulus package shipping if 8+ items
            if (tohfaKhulusCount >= 8) {
                double tohfaKhulusWeight = 0.800; // Specific Tohfa-e-Khulus package weight
                double tohfaKhulusShipping = tohfaKhulusWeight * rate;
                totalShipping += tohfaKhulusShipping;
                totalWeight += tohfaKhulusWeight;
                
                // Add Tohfa-e-Khulus dimensions to total
                totalLength = Math.max(totalLength, 20.0);
                totalWidth = Math.max(totalWidth, 36.0);
                totalHeight += 10.0;
                
                System.out.printf("[OrderShippingCalc] Tohfa-e-Khulus package (8+ items): Added package weight %.3f kg, shipping %.2f for %d items\n", 
                    tohfaKhulusWeight, tohfaKhulusShipping, tohfaKhulusCount);
            }
            
            // Calculate volumetric weight from total dimensions
            double totalVolumetricWeight = 0.0;
            if (totalLength > 0 && totalWidth > 0 && totalHeight > 0) {
                totalVolumetricWeight = (totalLength * totalWidth * totalHeight) / 4500.0;
                System.out.printf("[OrderShippingCalc] Final volumetric calculation: (%.1f x %.1f x %.1f) / 4500 = %.3f kg\n", 
                    totalLength, totalWidth, totalHeight, totalVolumetricWeight);
            }
            
            // Use the higher of actual weight or volumetric weight
            double finalWeight = Math.max(totalWeight, totalVolumetricWeight);
            System.out.printf("[OrderShippingCalc] Weight comparison: Actual Weight (%.3f kg) vs Volumetric Weight (%.3f kg) = Using %.3f kg\n", 
                totalWeight, totalVolumetricWeight, finalWeight);
            
            // Ensure minimum weight of 1 kg if final weight is 0 or very small
            if (finalWeight <= 0.001) {
                finalWeight = 1.0;
                System.out.printf("[OrderShippingCalc] Final weight was 0, applying minimum 1 kg\n");
            }
            
            // Calculate shipping based on ceiling weight
            int ceilingWeight = (int) Math.ceil(finalWeight);
            System.out.printf("[OrderShippingCalc] Ceiling calculation: ceil(%.3f) = %d kg\n", finalWeight, ceilingWeight);
            totalShipping = ceilingWeight * rate;
            System.out.printf("[OrderShippingCalc] Final shipping calculation: %d kg x ₹%.2f = ₹%.2f\n", ceilingWeight, rate, totalShipping);
            System.out.printf("[OrderShippingCalc] Total Weight: %.3f kg, Total Volumetric Weight: %.3f kg, Final Weight: %.3f kg, Ceiling Weight: %d kg, Shipping for %d kg: %.2f\n", 
                totalWeight, totalVolumetricWeight, finalWeight, ceilingWeight, ceilingWeight, totalShipping);
            
            BigDecimal shippingAmount = BigDecimal.valueOf(totalShipping).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalAmount = subtotalAmount.add(shippingAmount);
            
            Order order = new Order();
            
            // Generate unique 6-digit order ID
            String generatedOrderId = orderIdGenerator.generateOrderId();
            order.setOrderId(generatedOrderId);
            
            order.setUserId(customerDetails.get("userId"));
            order.setItems(orderItems);
            order.setSubtotalAmount(subtotalAmount.doubleValue());
            order.setShippingAmount(shippingAmount.doubleValue());
            order.setTotalAmount(totalAmount.doubleValue());
            order.setStatus("CONFIRMED");
            order.setOrderDate(LocalDateTime.now());
            
            // Set enhanced address fields
            order.setFullName(customerDetails.get("fullName"));
            order.setFlatNo(customerDetails.get("flatNo"));
            order.setApartmentName(customerDetails.get("apartmentName"));
            order.setFloor(customerDetails.get("floor"));
            order.setStreetName(customerDetails.get("streetName"));
            order.setNearbyLandmark(customerDetails.get("nearbyLandmark"));
            order.setCity(customerDetails.get("city"));
            order.setPincode(customerDetails.get("pincode"));
            order.setState(customerDetails.get("state"));
            order.setCountry(customerDetails.get("country"));
            
            // Set legacy fields for backward compatibility
            order.setShippingAddress(customerDetails.get("shippingAddress"));
            order.setContactNumber(customerDetails.get("contactNumber"));
            order.setEmail(customerDetails.get("email"));
            order.setPaymentStatus("PAID");
            order.setPaymentMethod(paymentMethod);
            
            // Set transaction ID if available from payment details
            if (requestBody.containsKey("paymentDetails")) {
                Map<String, Object> paymentDetails = (Map<String, Object>) requestBody.get("paymentDetails");
                if (paymentDetails.containsKey("razorpay_payment_id")) {
                    order.setTransactionId((String) paymentDetails.get("razorpay_payment_id"));
                }
            }
            
            Order savedOrder = orderRepository.save(order);
            
            // Send email notification
            try {
                System.out.println("Attempting to send order confirmation email for order: " + savedOrder.getOrderId());
                System.out.println("Customer email: " + savedOrder.getEmail());
                emailService.sendOrderConfirmationEmail(savedOrder);
                System.out.println("Email service call completed successfully");
            } catch (Exception e) {
                // Log but don't fail the order placement if email fails
                System.err.println("Failed to send order confirmation email: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Optional: Clear the user's cart after successful order placement
            // cartItemRepository.deleteAllByUser(....);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", savedOrder.getOrderId()); // Return the 6-digit orderId
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace(); 
            return ResponseEntity.badRequest().body(Map.of("error", "Error placing order: " + e.getMessage()));
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

    @GetMapping("/test-email")
    public ResponseEntity<?> testEmail() {
        try {
            System.out.println("Testing email service...");
            
            // Create a test order
            Order testOrder = new Order();
            testOrder.setOrderId("TEST123");
            testOrder.setEmail("mongodbdummy689@gmail.com"); // Use the configured email
            testOrder.setFullName("Test Customer");
            testOrder.setTotalAmount(100.0);
            testOrder.setSubtotalAmount(90.0);
            testOrder.setShippingAmount(10.0);
            testOrder.setContactNumber("1234567890");
            testOrder.setFlatNo("123");
            testOrder.setApartmentName("Test Building");
            testOrder.setFloor("1st");
            testOrder.setStreetName("Test Street");
            testOrder.setNearbyLandmark("Test Landmark");
            testOrder.setCity("Test City");
            testOrder.setState("Test State");
            testOrder.setCountry("Test Country");
            testOrder.setPincode("123456");
            
            // Send test email
            emailService.sendOrderConfirmationEmail(testOrder);
            
            return ResponseEntity.ok(Map.of("message", "Test email sent successfully"));
        } catch (Exception e) {
            System.err.println("Test email failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Test email failed: " + e.getMessage()));
        }
    }
} 