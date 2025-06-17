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
    public String mensKit(Model model) {
        // First, get the kit product
        Product kitProduct = productService.getProductById("mens-kit-001");
        if (kitProduct != null) {
            model.addAttribute("kitProduct", kitProduct);
        }

        // Get all products for the men's kit in the specified sequence
        List<String> productIds = Arrays.asList(
            "ehram",           // Ehram
            "ehram-belt",            // Ehram Belt
            "ehram-lock-button",     // Ehram Lock Button
            "travel-janamaz",            // Travel Janamaz
            "universal-adaptor",     // Universal Adaptor
            "soap",                  // Odourless Soap
            "tasbeeh",          // Tasbeeh
            "tawaf-tasbeeh",         // Tawaf Tasbeeh
            "digital-tasbeeh",       // Digital Tasbeeh
            "ittar",               // Ittar
            "surma",                 // Surma
            "prayer-cap",            // Prayer Cap
            "miswak",                // Meswak
            "napkin",                // Cotton Napkin
            "guide",                 // Umrah Guide
            "dua-cards",             // Umrah Dua Card
            "sewing-kit",         // Sewing Kit
            "shoe-bag",              // Chappal Bag
            "kit-bag"                // Kit bag
        );
        
        List<Product> products = productIds.stream()
            .map(id -> productService.getProductById(id))
            .filter(product -> product != null)
            .collect(Collectors.toList());
            
        model.addAttribute("products", products);
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
            "ehram",           // Ehram
            "ehram-belt",      // Ehram Belt
            "ehram-lock-button", // Ehram Lock Button
            "tawaf-tasbeeh",   // Tawaf Tasbeeh
            "soap",            // Odourless Soap
            "digital-tasbeeh", // Digital Tasbeeh
            "shoe-bag",        // Chappal Bag
            "dua-cards"        // Umrah Dua Card
        );
        
        List<Product> products = productIds.stream()
            .map(id -> productService.getProductById(id))
            .filter(product -> product != null)
            .collect(Collectors.toList());
            
        model.addAttribute("products", products);
        return "mensminikit";
    }

    @GetMapping("/womens-kit")
    public String womensKit(Model model) {
        // First, get the kit product
        Product kitProduct = productService.getProductById("womens-kit-001");
        if (kitProduct != null) {
            model.addAttribute("kitProduct", kitProduct);
        }

        // Get all products for the women's kit in the specified sequence
        List<String> productIds = Arrays.asList(
            "makhani",              // Makhani
            "namaz-dupatta",        // Namaz Dupatta
            "hand-gloves",          // Handgloves
            "socks",                // Socks
            "hijabcap",           // Hijab Cap
            "travel-janamaz",       // Travel Janamaz
            "universal-adaptor",    // Universal Adaptor
            "soap",                 // Odourless Soap
            "tasbeeh",             // Tasbeeh
            "tawaf-tasbeeh",       // Tawaf Tasbeeh
            "digital-tasbeeh",     // Digital Tasbeeh
            "surma",               // Surma
            "napkin",              // Cotton Napkin
            "guide",               // Umrah Guide
            "dua-cards",           // Umrah Dua Card
            "sewing-kit",          // Sewing Kit
            "shoe-bag",            // Chappal Bag
            "kit-bag"              // Kit bag
        );
        
        List<Product> products = productIds.stream()
            .map(id -> productService.getProductById(id))
            .filter(product -> product != null)
            .collect(Collectors.toList());
            
        model.addAttribute("products", products);
        return "womens-kit";
    }

    @GetMapping("/womens-mini-kit")
    public String womensMiniKit(Model model) {
        // First, get the kit product
        Product kitProduct = productService.getProductById("womens-mini-kit-001");
        if (kitProduct != null) {
            model.addAttribute("kitProduct", kitProduct);
        }

        // Then get the individual products that make up the kit in the specified sequence
        List<String> productIds = Arrays.asList(
            "makhani",              // 1) Makhani
            "namaz-dupatta",        // 2) Dupatta
            "hand-gloves",          // 3) Hand gloves
            "socks",                // 4) Socks
            "hijabcap",           // 5) Hijab Cap
            "tawaf-tasbeeh",        // 6) Tawaf Tasbeeh
            "soap",                 // 7) Odourless Soap
            "digital-tasbeeh",      // 8) Digital Tasbeeh
            "shoe-bag",             // 9) Shoe Bag
            "dua-cards"             // 10) Umrah Dua Card
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
            .filter(product -> !"Khajur (Dates - 4 pcs)".equals(product.getName()))
            .filter(product -> !"Zam Zam Water (60 ml)".equals(product.getName()))
            .collect(Collectors.toList());
        model.addAttribute("products", filteredProducts);
        return "individual";
    }

    @GetMapping("/tohfa-e-khulus")
    public String tohfaEKhulus(Model model) {
        try {
            // Get specific products by IDs in the requested order
            List<String> specificIds = Arrays.asList(
                "janamaz-001",      // Janamaz
                "tasbeeh",          // Tasbeeh
                "prayer-cap",       // Namaj Cap
                "miswak",           // Miswak
                "digital-tasbeeh",  // Digital tasbeeh
                "zamzam",           // Zamzam water bottle (60 ml)
                "khajur",           // Khajur (4 nos.)
                "safa",             // Big Rumal(Safa)
                "surma",            // Surma
                "makhani",          // Makhani
                "namaz-dupatta",    // Namaj Dupatta
                "napkin"            // Napkin
            );
            
            List<Product> products = specificIds.stream()
                .map(id -> productService.getProductById(id))
                .filter(product -> product != null)
                .collect(Collectors.toList());
            
            model.addAttribute("products", products);
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