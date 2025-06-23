package com.hajjumrah.service;

import com.hajjumrah.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OrderIdGenerator {

    @Autowired
    private OrderRepository orderRepository;

    private final Random random = new Random();

    /**
     * Generates a unique 6-digit order ID
     * @return unique 6-digit order ID as string
     */
    public String generateOrderId() {
        String orderId;
        do {
            // Generate a 6-digit number (100000 to 999999)
            int number = 100000 + random.nextInt(900000);
            orderId = String.valueOf(number);
        } while (orderRepository.existsByOrderId(orderId));
        
        return orderId;
    }
} 