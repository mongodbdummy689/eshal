package com.hajjumrah.controller;

import com.hajjumrah.model.CartItem;
import com.hajjumrah.model.User;
import com.hajjumrah.model.Product;
import com.hajjumrah.model.Order;
import com.hajjumrah.repository.CartItemRepository;
import com.hajjumrah.repository.UserRepository;
import com.hajjumrah.repository.ProductRepository;
import com.hajjumrah.repository.OrderRepository;
import com.hajjumrah.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

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

    @GetMapping("/orders")
    public String trackOrders(Model model) {
        List<Order> allOrders = orderRepository.findAllByOrderByOrderDateDesc();
        
        // Calculate statistics
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream()
            .filter(order -> "PENDING".equals(order.getStatus()))
            .count();
        long confirmedOrders = allOrders.stream()
            .filter(order -> "CONFIRMED".equals(order.getStatus()))
            .count();
        long deliveredOrders = allOrders.stream()
            .filter(order -> "DELIVERED".equals(order.getStatus()))
            .count();
        
        model.addAttribute("orders", allOrders);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        
        return "admin/orders";
    }

    @GetMapping("/products/add")
    public String addProduct(Model model) {
        return "admin/add-product";
    }

    @PostMapping("/products/add")
    public ResponseEntity<?> addProduct(@RequestParam("productId") String productId,
                                       @RequestParam("name") String name,
                                       @RequestParam("description") String description,
                                       @RequestParam("category") String category,
                                       @RequestParam("price") double price,
                                       @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            // Check if product already exists
            if (productRepository.findById(productId).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Product with ID " + productId + " already exists"));
            }

            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                // Upload image to Cloudinary
                imageUrl = cloudinaryService.uploadImage(image, productId, "admin");
            }

            Product product = new Product();
            product.setId(productId);
            product.setName(name);
            product.setDescription(description);
            product.setCategory(category);
            product.setPrice(price);
            product.setImageUrl(imageUrl);
            product.setInStock(true);
            product.setStockQuantity(100);

            productRepository.save(product);

            return ResponseEntity.ok(Map.of("message", "Product added successfully", "imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error adding product: " + e.getMessage()));
        }
    }

    @GetMapping("/api/cart-items")
    public ResponseEntity<?> getAllCartItems() {
        List<CartItem> allCartItems = cartItemRepository.findAll();
        return ResponseEntity.ok(allCartItems);
    }
} 