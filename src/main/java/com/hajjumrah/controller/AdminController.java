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
public class AdminController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        List<CartItem> allCartItems = cartItemRepository.findAll();
        List<User> allUsers = userRepository.findAll();

        // Create a map of product IDs to names
        Map<String, String> productNames = new HashMap<>();
        allCartItems.forEach(item -> {
            if (!productNames.containsKey(item.getProductId())) {
                productRepository.findById(item.getProductId())
                    .ifPresent(product -> productNames.put(product.getId(), product.getName()));
            }
        });

        // Group cart items by user and add product names
        Map<String, List<CartItem>> userCartItems = allCartItems.stream()
            .collect(Collectors.groupingBy(CartItem::getUserId));

        model.addAttribute("users", allUsers);
        model.addAttribute("userCartItems", userCartItems);
        model.addAttribute("productNames", productNames);
        return "admin/dashboard";
    }

    @GetMapping("/api/cart-items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCartItems() {
        List<CartItem> allCartItems = cartItemRepository.findAll();
        return ResponseEntity.ok(allCartItems);
    }
} 