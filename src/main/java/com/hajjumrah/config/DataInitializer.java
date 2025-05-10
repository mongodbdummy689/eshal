package com.hajjumrah.config;

import com.hajjumrah.model.Product;
import com.hajjumrah.repository.ProductRepository;
import com.hajjumrah.model.User;
import com.hajjumrah.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Initialize products if they don't exist
        if (!productRepository.existsById("mens-kit-001")) {
            Product mensKit = new Product();
            mensKit.setId("mens-kit-001");
            mensKit.setName("Men's Complete Kit");
            mensKit.setDescription("Everything you need for your Hajj & Umrah journey in one comprehensive package");
            mensKit.setPrice(299.99);
            mensKit.setImageUrl("/img/menkit.jpg");
            mensKit.setCategory("Hajj & Umrah Kits");
            mensKit.setInStock(true);
            mensKit.setStockQuantity(100);
            productRepository.save(mensKit);
        }

        // Initialize women's kit if it doesn't exist
        if (!productRepository.existsById("womens-kit-001")) {
            Product womensKit = new Product();
            womensKit.setId("womens-kit-001");
            womensKit.setName("Women's Complete Kit");
            womensKit.setDescription("Everything you need for your Hajj & Umrah journey in one comprehensive package");
            womensKit.setPrice(349.99);
            womensKit.setImageUrl("/img/womenkit.jpg");
            womensKit.setCategory("Hajj & Umrah Kits");
            womensKit.setInStock(true);
            womensKit.setStockQuantity(100);
            productRepository.save(womensKit);
        }

        // Initialize Common Items
        initializeProduct("common1", "Common Item 1", "Essential item for Hajj & Umrah", 19.99, "/img/common1.JPG", "Common Items");
        initializeProduct("common2", "Common Item 2", "Essential item for Hajj & Umrah", 24.99, "/img/common2.JPG", "Common Items");
        initializeProduct("common3", "Common Item 3", "Essential item for Hajj & Umrah", 29.99, "/img/common3.JPG", "Common Items");
        initializeProduct("common4", "Common Item 4", "Essential item for Hajj & Umrah", 34.99, "/img/common4.JPG", "Common Items");

        // Initialize Men's Items
        initializeProduct("men1", "Men's Item 1", "Essential item for men during Hajj & Umrah", 39.99, "/img/men1.JPG", "Men's Items");
        initializeProduct("men2", "Men's Item 2", "Essential item for men during Hajj & Umrah", 44.99, "/img/men2.JPG", "Men's Items");
        initializeProduct("men3", "Men's Item 3", "Essential item for men during Hajj & Umrah", 54.99, "/img/men3.JPG", "Men's Items");
        initializeProduct("men4.1", "Men's Item 4.1", "Essential item for men during Hajj & Umrah", 59.99, "/img/men4.1.JPG", "Men's Items");
        initializeProduct("men4.2", "Men's Item 4.2", "Essential item for men during Hajj & Umrah", 64.99, "/img/men4.2.JPG", "Men's Items");
        initializeProduct("men4.3", "Men's Item 4.3", "Essential item for men during Hajj & Umrah", 69.99, "/img/men4.3.JPG", "Men's Items");
        initializeProduct("men5", "Men's Item 5", "Essential item for men during Hajj & Umrah", 74.99, "/img/men5.JPG", "Men's Items");
        initializeProduct("men6", "Men's Item 6", "Essential item for men during Hajj & Umrah", 79.99, "/img/men6.JPG", "Men's Items");
        initializeProduct("men7.1", "Men's Item 7.1", "Essential item for men during Hajj & Umrah", 84.99, "/img/men7.1.JPG", "Men's Items");
        initializeProduct("men7.2", "Men's Item 7.2", "Essential item for men during Hajj & Umrah", 89.99, "/img/men7.2.JPG", "Men's Items");
        initializeProduct("men8.1", "Men's Item 8.1", "Essential item for men during Hajj & Umrah", 94.99, "/img/men8.1.JPG", "Men's Items");
        initializeProduct("men8.2", "Men's Item 8.2", "Essential item for men during Hajj & Umrah", 99.99, "/img/men8.2.JPG", "Men's Items");
        initializeProduct("men8.3", "Men's Item 8.3", "Essential item for men during Hajj & Umrah", 104.99, "/img/men8.3.JPG", "Men's Items");

        // Initialize Women's Items
        initializeProduct("women1", "Women's Item 1", "Essential item for women during Hajj & Umrah", 49.99, "/img/women1.JPG", "Women's Items");
        initializeProduct("women2", "Women's Item 2", "Essential item for women during Hajj & Umrah", 109.99, "/img/women2.JPG", "Women's Items");
        initializeProduct("women3", "Women's Item 3", "Essential item for women during Hajj & Umrah", 114.99, "/img/women3.JPG", "Women's Items");
        initializeProduct("women4.1", "Women's Item 4.1", "Essential item for women during Hajj & Umrah", 119.99, "/img/women4.1.JPG", "Women's Items");
        initializeProduct("women4.2", "Women's Item 4.2", "Essential item for women during Hajj & Umrah", 124.99, "/img/women4.2.JPG", "Women's Items");
        initializeProduct("women4.3", "Women's Item 4.3", "Essential item for women during Hajj & Umrah", 129.99, "/img/women4.3.JPG", "Women's Items");
        initializeProduct("women5", "Women's Item 5", "Essential item for women during Hajj & Umrah", 134.99, "/img/women5.JPG", "Women's Items");
        initializeProduct("women6", "Women's Item 6", "Essential item for women during Hajj & Umrah", 139.99, "/img/women6.JPG", "Women's Items");
        initializeProduct("women7", "Women's Item 7", "Essential item for women during Hajj & Umrah", 144.99, "/img/women7.JPG", "Women's Items");

        // Create admin user if it doesn't exist
        if (!userRepository.existsByEmail("admin@hajjumrah.com")) {
            User admin = new User();
            admin.setEmail("admin@hajjumrah.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }

    private void initializeProduct(String id, String name, String description, double price, String imageUrl, String category) {
        if (!productRepository.existsById(id)) {
            Product product = new Product();
            product.setId(id);
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setImageUrl(imageUrl);
            product.setCategory(category);
            product.setInStock(true);
            product.setStockQuantity(100);
            productRepository.save(product);
        }
    }
} 