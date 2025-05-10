package com.hajjumrah.controller;

import com.hajjumrah.model.CartItem;
import com.hajjumrah.model.User;
import com.hajjumrah.repository.CartItemRepository;
import com.hajjumrah.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        List<CartItem> allCartItems = cartItemRepository.findAll();
        List<User> allUsers = userRepository.findAll();

        // Group cart items by user
        Map<String, List<CartItem>> userCartItems = allCartItems.stream()
            .collect(Collectors.groupingBy(CartItem::getUserId));

        model.addAttribute("users", allUsers);
        model.addAttribute("userCartItems", userCartItems);
        return "admin/dashboard";
    }

    @GetMapping("/api/cart-items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCartItems() {
        List<CartItem> allCartItems = cartItemRepository.findAll();
        return ResponseEntity.ok(allCartItems);
    }
} 