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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

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
    public String dashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {
        
        // Get all users for statistics (unpaginated)
        List<User> allUsers = userRepository.findAll().stream()
            .filter(user -> !user.getRole().equals("ROLE_ADMIN"))
            .collect(Collectors.toList());
        
        // Create pageable object
        org.springframework.data.domain.PageRequest pageRequest = 
            org.springframework.data.domain.PageRequest.of(page, size);
        
        // Get paginated users based on search filter
        org.springframework.data.domain.Page<User> usersPage;
        if (search != null && !search.trim().isEmpty()) {
            usersPage = userRepository.findRegularUsersBySearch(search.trim(), pageRequest);
        } else {
            usersPage = userRepository.findRegularUsers(pageRequest);
        }
        
        List<User> regularUsers = usersPage.getContent();
        
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
        
        // Calculate statistics
        long totalUsers = allUsers.size();
        long usersWithCartItems = allUsers.stream()
            .filter(user -> !cartItemRepository.findByUserId(user.getId()).isEmpty())
            .count();
        long totalCartItems = allUsers.stream()
            .mapToLong(user -> cartItemRepository.findByUserId(user.getId()).size())
            .sum();
        
        // Calculate pagination info
        int totalPages = usersPage.getTotalPages();
        long totalElements = usersPage.getTotalElements();
        boolean hasNext = usersPage.hasNext();
        boolean hasPrevious = usersPage.hasPrevious();
        
        model.addAttribute("regularUsers", regularUsers);
        model.addAttribute("regularUserCartItems", regularUserCartItems);
        model.addAttribute("productNames", productNames);
        
        // Statistics attributes
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("usersWithCartItems", usersWithCartItems);
        model.addAttribute("totalCartItems", totalCartItems);
        
        // Pagination attributes
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrevious", hasPrevious);
        model.addAttribute("pageSize", size);
        
        // Search attribute
        model.addAttribute("search", search);
        
        return "admin/dashboard";
    }

    @GetMapping("/orders")
    public String trackOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {
        
        // Get all orders for statistics (unpaginated)
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
        
        // Create pageable object
        org.springframework.data.domain.PageRequest pageRequest = 
            org.springframework.data.domain.PageRequest.of(page, size);
        
        // Get paginated orders based on search and status filters
        org.springframework.data.domain.Page<Order> ordersPage;
        if (search != null && !search.trim().isEmpty()) {
            ordersPage = orderRepository.searchOrders(search.trim(), pageRequest);
        } else if (status != null && !status.trim().isEmpty()) {
            ordersPage = orderRepository.findByStatusOrderByOrderDateDesc(status.trim(), pageRequest);
        } else {
            ordersPage = orderRepository.findAllByOrderByOrderDateDesc(pageRequest);
        }
        
        // Calculate pagination info
        int totalPages = ordersPage.getTotalPages();
        long totalElements = ordersPage.getTotalElements();
        boolean hasNext = ordersPage.hasNext();
        boolean hasPrevious = ordersPage.hasPrevious();
        
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        
        // Pagination attributes
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrevious", hasPrevious);
        model.addAttribute("pageSize", size);
        
        // Search and filter attributes
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        
        return "admin/orders";
    }

    @GetMapping("/products")
    public String manageProducts(Model model) {
        List<Product> allProducts = productRepository.findAll();
        model.addAttribute("products", allProducts);
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String addProduct(Model model) {
        return "admin/add-product";
    }

    @GetMapping("/products/edit/{productId}")
    public String editProduct(@PathVariable String productId, Model model) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/admin/products";
        }
        
        // Add categories list to model
        List<String> categories = Arrays.asList(
            "Hajj & Umrah Kits",
            "Men's Items", 
            "Women's Items",
            "Common Items",
            "Tohfa-E-Khulus",
            "Men's Mini Kit",
            "Women's Mini Kit",
            "Individual Items",
            "Janamaz"
        );
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "admin/edit-product";
    }

    @PostMapping("/products/add")
    public ResponseEntity<?> addProduct(@RequestParam("productId") String productId,
                                       @RequestParam("name") String name,
                                       @RequestParam("description") String description,
                                       @RequestParam("category") String category,
                                       @RequestParam("price") double price,
                                       @RequestParam("stockQuantity") int stockQuantity,
                                       @RequestParam("gstRate") double gstRate,
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
            product.setStockQuantity(stockQuantity);
            product.setGstRate(gstRate);
            product.setImageUrl(imageUrl);
            product.setInStock(true);

            productRepository.save(product);

            return ResponseEntity.ok(Map.of("message", "Product added successfully", "imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error adding product: " + e.getMessage()));
        }
    }

    @PostMapping("/products/edit")
    public ResponseEntity<?> editProduct(@RequestParam("productId") String productId,
                                        @RequestParam("name") String name,
                                        @RequestParam("description") String description,
                                        @RequestParam("category") String category,
                                        @RequestParam("price") double price,
                                        @RequestParam("stockQuantity") int stockQuantity,
                                        @RequestParam("gstRate") double gstRate,
                                        @RequestParam(value = "inStock", required = false) String inStock,
                                        @RequestParam(value = "weight", required = false) Double weight,
                                        @RequestParam(value = "length", required = false) Double length,
                                        @RequestParam(value = "width", required = false) Double width,
                                        @RequestParam(value = "height", required = false) Double height,
                                        @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Product not found"));
            }

            Product product = productOpt.get();
            product.setName(name);
            product.setDescription(description);
            product.setCategory(category);
            product.setPrice(price);
            product.setStockQuantity(stockQuantity);
            product.setGstRate(gstRate);
            product.setInStock("on".equals(inStock));
            product.setWeight(weight);
            product.setLength(length);
            product.setWidth(width);
            product.setHeight(height);

            // Handle image upload if new image is provided
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image, productId, "admin");
                product.setImageUrl(imageUrl);
            }

            productRepository.save(product);

            return ResponseEntity.ok(Map.of("message", "Product updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error updating product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable String productId) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Product not found"));
            }

            productRepository.deleteById(productId);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error deleting product: " + e.getMessage()));
        }
    }

    @PostMapping("/products/{productId}/toggle-stock")
    public ResponseEntity<?> toggleStock(@PathVariable String productId, @RequestBody Map<String, Boolean> request) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Product not found"));
            }

            Product product = productOpt.get();
            product.setInStock(request.get("inStock"));
            productRepository.save(product);

            return ResponseEntity.ok(Map.of("message", "Stock status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error updating stock status: " + e.getMessage()));
        }
    }

    @GetMapping("/api/cart-items")
    public ResponseEntity<?> getAllCartItems() {
        List<CartItem> allCartItems = cartItemRepository.findAll();
        return ResponseEntity.ok(allCartItems);
    }
} 