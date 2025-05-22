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
            mensKit.setImageUrl("/img/menkit.jpeg");
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

//Men's Items
initializeProduct("ehram-cloth", "Ehram", "Essential item for Hajj & Umrah", 19.99, "/img/men/ihram-cloth.jpg", "Men's Items");
initializeProduct("ehram-belt", "Ehram Belt", "Essential item for Hajj & Umrah", 19.99, "/img/men/ihram-belt.jpg", "Men's Items");
initializeProduct("prayer-cap", "Prayer Cap", "Essential item for Hajj & Umrah", 19.99, "/img/men/prayer-cap.jpg", "Men's Items");
initializeProduct("miswak", "Miswak", "Essential item for Hajj & Umrah", 19.99, "/img/men/miswak.jpg", "Men's Items");	
initializeProduct("perfume", "Perfume", "Essential item for Hajj & Umrah", 19.99, "/img/men/perfume.jpg", "Men's Items");

//Women's Items
initializeProduct("hand-gloves", "Hand Gloves", "Essential item for Hajj & Umrah", 19.99, "/img/women/hand-gloves.jpg", "Women's Items");
initializeProduct("makhani", "Makhani", "Essential item for Hajj & Umrah", 19.99, "/img/women/makhani.jpg", "Women's Items");
initializeProduct("namaz-dupatta", "Namaz Dupatta", "Essential item for Hajj & Umrah", 19.99, "/img/women/namaz-dupatta.jpg", "Women's Items");
initializeProduct("soap", "Odourless Soap", "Essential item for Hajj & Umrah", 19.99, "/img/women/soap.jpg", "Women's Items");
initializeProduct("socks", "Ladies Socks", "Essential item for Hajj & Umrah", 19.99, "/img/women/socks.jpg", "Women's Items");
initializeProduct("tawaf-tasbeeh", "Tawaf Tasbeeh", "Essential item for Hajj & Umrah", 19.99, "/img/women/women2.jpg", "Women's Items");

//Common Items
initializeProduct("guide", "Hajj & Umrah Guide as well as card", "Essential item for Hajj & Umrah", 19.99, "/img/common/guide.jpg", "Common Items");
initializeProduct("dua-cards", "Hajj & Umrah Dua Cards", "Essential item for Hajj & Umrah", 19.99, "/img/common/dua-cards.jpg", "Common Items");
initializeProduct("tasbeeh", "Tasbeeh", "Essential item for Hajj & Umrah", 19.99, "/img/common/prayer-beads.jpg", "Common Items");
initializeProduct("finger-counter", "Finger Counter", "Essential item for Hajj & Umrah", 19.99, "/img/common/finger-counter2.jpg", "Common Items");
initializeProduct("napkin", "Cotton Napkin", "Essential item for Hajj & Umrah", 19.99, "/img/common/napkin.jpg", "Common Items");
initializeProduct("prayer-mat-bag", "Prayer Mat Bag", "Essential item for Hajj & Umrah", 19.99, "/img/common/prayer-mat-bag.jpg", "Common Items");
initializeProduct("surma", "Surma", "Essential item for Hajj & Umrah", 19.99, "/img/common/surma.jpg", "Common Items");
initializeProduct("shoe-bag", "Shoe Bag", "Essential item for Hajj & Umrah", 19.99, "/img/common/shoe-bag.jpg", "Common Items");
initializeProduct("stitching-set", "Sewing Kit", "Essential item for Hajj & Umrah", 19.99, "/img/common/stitching-set.jpg", "Common Items");
initializeProduct("travel-janamaz", "Travel Janamaz with Compass", "Essential item for Hajj & Umrah", 19.99, "/img/common/travel-janamaz.jpg", "Common Items");
initializeProduct("universal-adaptor", "Universal Adaptor", "Essential item for Hajj & Umrah", 19.99, "/img/common/universal-adaptor1.jpg", "Common Items");

//Tohfa-E-Khulus
initializeProduct("khajur", "Khajur (Dates)", "Premium quality dates from Madinah", 299, "/img/tohfaekhulus/khajur.jpg", "Tohfa-E-Khulus");
initializeProduct("zamzam", "Zam Zam Water", "Blessed water from the holy well of Zam Zam", 499, "/img/tohfaekhulus/zamzam.jpg", "Tohfa-E-Khulus");

//Men's Mini Kit
initializeProduct("mens-mini-kit-001", "Men's Umrah Mini Kit", "Essential Items for Your Spiritual Journey", 1000, "/img/umrahminikit/men-kit.jpeg", "Men's Mini Kit");

//Women's Mini Kit
initializeProduct("womens-mini-kit-001", "Women's Umrah Mini Kit", "Essential Items for Your Spiritual Journey", 900, "/img/umrahminikit/women-kit.jpeg", "Women's Mini Kit");

//Janamaz
initializeProduct("janamaz-001", "Classic Janamaz", "Traditional design prayer mat with premium quality material", 1299, "/img/janamaz/janamaz1.jpeg", "Janamaz");
initializeProduct("janamaz-002", "Modern Janamaz", "Contemporary design prayer mat with elegant patterns", 1499, "/img/janamaz/janamaz2.jpeg", "Janamaz");
initializeProduct("janamaz-003", "Luxury Janamaz", "High-end prayer mat with intricate designs", 1999, "/img/janamaz/janamaz3.jpeg", "Janamaz");
initializeProduct("janamaz-004", "Travel Janamaz", "Compact and portable prayer mat for travelers", 999, "/img/janamaz/janamaz4.jpeg", "Janamaz");
initializeProduct("janamaz-005", "Premium Janamaz", "Premium quality prayer mat with beautiful embroidery", 1799, "/img/janamaz/janamaz5.jpeg", "Janamaz");
initializeProduct("janamaz-006", "Deluxe Janamaz", "Deluxe prayer mat with premium comfort", 1699, "/img/janamaz/janamaz6.jpeg", "Janamaz");

        // Create admin user if it doesn't exist
        if (!userRepository.existsByEmail("admin@eshal.com")) {
            User admin = new User();
            admin.setEmail("admin@eshal.com");
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