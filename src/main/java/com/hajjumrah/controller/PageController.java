package com.hajjumrah.controller;

import com.hajjumrah.repository.ProductRepository;
import com.hajjumrah.service.ProductService;
import com.hajjumrah.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PageController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @GetMapping("/mens-kit")
    public String mensKit() {
        return "mens-kit";
    }

    @GetMapping("/mens-mini-kit")
    public String mensMiniKit(Model model) {
        // First, get the kit product
        Product kitProduct = productService.getProductById("mens-mini-kit-001");
        if (kitProduct != null) {
            model.addAttribute("kitProduct", kitProduct);
        }

        // Then get the individual products that make up the kit
        List<String> productIds = Arrays.asList(
            "ihram-cloth",
            "ihram-belt",
            "women2",
            "soap",
            "finger-counter2",
            "shoe-bag",
            "dua-cards"
        );
        
        List<Product> products = productIds.stream()
            .map(id -> productService.getProductById(id))
            .filter(product -> product != null)
            .collect(Collectors.toList());
            
        model.addAttribute("products", products);
        return "mensminikit";
    }

    @GetMapping("/womens-kit")
    public String womensKit() {
        return "womens-kit";
    }

    @GetMapping("/womens-mini-kit")
    public String womensMiniKit(Model model) {
        // First, get the kit product
        Product kitProduct = productService.getProductById("womens-mini-kit-001");
        if (kitProduct != null) {
            model.addAttribute("kitProduct", kitProduct);
        }

        // Then get the individual products that make up the kit
        List<String> productIds = Arrays.asList(
            "makhani",
            "namaz-dupatta",
            "hand-gloves",
            "socks",
            "women2",
            "soap",
            "finger-counter2",
            "shoe-bag",
            "dua-cards"
        );
        
        List<Product> products = productIds.stream()
            .map(id -> productService.getProductById(id))
            .filter(product -> product != null)
            .collect(Collectors.toList());
            
        model.addAttribute("products", products);
        return "womensminikit";
    }

    @GetMapping("/individual")
    public String individual(Model model) {
        List<Product> allProducts = productRepository.findAll();
        List<Product> filteredProducts = allProducts.stream()
            .filter(product -> !"Janamaz".equals(product.getCategory()))
            .collect(Collectors.toList());
        model.addAttribute("products", filteredProducts);
        return "individual";
    }

    @GetMapping("/tohfa-e-khulus")
    public String tohfaEKhulus(Model model) {
        try {
            // Get all products from Tohfa-E-Khulus category
            List<Product> categoryProducts = productService.getProductsByCategory("Tohfa-E-Khulus");
            
            // Get specific products by IDs that might not be in the category
            List<String> specificIds = Arrays.asList("prayer-beads", "surma", "perfume", "prayer-cap", "miswak", "janamaz-001");
            List<Product> specificProducts = specificIds.stream()
                .map(id -> productService.getProductById(id))
                .filter(product -> product != null)
                .collect(Collectors.toList());
            
            // Combine both lists, removing duplicates
            List<Product> allProducts = new ArrayList<>();
            allProducts.addAll(categoryProducts);
            
            // Add specific products if they're not already in the list
            specificProducts.forEach(product -> {
                if (allProducts.stream().noneMatch(p -> p.getId().equals(product.getId()))) {
                    allProducts.add(product);
                }
            });

            // Log the products for debugging
            System.out.println("Category Products: " + categoryProducts.size());
            System.out.println("Specific Products: " + specificProducts.size());
            System.out.println("Total Products: " + allProducts.size());
            
            model.addAttribute("products", allProducts);
        } catch (Exception e) {
            System.err.println("Error fetching products: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("products", new ArrayList<>());
        }
        return "tohfa-e-khulus";
    }

    @GetMapping("/janamaz")
    public String janamaz(Model model) {
        try {
            // Get all products from Janamaz category
            List<Product> products = productService.getProductsByCategory("Janamaz");
            model.addAttribute("products", products);
        } catch (Exception e) {
            System.err.println("Error fetching Janamaz products: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("products", new ArrayList<>());
        }
        return "janamaz";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/trolley-bag-on-rent")
    public String trolleyBagOnRent() {
        return "trolley-bag-on-rent";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }
} 