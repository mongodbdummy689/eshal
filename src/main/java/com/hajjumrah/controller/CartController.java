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

import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItem cartItem, Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Verify product exists before adding to cart
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            cartItem.setUserId(user.getId());
            cartItemRepository.save(cartItem);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCart(Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
            
            List<Map<String, Object>> cartItemsWithDetails = cartItems.stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("price", item.getPrice());
                    itemMap.put("productId", item.getProductId());
                    
                    // Get product details
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    if (product != null) {
                        Map<String, Object> productDetails = new HashMap<>();
                        productDetails.put("id", product.getId());
                        productDetails.put("name", product.getName());
                        productDetails.put("description", product.getDescription());
                        productDetails.put("imageUrl", product.getImageUrl());
                        productDetails.put("category", product.getCategory());
                        itemMap.put("product", productDetails);
                    } else {
                        // If product not found, add minimal product info
                        Map<String, Object> productDetails = new HashMap<>();
                        productDetails.put("id", item.getProductId());
                        productDetails.put("name", "Product not available");
                        productDetails.put("description", "This product is no longer available");
                        productDetails.put("imageUrl", "/images/product-unavailable.jpg");
                        productDetails.put("category", "Unavailable");
                        itemMap.put("product", productDetails);
                    }
                    
                    return itemMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(cartItemsWithDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable String itemId, @RequestBody Map<String, Integer> update, Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CartItem cartItem = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));
            
            if (!cartItem.getUserId().equals(user.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            cartItem.setQuantity(update.get("quantity"));
            cartItemRepository.save(cartItem);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable String itemId, Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CartItem cartItem = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));
            
            if (!cartItem.getUserId().equals(user.getId())) {
                return ResponseEntity.status(403).build();
            }
            
            cartItemRepository.delete(cartItem);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 