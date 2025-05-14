package com.hajjumrah.controller;

import com.hajjumrah.model.CartItem;
import com.hajjumrah.model.User;
import com.hajjumrah.model.Product;
import com.hajjumrah.repository.CartItemRepository;
import com.hajjumrah.repository.UserRepository;
import com.hajjumrah.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public String dashboard(Model model) {
        List<User> regularUsers = userRepository.findAll().stream()
            .filter(user -> !user.getRole().equals("ROLE_ADMIN"))
            .collect(Collectors.toList());
        
        Map<String, List<CartItem>> regularUserCartItems = new HashMap<>();
        Map<String, String> productNames = new HashMap<>();
        
        for (User user : regularUsers) {
            List<CartItem> userCartItems = cartItemRepository.findByUserId(user.getId());
            if (!userCartItems.isEmpty()) {
                regularUserCartItems.put(user.getId(), userCartItems);
                // Get product names for the cart items
                for (CartItem item : userCartItems) {
                    if (!productNames.containsKey(item.getProductId())) {
                        productRepository.findById(item.getProductId())
                            .ifPresent(product -> productNames.put(product.getId(), product.getName()));
                    }
                }
            }
        }
        
        model.addAttribute("regularUsers", regularUsers);
        model.addAttribute("regularUserCartItems", regularUserCartItems);
        model.addAttribute("productNames", productNames);
        
        return "admin/dashboard";
    }

    @GetMapping("/api/cart-items")
    public ResponseEntity<?> getAllCartItems() {
        List<CartItem> allCartItems = cartItemRepository.findAll();
        return ResponseEntity.ok(allCartItems);
    }
} 