package com.hajjumrah.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/mens-kit")
    public String mensKit() {
        return "mens-kit";
    }

    @GetMapping("/womens-kit")
    public String womensKit() {
        return "womens-kit";
    }

    @GetMapping("/individual")
    public String individual() {
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