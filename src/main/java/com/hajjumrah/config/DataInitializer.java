package com.hajjumrah.config;

import com.hajjumrah.model.Product;
import com.hajjumrah.repository.ProductRepository;
import com.hajjumrah.model.User;
import com.hajjumrah.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.hajjumrah.model.ProductVariant;
import java.util.Arrays;

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
            mensKit.setPrice(1800);
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
            womensKit.setPrice(1600);
            womensKit.setImageUrl("/img/womenkit.jpeg");
            womensKit.setCategory("Hajj & Umrah Kits");
            womensKit.setInStock(true);
            womensKit.setStockQuantity(100);
            productRepository.save(womensKit);
        }

//Men's Items
initializeProduct("ehram-belt", "Ehram Belt", "Adjustable belt to secure Ihram cloth in place, made from durable material with secure fastening.", 20, "/img/men/ihram-belt.jpg", "Men's Items");
initializeProduct("miswak", "Miswak", "Natural tooth cleaning stick (Siwak) from the Salvadora Persica tree, traditionally used for oral hygiene.", 30, "/img/men/miswak.jpg", "Men's Items");	
initializeProduct("ittar", "Ittar", "Traditional Arabian fragrance made without alcohol, perfect for maintaining a pleasant scent during your journey.", 20, "/img/men/perfume.jpg", "Men's Items");

//Women's Items
initializeProduct("hand-gloves", "Ladies Hand Gloves", "High-quality cotton gloves designed for women during Hajj & Umrah, providing comfort and protection while maintaining modesty requirements.", 80, "/img/women/hand-gloves.jpg", "Women's Items");
initializeProduct("makhani", "Makhani", "Traditional prayer cap (Makhani) made from premium quality fabric, perfect for maintaining modesty during prayers and religious activities.", 400, "/img/women/makhani.jpg", "Women's Items");
initializeProduct("namaz-dupatta", "Namaz Dupatta", "Elegant and lightweight prayer scarf made from breathable fabric, designed specifically for women's prayers with appropriate length and coverage.", 150, "/img/women/namaz-dupatta.jpg", "Women's Items");
initializeProduct("soap", "Odourless Soap", "Gentle, fragrance-free soap made from natural ingredients, perfect for maintaining cleanliness during Ihram and other religious activities.", 40, "/img/women/soap.jpg", "Women's Items");
initializeProduct("socks", "Ladies Socks", "Comfortable, breathable socks designed specifically for women, providing protection and comfort during prayers and walking rituals.", 80, "/img/women/socks.jpg", "Women's Items");
initializeProduct("tawaf-tasbeeh", "Tawaf Tasbeeh", "Beautiful prayer beads (Tasbeeh) specifically designed for women, made with high-quality materials and perfect for counting dhikr during Tawaf.", 7, "/img/women/women2.jpg", "Women's Items");
initializeProduct("hijabcap", "Hijab Cap", "Elegant and comfortable prayer cap (Hijab Cap) designed specifically for women, made from soft, breathable fabric. Perfect for maintaining modesty during prayers and religious activities, this cap provides a secure base for wearing hijab while ensuring comfort throughout the day.", 60, "/img/women/hijabcap.webp", "Women's Items");

//Common Items
initializeProduct("digital-tasbeeh", "Finger Counter", "Finger Counter for keeping track of dhikr and prayers, with easy-to-use buttons and clear display.", 40, "/img/common/finger-counter2.jpg", "Common Items");
initializeProduct("napkin", "Cotton Napkin", "Soft, absorbent cotton napkin for personal hygiene and comfort during your journey.", 30, "/img/common/napkin.jpg", "Common Items");
initializeProduct("surma", "Surma", "Traditional kohl (Surma) for eye care, made from natural ingredients following traditional methods.", 20, "/img/common/surma.jpg", "Common Items");
initializeProduct("shoe-bag", "Shoe Bag", "Durable, lightweight shoe bag with secure closure, designed to keep your footwear clean and organized during mosque visits and prayers.", 65, "/img/common/shoe-bag.jpg", "Common Items");
initializeProduct("sewing-kit", "Sewing Kit", "Compact sewing kit with needles, thread, and safety pins for emergency repairs during your journey.", 50, "/img/common/stitching-set.jpg", "Common Items");
initializeProduct("travel-janamaz", "Travel Janamaz with Compass", "Portable, lightweight prayer mat with carrying bag, perfect for daily prayers during your journey.", 150, "/img/common/travel-janamaz.jpg", "Common Items");
initializeProduct("universal-adaptor", "Universal Travel Adaptor", "Multi-country travel adaptor compatible with various plug types used in Saudi Arabia and other countries.", 150, "/img/common/universal-adaptor1.jpg", "Common Items");

//Tohfa-E-Khulus
initializeProduct("khajur", "Khajur (Dates - 4 pcs)", "Premium quality dates from Madinah", 299, "/img/tohfaekhulus/khajur.jpg", "Tohfa-E-Khulus");
initializeProduct("zamzam", "Zam Zam Water (60 ml)", "Blessed water from the holy well of Zam Zam", 499, "/img/tohfaekhulus/zamzam.jpg", "Tohfa-E-Khulus");
initializeProduct("safa", "Big Rumal(Safa)", "Traditional prayer scarf made from premium quality fabric, perfect for maintaining modesty during prayers and religious activities", 150, "/img/tohfaekhulus/safa.jpeg", "Tohfa-E-Khulus");

//Men's Mini Kit
initializeProduct("mens-mini-kit-001", "Men's Umrah Mini Kit", "Essential collection of items for men performing Umrah, including Ihram, prayer cap, miswak, and other necessary items for a complete spiritual journey.", 1000, "/img/umrahminikit/men-kit.jpeg", "Men's Mini Kit");
initializeProduct("ehram-lock-button", "Ehram Lock Button", "Secure and durable lock button designed specifically for Ehram, ensuring your Ihram cloth stays properly fastened during your spiritual journey.", 30, "/img/men/ihram-lock-button.jpeg", "Men's Mini Kit");

//Women's Mini Kit
initializeProduct("womens-mini-kit-001", "Women's Umrah Mini Kit", "Essential collection of items for women performing Umrah, including prayer cap (Makhani), prayer scarf, hand gloves, and other necessary items for a complete spiritual journey.", 900, "/img/umrahminikit/women-kit.jpeg", "Women's Mini Kit");

//Individual Items
initializeProduct("ehram-terry", "Ehram (Terry)", "Complete Ehram set including two pieces of white, seamless cloth made from high-quality cotton.", 950, "/img/individual/ihram-terry.jpeg", "Individual Items");
initializeProduct("ehram-cotton", "Ehram (Cotton)", "A complete Ihram set for men, made from soft, breathable cotton for comfort during your pilgrimage.", 700, "/img/men/ihram-cloth.jpg", "Individual Items");
initializeProduct("prayer-cap-a", "Prayer Cap", "A traditional prayer cap, essential for prayers and religious observance.", 80, "/img/men/prayer-cap.jpg", "Individual Items");
initializeProduct("prayer-cap-b-sufi", "Prayer Cap (Sufi)", "A comfortable and simple prayer cap for daily use.", 60, "/img/individual/prayer-cap-b-sufi.jpeg", "Individual Items");
initializeProduct("prayer-cap-c-fancy", "Prayer Cap (Fancy)", "A lightweight and breathable prayer cap, suitable for all conditions.", 40, "/img/individual/prayer-cap-c-fancy.jpeg", "Individual Items");
initializeProduct("ittar-jannatul-firdos-3ml", "Ittar - Jannatul Firdos (3ml)", "A small bottle of classic Jannatul Firdos attar, a traditional non-alcoholic fragrance.", 25, "/img/individual/ittar-swiss-arabian-3ml.jpeg", "Individual Items");
initializeProduct("ittar-jannatul-firdos-6ml", "Ittar - Jannatul Firdos (6ml)", "A 6ml bottle of classic Jannatul Firdos attar, a traditional non-alcoholic fragrance.", 50, "/img/individual/ittar-Firdaus-6ml.jpeg", "Individual Items");
initializeProduct("ittar-majmua-6ml", "Ittar - Majmua (6ml)", "A 6ml bottle of Majmua attar, a rich and complex non-alcoholic fragrance.", 50, "/img/individual/ittar-majmua-6ml.jpeg", "Individual Items");
initializeProduct("hajj-guide", "Hajj Guide", "A comprehensive guide to performing the rituals of Hajj.", 100, "/img/individual/hajj-umrah-guide.jpeg", "Individual Items");
initializeProduct("umrah-guide", "Umrah Guide", "A handy guide to performing the rituals of Umrah.", 100, "/img/individual/umrah-guide.jpeg", "Individual Items");
initializeProduct("hajj-dua-card", "Hajj Dua Card", "Laminated cards with important Duas for Hajj.", 290, "/img/common/dua-cards.jpg", "Individual Items");
initializeProduct("umrah-dua-card", "Umrah Dua Card", "Laminated cards with important Duas for Umrah.", 190, "/img/individual/umrah-duas.jpeg", "Individual Items");
initializeProduct("tasbeeh-a", "Tasbeeh (Type A)", "Simple and elegant prayer beads for your daily dhikr.", 50, "/img/common/tasbeeh-a.jpg", "Individual Items");
initializeProduct("tasbeeh-b", "Tasbeeh (Type B)", "A set of quality prayer beads for keeping track of your recitations.", 80, "/img/individual/tasbeeh-b.jpeg", "Individual Items");
initializeProduct("tasbeeh-c", "Tasbeeh (Type C)", "Beautifully crafted prayer beads for an enhanced spiritual experience.", 120, "/img/individual/tasbeeh-c.jpeg", "Individual Items");
initializeProduct("tasbeeh-d", "Tasbeeh (Type D)", "Premium prayer beads with a distinct design for your spiritual practice.", 150, "/img/individual/tabeeh-d.jpeg", "Individual Items");

// Product with variants
if (!productRepository.existsById("ehram-belt-variant")) {
    Product ehramBelt = new Product();
    ehramBelt.setId("ehram-belt-variant");
    ehramBelt.setName("Ehram Belt");
    ehramBelt.setDescription("Adjustable belt to secure Ihram cloth in place, made from durable material with secure fastening.");
    ehramBelt.setImageUrl("/img/men/ihram-belt.jpg");
    ehramBelt.setCategory("Individual Items");
    ehramBelt.setPriceType("variant");
    ehramBelt.setVariants(Arrays.asList(
            new ProductVariant("Wafa", 200),
            new ProductVariant("Royal", 190)
    ));
    ehramBelt.setPrice(200); // Set a default price
    ehramBelt.setInStock(true);
    ehramBelt.setStockQuantity(100);
    productRepository.save(ehramBelt);
}

// Product with variants
if (!productRepository.existsById("lungi-ehram")) {
    Product lungiEhram = new Product();
    lungiEhram.setId("lungi-ehram");
    lungiEhram.setName("Lungi Ehram");
    lungiEhram.setDescription("A Lungi-style Ehram for comfort and ease of movement, available in multiple sizes.");
    lungiEhram.setImageUrl("/img/individual/lungi.jpeg");
    lungiEhram.setCategory("Individual Items");
    lungiEhram.setPriceType("variant");
    lungiEhram.setVariants(Arrays.asList(
        new ProductVariant("46\"", 490),
        new ProductVariant("50\"", 550),
        new ProductVariant("55\"", 590)
    ));
    lungiEhram.setPrice(490); // Set a default price
    lungiEhram.setInStock(true);
    lungiEhram.setStockQuantity(100);
    productRepository.save(lungiEhram);
}

initializeProduct("weighing-scale", "Weighing Scale", "A portable weighing scale to keep track of your luggage allowance.", 350, "/img/individual/weighing-portable-machine.jpeg", "Individual Items");
initializeProduct("patti-cap-bw", "Patti Cap Black & White (Ladies)", "A stylish black and white patti cap for ladies.", 50, "/img/individual/Ladies-patti-cap-black-n-white.jpeg", "Individual Items");
initializeProduct("kankar-bag", "Kankar ki bag", "A small bag for carrying stones (kankar) for the Jamarat ritual.", 45, "/img/individual/Kankar-ki-bag.jpeg", "Individual Items");
initializeProduct("hajj-mat-with-pillow", "Hajj Matt with pillow (Chatai)", "A comfortable prayer mat with an attached pillow, designed for Hajj.", 250, "/img/individual/Chatai-with-pillow.jpeg", "Individual Items");
initializeProduct("zamzam-empty-bottle-60ml", "Zamzam empty bottle (60 ml)", "An empty 60ml bottle for carrying Zamzam water.", 4, "/img/individual/Zamzam-60ml-bottle.jpeg", "Individual Items");
initializeProduct("plastic-pouch-4x6", "Plastic pouch for khajur pani 4x6 (100 nos)", "A pack of 100 plastic pouches, size 4x6, ideal for dates and water.", 150, "/img/individual/plastic-pouch-4x6.jpeg", "Individual Items");
initializeProduct("plastic-pouch-5x7", "Plastic pouch for khajur pani 5x7 (100 nos)", "A pack of 100 plastic pouches, size 5x7, ideal for dates and water.", 200, "/img/individual/plastic-pouch-5x7.jpeg", "Individual Items");
initializeProduct("ladies-cap-with-nakab", "Ladies Cap with nakab", "A ladies' cap with an attached niqab for convenience and modesty.", 150, "/img/individual/Ladies-cap-with-nakab.jpeg", "Individual Items");
initializeProduct("printed-cotton-makhani-half", "Printed Cotton Makhani (Half)", "A half-size printed cotton makhani for prayers.", 375, "/img/individual/Half-cotton-makhani.jpeg", "Individual Items");
initializeProduct("plain-lycra-makhani-half", "Plain Lycra Makhani (Half)", "A half-size plain lycra makhani, offering stretch and comfort.", 400, "/img/individual/Half-lycra-makhani-plain-colours.jpeg", "Individual Items");
initializeProduct("ladies-ehram-cotton-white", "Ladies ehram - cotton white", "A white cotton Ehram designed for ladies.", 350, "/img/individual/ladies-Cotton-ihram.jpeg", "Individual Items");
initializeProduct("ladies-ehram-swiss-cotton", "Ladies ehram - swiss cotton", "A premium Swiss cotton Ehram for ladies, offering superior comfort.", 365, "/img/individual/Ladies-ihram-swiss-cotton.jpeg", "Individual Items");
initializeProduct("ladies-ehram-hosiery", "Ladies ehram - hosiery", "A hosiery Ehram for ladies, providing flexibility and a snug fit.", 380, "/img/individual/hosiery-ladies-ihram.jpeg", "Individual Items");

// Kid's Ehram with variants
if (!productRepository.existsById("kids-ehram")) {
    Product kidsEhram = new Product();
    kidsEhram.setId("kids-ehram");
    kidsEhram.setName("Kid's Ehram");
    kidsEhram.setDescription("Comfortable and appropriately sized Ehram designed specifically for children, ensuring they can participate in religious activities with proper attire.");
    kidsEhram.setImageUrl("/img/individual/Kids-ihram.jpeg");
    kidsEhram.setCategory("Individual Items");
    kidsEhram.setPriceType("variant");
    kidsEhram.setVariants(Arrays.asList(
        new ProductVariant("Small", 320),
        new ProductVariant("Large", 450)
    ));
    kidsEhram.setPrice(320); // Set default price to the smaller variant
    kidsEhram.setInStock(true);
    kidsEhram.setStockQuantity(100);
    productRepository.save(kidsEhram);
}

// Janamaz
initializeJanamazProduct("janamaz-ibadat-15570", "Eshal Ibadat", "High-quality prayer mat.", 600, 7200, "70x110", "/img/janamaz/Eshal-Ibadat.png");
initializeJanamazProduct("janamaz-platinum-15571", "Eshal Platinum", "High-quality prayer mat.", 455, 5400, "70x110", "/img/janamaz/Eshal-Platinum.png");
initializeJanamazProduct("janamaz-gold-8570", "Eshal Gold", "High-quality prayer mat.", 250, 2950, "70x110", "/img/janamaz/Eshal-Gold.png");
initializeJanamazProduct("janamaz-silver-9170", "Eshal Silver", "High-quality prayer mat.", 200, 2380, "70x110", "/img/janamaz/Eshal-Silver1.png");
initializeJanamazProduct("janamaz-jasmin-8270", "Eshal Jasmin", "High-quality prayer mat.", 190, 2250, "70x110", "/img/janamaz/Eshal-Jasmin1.png");
initializeJanamazProduct("janamaz-lily-7970", "Eshal Lily", "High-quality prayer mat.", 210, 2500, "70x110", "/img/janamaz/Eshal-Lily1.png");
initializeJanamazProduct("janamaz-lotus-5870", "Eshal Lotus", "High-quality prayer mat.", 170, 2000, "70x110", "/img/janamaz/Eshal-Lotus1.png");

// Delete old lungi products if they exist
productRepository.deleteById("lungi-ehram-46");
productRepository.deleteById("ehram-belt-wafa");
productRepository.deleteById("ehram-belt-royal");
productRepository.deleteById("ehram-belt");

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

    private void initializeJanamazProduct(String id, String name, String description, double pricePerPiece, double pricePerDozen, String size, String imageUrl) {
        if (!productRepository.existsById(id)) {
            Product product = new Product();
            product.setId(id);
            product.setName(name);
            product.setDescription(description);
            product.setPrice(pricePerPiece); // Keep the original price field for backward compatibility
            product.setPricePerPiece(pricePerPiece);
            product.setPricePerDozen(pricePerDozen);
            product.setSize(size);
            product.setImageUrl(imageUrl);
            product.setCategory("Janamaz");
            product.setInStock(true);
            product.setStockQuantity(100);
            productRepository.save(product);
        }
    }
} 