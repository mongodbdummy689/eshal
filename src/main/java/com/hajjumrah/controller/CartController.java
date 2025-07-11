package com.hajjumrah.controller;

import com.hajjumrah.model.CartItem;
import com.hajjumrah.model.Product;
import com.hajjumrah.model.ProductVariant;
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
                // Calculate GST for variant using product's GST rate
                cartItem.setGstAmount((cartItem.getVariantPrice() * product.getGstRate()) / 100);
                cartItem.setVariantGstAmount((cartItem.getVariantPrice() * product.getGstRate()) / 100);
            } else if (cartItem.getPrice() == null) {
                // If no price is set, use the product's base price
                cartItem.setPrice(product.getPrice());
                // Calculate GST for product using product's GST rate
                cartItem.setGstAmount((product.getPrice() * product.getGstRate()) / 100);
            } else {
                // Calculate GST for provided price using product's GST rate
                cartItem.setGstAmount((cartItem.getPrice() * product.getGstRate()) / 100);
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
                        existingItem.get().setGstAmount((cartItem.getVariantPrice() * product.getGstRate()) / 100);
                        existingItem.get().setVariantGstAmount((cartItem.getVariantPrice() * product.getGstRate()) / 100);
                    } else if (cartItem.getPrice() != null) {
                        existingItem.get().setPrice(cartItem.getPrice());
                        existingItem.get().setGstAmount((cartItem.getPrice() * product.getGstRate()) / 100);
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
                            // Calculate GST if not already set
                            if (item.getGstAmount() == null) {
                                if (item.getSelectedVariant() != null && item.getVariantPrice() != null) {
                                    item.setGstAmount((item.getVariantPrice() * product.getGstRate()) / 100);
                                    item.setVariantGstAmount((item.getVariantPrice() * product.getGstRate()) / 100);
                                } else {
                                    item.setGstAmount((item.getPrice() * product.getGstRate()) / 100);
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
            System.out.println("[ShippingCalc] ===== SHIPPING CALCULATION START =====");
            System.out.println("[ShippingCalc] Request body: " + requestBody);
            
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) requestBody.get("cartItems");
            String state = (String) requestBody.getOrDefault("state", "");
            System.out.printf("[ShippingCalc] Received state: '%s'\n", state);
            double rate = "Maharashtra".equalsIgnoreCase(state) ? 80.0 : 120.0;
            System.out.printf("[ShippingCalc] Using rate: ₹%.2f\n", rate);
            System.out.printf("[ShippingCalc] Cart items count: %d\n", cartItems.size());
            double totalFinalWeight = 0.0;

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
                Product product = productRepository.findById((String) itemMap.get("productId")).orElse(null);
                if (product == null) continue;
                int quantity = (int) itemMap.get("quantity");
                String source = (String) itemMap.getOrDefault("source", "");
                String selectedVariant = (String) itemMap.getOrDefault("selectedVariant", null);
                
                // Skip individual calculation for Tohfa-e-Khulus items if 8+ items
                if ("tohfa-e-khulus".equals(source) && tohfaKhulusCount >= 8) {
                    System.out.printf("[ShippingCalc] Tohfa-e-Khulus item (8+ items): Skipping individual calculation for %s (qty: %d)\n", 
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
                    System.out.printf("[ShippingCalc] Tohfa-e-Khulus item (<8 items): Using null weight/dimensions (will get minimum 1 kg)\n");
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
                System.out.printf("[ShippingCalc] %s%s - Weight: %.3f, Dimensions: %.1fx%.1fx%.1f / 4500 = %.3f, Max(%.3f, %.3f) = %d * %.3f = %.3f kg\n",
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
                System.out.printf("[ShippingCalc] Tohfa-e-Khulus package (8+ items): Added package weight %.3f kg for %d items\n", 
                    tohfaKhulusWeight, tohfaKhulusCount);
            }
            
            // Ensure minimum weight of 1 kg if total final weight is 0 or very small
            if (totalFinalWeight <= 0.001) {
                totalFinalWeight = 1.0;
                System.out.printf("[ShippingCalc] Total final weight was 0, applying minimum 1 kg\n");
            }
            
            // Apply ceiling only once to the total final weight
            int ceilingWeight = (int) Math.ceil(totalFinalWeight);
            
            // Calculate shipping based on ceiling weight
            double totalShipping = ceilingWeight * rate;
            
            // Print final calculation
            System.out.printf("[ShippingCalc] Total Final Weight: %.3f kg\n", totalFinalWeight);
            System.out.printf("[ShippingCalc] Ceiling calculation: ceil(%.3f) = %d kg\n", totalFinalWeight, ceilingWeight);
            System.out.printf("[ShippingCalc] Final shipping calculation: %d kg x ₹%.2f = ₹%.2f\n", ceilingWeight, rate, totalShipping);
            
            double shippingAmount = Math.round(totalShipping * 100.0) / 100.0;
            System.out.printf("[ShippingCalc] Final shipping amount returned: ₹%.2f\n", shippingAmount);
            System.out.println("[ShippingCalc] ===== SHIPPING CALCULATION END =====");
            Map<String, Object> result = new HashMap<>();
            result.put("shippingAmount", shippingAmount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 