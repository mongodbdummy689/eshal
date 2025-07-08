package com.hajjumrah.controller;

import com.hajjumrah.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("images", imageRepository.findAll());
        return "home";
    }
    
    @GetMapping("/terms-and-conditions")
    public String termsAndConditions() {
        return "terms-and-conditions";
    }
    
    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy";
    }
    
    @GetMapping("/shipping-policy")
    public String shippingPolicy() {
        return "shipping-policy";
    }
    
    @GetMapping("/contact-us")
    public String contactUs() {
        return "contact-us";
    }
    
    @GetMapping("/cancellation-refunds")
    public String cancellationRefunds() {
        return "cancellation-refunds";
    }
} 