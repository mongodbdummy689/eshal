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
            System.out.println("[OrderDebug] ===== ORDER PLACEMENT START =====");
            System.out.println("[OrderDebug] Request body: " + requestBody);
            
            Map<String, String> customerDetails = (Map<String, String>) requestBody.get("customerDetails");
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) requestBody.get("cartItems");
            String paymentMethod = (String) requestBody.get("paymentMethod");
            
            System.out.printf("[OrderDebug] Customer State: %s\n", customerDetails.getOrDefault("state", "NOT_PROVIDED"));
            System.out.printf("[OrderDebug] Payment Method: %s\n", paymentMethod);
            System.out.printf("[OrderDebug] Cart Items Count: %d\n", cartItems.size());

            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal subtotalAmount = BigDecimal.ZERO;
            BigDecimal totalGstAmount = BigDecimal.ZERO;

            for (Map<String, Object> itemMap : cartItems) {
                Product product = productRepository.findById((String) itemMap.get("productId"))
                        .orElseThrow(() -> new RuntimeException("Product not found for ID: " + itemMap.get("productId")));

                BigDecimal totalPrice = new BigDecimal(itemMap.get("price").toString());
                int quantity = (int) itemMap.get("quantity");
                String source = (String) itemMap.get("source"); // Get source from cart item
                
                // Calculate GST for this item using product's specific rate
                BigDecimal gstRate = BigDecimal.valueOf(product.getGstRate());
                BigDecimal itemGstAmount = totalPrice.multiply(gstRate).divide(BigDecimal.valueOf(100));
                totalGstAmount = totalGstAmount.add(itemGstAmount);
                
                OrderItem orderItem = new OrderItem(
                    product.getId(), 
                    product.getName(), 
                    product.getImageUrl(), 
                    (String) itemMap.get("selectedVariant"), 
                    totalPrice,
                    quantity,
                    source
                );
                orderItem.setGstAmount(itemGstAmount);
                orderItems.add(orderItem);
                subtotalAmount = subtotalAmount.add(totalPrice);
            }
            
            // Calculate shipping cost using actual weight and state
            System.out.println("[OrderShippingCalc] ===== SHIPPING CALCULATION START =====");
            double totalFinalWeight = 0.0;
            String state = customerDetails.getOrDefault("state", "");
            System.out.printf("[OrderShippingCalc] Received state: '%s'\n", state);
            double rate = (state.equalsIgnoreCase("Maharashtra")) ? 80.0 : 120.0;
            System.out.printf("[OrderShippingCalc] Using rate: ₹%.2f\n", rate);
            
            // Count Tohfa-e-Khulus items
            int tohfaKhulusCount = 0;
            for (Map<String, Object> itemMap : cartItems) {
                String source = (String) itemMap.getOrDefault("source", "");
                if ("tohfa-e-khulus".equals(source)) {
                    tohfaKhulusCount += (int) itemMap.get("quantity");
                }
            }
            
            // Process each item and calculate individual final weights
            for (Map<String, Object> itemMap : cartItems) {
                Product product = productRepository.findById((String) itemMap.get("productId"))
                        .orElseThrow(() -> new RuntimeException("Product not found for ID: " + itemMap.get("productId")));
                int quantity = (int) itemMap.get("quantity");
                String source = (String) itemMap.getOrDefault("source", "");
                String selectedVariant = (String) itemMap.getOrDefault("selectedVariant", null);
                
                // Skip individual calculation for Tohfa-e-Khulus items if 8+ items
                if ("tohfa-e-khulus".equals(source) && tohfaKhulusCount >= 8) {
                    System.out.printf("[OrderShippingCalc] Tohfa-e-Khulus item (8+ items): Skipping individual calculation for %s (qty: %d)\n", 
                        product.getName(), quantity);
                    continue;
                }
                
                Double length = product.getLength();
                Double width = product.getWidth();
                Double height = product.getHeight();
                Double actualWeight = product.getWeight();
                
                // Handle variant-specific weight and dimensions
                if (selectedVariant != null && product.getVariants() != null) {
                    for (ProductVariant variant : product.getVariants()) {
                        if (selectedVariant.equals(variant.getSize())) {
                            // Use variant-specific weight and dimensions
                            if (variant.getWeight() != null) {
                                actualWeight = variant.getWeight();
                            }
                            if (variant.getLength() != null) {
                                length = variant.getLength();
                            }
                            if (variant.getWidth() != null) {
                                width = variant.getWidth();
                            }
                            if (variant.getHeight() != null) {
                                height = variant.getHeight();
                            }
                            break;
                        }
                    }
                }
                
                // Special handling for Tohfa-e-Khulus items (less than 8 items)
                if ("tohfa-e-khulus".equals(source) && tohfaKhulusCount < 8) {
                    // Less than 8 items: use null weight/dimensions (will get minimum 1 kg)
                    actualWeight = null;
                    length = null;
                    width = null;
                    height = null;
                    System.out.printf("[OrderShippingCalc] Tohfa-e-Khulus item (<8 items): Using null weight/dimensions (will get minimum 1 kg)\n");
                }
                
                // Calculate volumetric weight for this item
                double volumetricWeight = 0.0;
                if (length != null && width != null && height != null) {
                    volumetricWeight = (length * width * height) / 4500.0;
                }
                
                // Use actual weight if available, otherwise use volumetric weight
                double usedWeight;
                if (actualWeight != null && actualWeight > 0) {
                    usedWeight = actualWeight;
                } else {
                    usedWeight = volumetricWeight;
                }
                
                // Calculate final weight for this item (max of actual and volumetric)
                double itemFinalWeight = Math.max(usedWeight, volumetricWeight) * quantity;
                
                // Log the calculation for this item
                String variantInfo = selectedVariant != null ? " (" + selectedVariant + ")" : "";
                double maxWeight = Math.max(usedWeight, volumetricWeight);
                System.out.printf("[OrderShippingCalc] %s%s - Weight: %.3f, Dimensions: %.1fx%.1fx%.1f / 4500 = %.3f, Max(%.3f, %.3f) = %d * %.3f = %.3f kg\n",
                    product.getName(), variantInfo,
                    actualWeight != null ? actualWeight : 0.0,
                    length != null ? length : 0.0, width != null ? width : 0.0, height != null ? height : 0.0,
                    volumetricWeight,
                    usedWeight, volumetricWeight, quantity, maxWeight, itemFinalWeight);
                
                totalFinalWeight += itemFinalWeight;
            }
            
            // Add Tohfa-e-Khulus package weight if 8+ items
            if (tohfaKhulusCount >= 8) {
                double tohfaKhulusWeight = 0.800; // Specific Tohfa-e-Khulus package weight
                totalFinalWeight += tohfaKhulusWeight;
                System.out.printf("[OrderShippingCalc] Tohfa-e-Khulus package (8+ items): Added package weight %.3f kg for %d items\n", 
                    tohfaKhulusWeight, tohfaKhulusCount);
            }
            
            // Ensure minimum weight of 1 kg if total final weight is 0 or very small
            if (totalFinalWeight <= 0.001) {
                totalFinalWeight = 1.0;
                System.out.printf("[OrderShippingCalc] Total final weight was 0, applying minimum 1 kg\n");
            }
            
            // Apply ceiling only once to the total final weight
            int ceilingWeight = (int) Math.ceil(totalFinalWeight);
            
            // Calculate shipping based on ceiling weight
            double totalShipping = ceilingWeight * rate;
            
            // Print final calculation
            System.out.printf("[OrderShippingCalc] Total Final Weight: %.3f kg\n", totalFinalWeight);
            System.out.printf("[OrderShippingCalc] Ceiling calculation: ceil(%.3f) = %d kg\n", totalFinalWeight, ceilingWeight);
            System.out.printf("[OrderShippingCalc] Final shipping calculation: %d kg x ₹%.2f = ₹%.2f\n", ceilingWeight, rate, totalShipping);
            
            BigDecimal shippingAmount = BigDecimal.valueOf(totalShipping).setScale(2, BigDecimal.ROUND_HALF_UP);
            System.out.println("[OrderShippingCalc] ===== SHIPPING CALCULATION END =====");
            BigDecimal totalAmount = subtotalAmount.add(totalGstAmount).add(shippingAmount);
            
            // Debug logging
            System.out.println("[OrderDebug] ===== ORDER CREATION DEBUG =====");
            System.out.printf("[OrderDebug] Subtotal: ₹%.2f\n", subtotalAmount.doubleValue());
            System.out.printf("[OrderDebug] GST: ₹%.2f\n", totalGstAmount.doubleValue());
            System.out.printf("[OrderDebug] Shipping: ₹%.2f\n", shippingAmount.doubleValue());
            System.out.printf("[OrderDebug] Total Amount: ₹%.2f\n", totalAmount.doubleValue());
            System.out.printf("[OrderDebug] Customer State: %s\n", customerDetails.getOrDefault("state", "NOT_PROVIDED"));
            System.out.println("[OrderDebug] ===== END DEBUG =====");
            
            Order order = new Order();
            
            // Generate unique 6-digit order ID
            String generatedOrderId = orderIdGenerator.generateOrderId();
            order.setOrderId(generatedOrderId);
            
            order.setUserId(customerDetails.get("userId"));
            order.setItems(orderItems);
            order.setSubtotalAmount(subtotalAmount.doubleValue());
            order.setGstAmount(totalGstAmount.doubleValue());
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
            
            System.out.println("[OrderDebug] ===== ORDER PLACEMENT END =====");
            System.out.printf("[OrderDebug] Order saved with ID: %s\n", savedOrder.getOrderId());
            System.out.printf("[OrderDebug] Final order total: ₹%.2f\n", savedOrder.getTotalAmount());
            
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
            
            // Clear the user's cart after successful order placement
            String userId = customerDetails.get("userId");
            if (userId != null && !userId.trim().isEmpty()) {
                try {
                    cartItemRepository.deleteByUserId(userId);
                    System.out.println("Cart cleared for user: " + userId);
                } catch (Exception e) {
                    // Log but don't fail the order placement if cart clearing fails
                    System.err.println("Failed to clear cart for user " + userId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("No userId provided, skipping cart clearing");
            }

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