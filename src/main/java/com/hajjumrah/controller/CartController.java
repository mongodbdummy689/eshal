package com.hajjumrah.controller;

import com.hajjumrah.model.CartItem;
import com.hajjumrah.model.Product;
import com.hajjumrah.model.User;
import com.hajjumrah.repository.CartItemRepository;
import com.hajjumrah.repository.ProductRepository;
import com.hajjumrah.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private static final String GUEST_CART_KEY = "guestCart";

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItem cartItem, Authentication authentication, HttpSession session) {
        try {
            // Verify product exists before adding to cart
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // Set the price based on variant if available, otherwise use product price
            if (cartItem.getSelectedVariant() != null && cartItem.getVariantPrice() != null) {
                cartItem.setPrice(cartItem.getVariantPrice());
            } else if (cartItem.getPrice() == null) {
                // If no price is set, use the product's base price
                cartItem.setPrice(product.getPrice());
            }

            if (authentication != null && authentication.isAuthenticated()) {
                // Handle authenticated user
                User user = userRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                cartItem.setUserId(user.getId());
                cartItemRepository.save(cartItem);
            } else {
                // Handle guest user
                @SuppressWarnings("unchecked")
                List<CartItem> guestCart = (List<CartItem>) session.getAttribute(GUEST_CART_KEY);
                if (guestCart == null) {
                    guestCart = new ArrayList<>();
                }

                // Check if item already exists in cart (considering variants)
                Optional<CartItem> existingItem = guestCart.stream()
                        .filter(item -> {
                            // For products with variants, check both productId and selectedVariant
                            if (cartItem.getSelectedVariant() != null) {
                                return item.getProductId().equals(cartItem.getProductId()) && 
                                       cartItem.getSelectedVariant().equals(item.getSelectedVariant());
                            } else {
                                // For regular products, just check productId
                                return item.getProductId().equals(cartItem.getProductId());
                            }
                        })
                        .findFirst();

                if (existingItem.isPresent()) {
                    // Update quantity if item exists
                    existingItem.get().setQuantity(existingItem.get().getQuantity() + cartItem.getQuantity());
                    // Update price if variant price is provided
                    if (cartItem.getVariantPrice() != null) {
                        existingItem.get().setVariantPrice(cartItem.getVariantPrice());
                        existingItem.get().setPrice(cartItem.getVariantPrice());
                    } else if (cartItem.getPrice() != null) {
                        existingItem.get().setPrice(cartItem.getPrice());
                    }
                } else {
                    // Add new item if it doesn't exist
                    cartItem.setId(UUID.randomUUID().toString());
                    guestCart.add(cartItem);
                }

                session.setAttribute(GUEST_CART_KEY, guestCart);
            }
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getCart(Authentication authentication, HttpSession session) {
        try {
            List<CartItem> cartItems;
            if (authentication != null && authentication.isAuthenticated()) {
                // Get cart for authenticated user
                User user = userRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                cartItems = cartItemRepository.findByUserId(user.getId());
            } else {
                // Get cart for guest user
                @SuppressWarnings("unchecked")
                List<CartItem> guestCart = (List<CartItem>) session.getAttribute(GUEST_CART_KEY);
                cartItems = guestCart != null ? guestCart : new ArrayList<>();
            }

            // Enrich cart items with product details and ensure price is set
            cartItems = cartItems.stream()
                    .map(item -> {
                        Product product = productRepository.findById(item.getProductId()).orElse(null);
                        if (product != null) {
                            item.setProduct(product);
                            // Ensure price is set - use variant price if available, otherwise product price
                            if (item.getPrice() == null) {
                                if (item.getSelectedVariant() != null && item.getVariantPrice() != null) {
                                    item.setPrice(item.getVariantPrice());
                                } else {
                                    item.setPrice(product.getPrice());
                                }
                            }
                        }
                        return item;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(cartItems);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable String itemId, @RequestBody Map<String, Object> update, 
                                          Authentication authentication, HttpSession session) {
        try {
            Integer quantity = (Integer) update.get("quantity");
            Double price = update.get("price") != null ? ((Number) update.get("price")).doubleValue() : null;

            if (authentication != null && authentication.isAuthenticated()) {
                // Update for authenticated user
                CartItem cartItem = cartItemRepository.findById(itemId)
                        .orElseThrow(() -> new RuntimeException("Cart item not found"));
                cartItem.setQuantity(quantity);
                if (price != null) {
                    cartItem.setPrice(price);
                }
                cartItemRepository.save(cartItem);
            } else {
                // Update for guest user
                @SuppressWarnings("unchecked")
                List<CartItem> guestCart = (List<CartItem>) session.getAttribute(GUEST_CART_KEY);
                if (guestCart != null) {
                    guestCart.stream()
                            .filter(item -> item.getId().equals(itemId))
                            .findFirst()
                            .ifPresent(item -> {
                                item.setQuantity(quantity);
                                if (price != null) {
                                    item.setPrice(price);
                                }
                            });
                    session.setAttribute(GUEST_CART_KEY, guestCart);
                }
            }
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable String itemId, Authentication authentication, HttpSession session) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                // Remove for authenticated user
                cartItemRepository.deleteById(itemId);
            } else {
                // Remove for guest user
                @SuppressWarnings("unchecked")
                List<CartItem> guestCart = (List<CartItem>) session.getAttribute(GUEST_CART_KEY);
                if (guestCart != null) {
                    guestCart.removeIf(item -> item.getId().equals(itemId));
                    session.setAttribute(GUEST_CART_KEY, guestCart);
                }
            }
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/convert-guest-cart")
    public ResponseEntity<?> convertGuestCartToDatabaseCart(Authentication authentication, HttpSession session) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User must be authenticated to convert cart"));
            }

            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            @SuppressWarnings("unchecked")
            List<CartItem> guestCart = (List<CartItem>) session.getAttribute(GUEST_CART_KEY);
            
            if (guestCart != null && !guestCart.isEmpty()) {
                // Convert each guest cart item to a database cart item
                for (CartItem guestItem : guestCart) {
                    // Verify product still exists
                    Product product = productRepository.findById(guestItem.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found: " + guestItem.getProductId()));

                    // Create new cart item for database
                    CartItem dbCartItem = new CartItem();
                    dbCartItem.setUserId(user.getId());
                    dbCartItem.setProductId(guestItem.getProductId());
                    dbCartItem.setQuantity(guestItem.getQuantity());
                    dbCartItem.setPrice(guestItem.getPrice());
                    dbCartItem.setSelectedVariant(guestItem.getSelectedVariant());
                    dbCartItem.setVariantPrice(guestItem.getVariantPrice());
                    dbCartItem.setSource(guestItem.getSource());

                    // Check if item already exists in user's cart (considering variants)
                    Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(user.getId(), guestItem.getProductId());
                    
                    if (existingItem.isPresent()) {
                        // For products with variants, check if the same variant exists
                        CartItem existing = existingItem.get();
                        if (guestItem.getSelectedVariant() != null && 
                            guestItem.getSelectedVariant().equals(existing.getSelectedVariant())) {
                            // Same variant exists, update quantity
                            existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
                            cartItemRepository.save(existing);
                        } else if (guestItem.getSelectedVariant() == null && existing.getSelectedVariant() == null) {
                            // Same regular product exists, update quantity
                            existing.setQuantity(existing.getQuantity() + guestItem.getQuantity());
                            cartItemRepository.save(existing);
                        } else {
                            // Different variant or regular vs variant, save as new item
                            cartItemRepository.save(dbCartItem);
                        }
                    } else {
                        // Save new item if it doesn't exist
                        cartItemRepository.save(dbCartItem);
                    }
                }

                // Clear guest cart after successful conversion
                session.removeAttribute(GUEST_CART_KEY);
            }

            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication authentication, HttpSession session) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                // Clear cart for authenticated user
                User user = userRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                cartItemRepository.deleteByUserId(user.getId());
            } else {
                // Clear cart for guest user
                session.removeAttribute(GUEST_CART_KEY);
            }
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/shipping-estimate")
    public ResponseEntity<?> getShippingEstimate(@RequestBody Map<String, Object> requestBody) {
        try {
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) requestBody.get("cartItems");
            String state = (String) requestBody.getOrDefault("state", "");
            double rate = "Maharashtra".equalsIgnoreCase(state) ? 80.0 : 120.0;
            double totalShipping = 0.0;
            double totalWeight = 0.0;
            double totalLength = 0.0;
            double totalWidth = 0.0;
            double totalHeight = 0.0;

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
                Product product = productRepository.findById((String) itemMap.get("productId")).orElse(null);
                if (product == null) continue;
                int quantity = (int) itemMap.get("quantity");
                String source = (String) itemMap.getOrDefault("source", "");
                
                // Skip base shipping calculation for Tohfa-e-Khulus items if 8+ items
                if ("tohfa-e-khulus".equals(source) && tohfaKhulusCount >= 8) {
                    System.out.printf("[ShippingCalc] Tohfa-e-Khulus item (8+ items): Skipping individual base shipping for %s (qty: %d)\n", 
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
                    System.out.printf("[ShippingCalc] Tohfa-e-Khulus item (<8 items): Using null weight/dimensions (will get minimum 1 kg)\n");
                }
                
                // Calculate volumetric weight for all items (for logging purposes)
                double volumetricWeight = 0.0;
                if (length != null && width != null && height != null) {
                    volumetricWeight = (length * width * height) / 4500.0;
                    System.out.printf("[ShippingCalc] Volumetric calculation for %s: (%.1f x %.1f x %.1f) / 4500 = %.3f kg\n", 
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
                    System.out.printf("[ShippingCalc] Item %s contributes to total dimensions: max(L:%.1f), max(W:%.1f), add(H:%.1f x %d qty = %.1f)\n", 
                        product.getName(), length, width, height, quantity, height * quantity);
                }
                
                System.out.printf("[ShippingCalc] Product: %s, Qty: %d, LxWxH: %.1fx%.1fx%.1f, Actual Weight: %.3f, Volumetric Weight: %.3f, Used Weight: %.3f, Rate: %.2f, Base Shipping: %.2f\n",
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
                
                System.out.printf("[ShippingCalc] Tohfa-e-Khulus package (8+ items): Added package weight %.3f kg, shipping %.2f for %d items\n", 
                    tohfaKhulusWeight, tohfaKhulusShipping, tohfaKhulusCount);
            }
            
            // Calculate volumetric weight from total dimensions
            double totalVolumetricWeight = 0.0;
            if (totalLength > 0 && totalWidth > 0 && totalHeight > 0) {
                totalVolumetricWeight = (totalLength * totalWidth * totalHeight) / 4500.0;
                System.out.printf("[ShippingCalc] Final volumetric calculation: (%.1f x %.1f x %.1f) / 4500 = %.3f kg\n", 
                    totalLength, totalWidth, totalHeight, totalVolumetricWeight);
            }
            
            // Use the higher of actual weight or volumetric weight
            double finalWeight = Math.max(totalWeight, totalVolumetricWeight);
            System.out.printf("[ShippingCalc] Weight comparison: Actual Weight (%.3f kg) vs Volumetric Weight (%.3f kg) = Using %.3f kg\n", 
                totalWeight, totalVolumetricWeight, finalWeight);
            
            // Ensure minimum weight of 1 kg if final weight is 0 or very small
            if (finalWeight <= 0.001) {
                finalWeight = 1.0;
                System.out.printf("[ShippingCalc] Final weight was 0, applying minimum 1 kg\n");
            }
            
            // Calculate shipping based on ceiling weight
            int ceilingWeight = (int) Math.ceil(finalWeight);
            System.out.printf("[ShippingCalc] Ceiling calculation: ceil(%.3f) = %d kg\n", finalWeight, ceilingWeight);
            totalShipping = ceilingWeight * rate;
            System.out.printf("[ShippingCalc] Final shipping calculation: %d kg x ₹%.2f = ₹%.2f\n", ceilingWeight, rate, totalShipping);
            System.out.printf("[ShippingCalc] Total Weight: %.3f kg, Total Volumetric Weight: %.3f kg, Final Weight: %.3f kg, Ceiling Weight: %d kg, Shipping for %d kg: %.2f\n", 
                totalWeight, totalVolumetricWeight, finalWeight, ceilingWeight, ceilingWeight, totalShipping);
            
            double shippingAmount = Math.round(totalShipping * 100.0) / 100.0;
            Map<String, Object> result = new HashMap<>();
            result.put("shippingAmount", shippingAmount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 