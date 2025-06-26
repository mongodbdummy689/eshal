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
} 