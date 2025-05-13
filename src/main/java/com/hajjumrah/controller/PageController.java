package com.hajjumrah.controller;

import com.hajjumrah.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/mens-kit")
    public String mensKit() {
        return "mens-kit";
    }

    @GetMapping("/womens-kit")
    public String womensKit() {
        return "womens-kit";
    }

    @GetMapping("/individual")
    public String individual(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "individual";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }
} 