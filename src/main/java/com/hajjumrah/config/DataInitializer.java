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
            womensKit.setImageUrl("/img/womenkit.JPG");
            womensKit.setCategory("Hajj & Umrah Kits");
            womensKit.setInStock(true);
            womensKit.setStockQuantity(100);
            productRepository.save(womensKit);
        }

//Men's Items
initializeProduct("ihram-cloth", "Ihram Cloth", "Essential item for Hajj & Umrah", 19.99, "/img/men/ihram-cloth.jpg", "Men's Items");
initializeProduct("ihram-belt", "Ihram Belt", "Essential item for Hajj & Umrah", 19.99, "/img/men/ihram-belt.jpg", "Men's Items");
initializeProduct("prayer-cap", "Prayer Cap", "Essential item for Hajj & Umrah", 19.99, "/img/men/prayer-cap.jpg", "Men's Items");
initializeProduct("miswak", "Miswak", "Essential item for Hajj & Umrah", 19.99, "/img/men/miswak.jpg", "Men's Items");	
initializeProduct("perfume", "Perfume", "Essential item for Hajj & Umrah", 19.99, "/img/men/perfume.jpg", "Men's Items");

//Women's Items
initializeProduct("hand-gloves", "Hand Gloves", "Essential item for Hajj & Umrah", 19.99, "/img/women/hand-gloves.jpg", "Women's Items");
initializeProduct("makhani", "Makhani", "Essential item for Hajj & Umrah", 19.99, "/img/women/makhani.jpg", "Women's Items");
initializeProduct("namaz-dupatta", "Namaz Dupatta", "Essential item for Hajj & Umrah", 19.99, "/img/women/namaz-dupatta.jpg", "Women's Items");
initializeProduct("soap", "Soap", "Essential item for Hajj & Umrah", 19.99, "/img/women/soap.jpg", "Women's Items");
initializeProduct("socks", "Socks", "Essential item for Hajj & Umrah", 19.99, "/img/women/socks.jpg", "Women's Items");
initializeProduct("women2", "Beads", "Essential item for Hajj & Umrah", 19.99, "/img/women/women2.jpg", "Women's Items");

//Common Items
initializeProduct("guide", "Hajj/Umrah Guide", "Essential item for Hajj & Umrah", 19.99, "/img/common/guide.jpg", "Common Items");
initializeProduct("dua-cards", "Hajj/Umrah Dua Cards", "Essential item for Hajj & Umrah", 19.99, "/img/common/dua-cards.jpg", "Common Items");
initializeProduct("prayer-beads", "Prayer Beads", "Essential item for Hajj & Umrah", 19.99, "/img/common/prayer-beads.jpg", "Common Items");
initializeProduct("finger-counter2", "Finger Counter", "Essential item for Hajj & Umrah", 19.99, "/img/common/finger-counter2.jpg", "Common Items");
initializeProduct("napkin", "Cotton Napkin", "Essential item for Hajj & Umrah", 19.99, "/img/common/napkin.jpg", "Common Items");
initializeProduct("prayer-mat-bag", "Prayer Mat Bag", "Essential item for Hajj & Umrah", 19.99, "/img/common/prayer-mat-bag.jpg", "Common Items");
initializeProduct("surma", "Surma", "Essential item for Hajj & Umrah", 19.99, "/img/common/surma.jpg", "Common Items");
initializeProduct("qibla-compass1", "Qibla Compass", "Essential item for Hajj & Umrah", 19.99, "/img/common/qibla-compass1.jpg", "Common Items");
initializeProduct("shoe-bag", "Shoe Bag", "Essential item for Hajj & Umrah", 19.99, "/img/common/shoe-bag.jpg", "Common Items");
initializeProduct("stitching-set", "Stitching Set", "Essential item for Hajj & Umrah", 19.99, "/img/common/stitching-set.jpg", "Common Items");
initializeProduct("travel-janamaz", "Travel Janamaz", "Essential item for Hajj & Umrah", 19.99, "/img/common/travel-janamaz.jpg", "Common Items");
initializeProduct("universal-adaptor1", "Universal Adaptor", "Essential item for Hajj & Umrah", 19.99, "/img/common/universal-adaptor1.jpg", "Common Items");

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