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
        // Delete all existing products
        productRepository.deleteAll();
        
        // Initialize products if they don't exist
        if (!productRepository.existsById("mens-kit-001")) {
            Product mensKit = new Product();
            mensKit.setId("mens-kit-001");
            mensKit.setName("Eshal's Hajj & Umrah Kit for Men");
            mensKit.setDescription("Everything you need for your Hajj & Umrah journey in one comprehensive package");
            mensKit.setPrice(300);
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
            womensKit.setName("Eshal's Hajj & Umrah Kit for Women");
            womensKit.setDescription("Everything you need for your Hajj & Umrah journey in one comprehensive package");
            womensKit.setPrice(350);
            womensKit.setImageUrl("/img/womenkit.jpeg");
            womensKit.setCategory("Hajj & Umrah Kits");
            womensKit.setInStock(true);
            womensKit.setStockQuantity(100);
            productRepository.save(womensKit);
        }

//Men's Items
initializeProduct("ehram", "Ehram", "Complete Ihram set including two pieces of white, seamless cloth made from high-quality cotton.", 20, "/img/men/ihram-cloth.jpg", "Men's Items");
initializeProduct("ehram-belt", "Ehram Belt", "Adjustable belt to secure Ihram cloth in place, made from durable material with secure fastening.", 20, "/img/men/ihram-belt.jpg", "Men's Items");
initializeProduct("prayer-cap", "Prayer Cap", "Traditional prayer cap (Kufi) made from comfortable, breathable fabric suitable for daily prayers.", 20, "/img/men/prayer-cap.jpg", "Men's Items");
initializeProduct("miswak", "Miswak", "Natural tooth cleaning stick (Siwak) from the Salvadora Persica tree, traditionally used for oral hygiene.", 20, "/img/men/miswak.jpg", "Men's Items");	
initializeProduct("ittar", "Ittar", "Traditional Arabian fragrance made without alcohol, perfect for maintaining a pleasant scent during your journey.", 20, "/img/men/perfume.jpg", "Men's Items");

//Women's Items
initializeProduct("hand-gloves", "Hand Gloves", "High-quality cotton gloves designed for women during Hajj & Umrah, providing comfort and protection while maintaining modesty requirements.", 20, "/img/women/hand-gloves.jpg", "Women's Items");
initializeProduct("makhani", "Makhani", "Traditional prayer cap (Makhani) made from premium quality fabric, perfect for maintaining modesty during prayers and religious activities.", 20, "/img/women/makhani.jpg", "Women's Items");
initializeProduct("namaz-dupatta", "Namaz Dupatta", "Elegant and lightweight prayer scarf made from breathable fabric, designed specifically for women's prayers with appropriate length and coverage.", 20, "/img/women/namaz-dupatta.jpg", "Women's Items");
initializeProduct("soap", "Odourless Soap", "Gentle, fragrance-free soap made from natural ingredients, perfect for maintaining cleanliness during Ihram and other religious activities.", 20, "/img/women/soap.jpg", "Women's Items");
initializeProduct("socks", "Ladies Socks", "Comfortable, breathable socks designed specifically for women, providing protection and comfort during prayers and walking rituals.", 20, "/img/women/socks.jpg", "Women's Items");
initializeProduct("tawaf-tasbeeh", "Tawaf Tasbeeh", "Beautiful prayer beads (Tasbeeh) specifically designed for women, made with high-quality materials and perfect for counting dhikr during Tawaf.", 20, "/img/women/women2.jpg", "Women's Items");
initializeProduct("hijabcap", "Hijab Cap", "Elegant and comfortable prayer cap (Hijab Cap) designed specifically for women, made from soft, breathable fabric. Perfect for maintaining modesty during prayers and religious activities, this cap provides a secure base for wearing hijab while ensuring comfort throughout the day.", 20, "/img/women/hijabcap.webp", "Women's Items");

//Common Items
initializeProduct("guide", "Hajj & Umrah Guide", "Comprehensive guidebook with step-by-step instructions for Hajj and Umrah rituals, including maps and important information.", 20, "/img/common/guide.jpg", "Common Items");
initializeProduct("dua-cards", "Hajj & Umrah Dua Cards", "Set of laminated cards containing essential duas and supplications for different stages of Hajj and Umrah.", 20, "/img/common/dua-cards.jpg", "Common Items");
initializeProduct("tasbeeh", "Tasbeeh", "High-quality prayer beads (Tasbeeh) for counting dhikr and prayers, made with durable materials.", 20, "/img/common/prayer-beads.jpg", "Common Items");
initializeProduct("digital-tasbeeh", "Digital Tasbeeh", "Digital Tasbeeh for keeping track of dhikr and prayers, with easy-to-use buttons and clear display.", 20, "/img/common/finger-counter2.jpg", "Common Items");
initializeProduct("napkin", "Cotton Napkin", "Soft, absorbent cotton napkin for personal hygiene and comfort during your journey.", 20, "/img/common/napkin.jpg", "Common Items");
initializeProduct("surma", "Surma", "Traditional kohl (Surma) for eye care, made from natural ingredients following traditional methods.", 20, "/img/common/surma.jpg", "Common Items");
initializeProduct("shoe-bag", "Shoe Bag", "Durable, lightweight shoe bag with secure closure, designed to keep your footwear clean and organized during mosque visits and prayers.", 20, "/img/common/shoe-bag.jpg", "Common Items");
initializeProduct("sewing-kit", "Sewing Kit", "Compact sewing kit with needles, thread, and safety pins for emergency repairs during your journey.", 20, "/img/common/stitching-set.jpg", "Common Items");
initializeProduct("travel-janamaz", "Travel Janamaz with Compass", "Portable, lightweight prayer mat with carrying bag, perfect for daily prayers during your journey.", 20, "/img/common/travel-janamaz.jpg", "Common Items");
initializeProduct("universal-adaptor", "Universal Travel Adaptor", "Multi-country travel adaptor compatible with various plug types used in Saudi Arabia and other countries.", 20, "/img/common/universal-adaptor1.jpg", "Common Items");

//Tohfa-E-Khulus
initializeProduct("khajur", "Khajur (Dates - 4 pcs)", "Premium quality dates from Madinah", 299, "/img/tohfaekhulus/khajur.jpg", "Tohfa-E-Khulus");
initializeProduct("zamzam", "Zam Zam Water (60 ml)", "Blessed water from the holy well of Zam Zam", 499, "/img/tohfaekhulus/zamzam.jpg", "Tohfa-E-Khulus");

//Men's Mini Kit
initializeProduct("mens-mini-kit-001", "Men's Umrah Mini Kit", "Essential collection of items for men performing Umrah, including Ihram, prayer cap, miswak, and other necessary items for a complete spiritual journey.", 1000, "/img/umrahminikit/men-kit.jpeg", "Men's Mini Kit");

//Women's Mini Kit
initializeProduct("womens-mini-kit-001", "Women's Umrah Mini Kit", "Essential collection of items for women performing Umrah, including prayer cap (Makhani), prayer scarf, hand gloves, and other necessary items for a complete spiritual journey.", 900, "/img/umrahminikit/women-kit.jpeg", "Women's Mini Kit");

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