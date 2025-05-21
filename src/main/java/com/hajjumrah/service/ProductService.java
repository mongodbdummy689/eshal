package com.hajjumrah.service;

import com.hajjumrah.model.Product;
import com.hajjumrah.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getProductsByCategory(String category) {
        logger.info("Fetching products for category: {}", category);
        List<Product> products = productRepository.findByCategory(category);
        logger.info("Found {} products for category {}", products.size(), category);
        return products;
    }

    public Product getProductById(String id) {
        logger.info("Fetching product with id: {}", id);
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            logger.info("Found product: {}", product.get().getName());
        } else {
            logger.warn("No product found with id: {}", id);
        }
        return product.orElse(null);
    }
} 