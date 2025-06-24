package com.hajjumrah.service;

import com.hajjumrah.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${email.from.address}")
    private String fromEmail;

    @Value("${email.from.name}")
    private String fromName;

    @Value("${email.cc.address}")
    private String ccEmail;

    public void sendOrderConfirmationEmail(Order order) {
        try {
            System.out.println("Starting email sending process for order: " + (order.getOrderId() != null ? order.getOrderId() : order.getId()));
            
            if (order.getEmail() == null || order.getEmail().isEmpty()) {
                System.out.println("No email address found for order: " + order.getOrderId());
                return;
            }

            // Check if email is properly configured
            if (fromEmail == null || fromEmail.isEmpty() || fromEmail.equals("your-email@gmail.com")) {
                System.out.println("Email notification skipped - Email not configured. Order: " + order.getOrderId());
                return;
            }

            System.out.println("Email configuration looks good. From: " + fromEmail + ", To: " + order.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getEmail());
            
            // Add CC if configured
            if (ccEmail != null && !ccEmail.isEmpty() && !ccEmail.equals("your-email@gmail.com")) {
                helper.addCc(ccEmail);
                System.out.println("CC email added: " + ccEmail);
            }
            
            helper.setSubject("Order Confirmation - Order #" + (order.getOrderId() != null ? order.getOrderId() : order.getId()));

            // Create email content using Thymeleaf template
            System.out.println("Creating email content...");
            String emailContent;
            try {
                emailContent = createOrderConfirmationEmailContent(order);
                System.out.println("Email content created successfully. Length: " + emailContent.length());
            } catch (Exception e) {
                System.err.println("Error creating email content: " + e.getMessage());
                e.printStackTrace();
                return; // Don't proceed if template processing fails
            }
            
            helper.setText(emailContent, true);

            System.out.println("Sending email...");
            mailSender.send(message);
            System.out.println("Order confirmation email sent successfully to: " + order.getEmail());

        } catch (MessagingException e) {
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String createOrderConfirmationEmailContent(Order order) {
        Context context = new Context();
        
        // Add order data to template context
        context.setVariable("order", order);
        context.setVariable("orderId", order.getOrderId() != null ? order.getOrderId() : order.getId());
        context.setVariable("totalAmount", order.getTotalAmount());
        
        // Add shipping cost information with null checks
        Double subtotalAmount = order.getSubtotalAmount();
        Double shippingAmount = order.getShippingAmount();
        
        // If subtotalAmount is null (old orders), use totalAmount as fallback
        if (subtotalAmount == null) {
            subtotalAmount = order.getTotalAmount();
        }
        
        // If shippingAmount is null (old orders), set to 0
        if (shippingAmount == null) {
            shippingAmount = 0.0;
        }
        
        context.setVariable("subtotalAmount", subtotalAmount);
        context.setVariable("shippingAmount", shippingAmount);
        
        // Add transaction information
        context.setVariable("transactionId", order.getTransactionId());
        context.setVariable("paymentStatus", order.getPaymentStatus());
        
        // Format order items for email
        if (order.getItems() != null) {
            context.setVariable("orderItems", order.getItems());
        }
        
        // Format shipping address
        String shippingAddress = formatShippingAddress(order);
        context.setVariable("shippingAddress", shippingAddress);
        
        return templateEngine.process("email/order-confirmation", context);
    }

    private String formatShippingAddress(Order order) {
        StringBuilder address = new StringBuilder();
        
        if (order.getFullName() != null) {
            address.append(order.getFullName()).append("\n");
        }
        if (order.getFlatNo() != null) {
            address.append("Flat/Unit: ").append(order.getFlatNo()).append("\n");
        }
        if (order.getApartmentName() != null) {
            address.append("Apartment: ").append(order.getApartmentName()).append("\n");
        }
        if (order.getFloor() != null) {
            address.append("Floor: ").append(order.getFloor()).append("\n");
        }
        if (order.getStreetName() != null) {
            address.append(order.getStreetName()).append("\n");
        }
        if (order.getNearbyLandmark() != null) {
            address.append("Near: ").append(order.getNearbyLandmark()).append("\n");
        }
        if (order.getCity() != null) {
            address.append(order.getCity());
        }
        if (order.getState() != null) {
            address.append(", ").append(order.getState());
        }
        if (order.getPincode() != null) {
            address.append(" - ").append(order.getPincode());
        }
        if (order.getCountry() != null) {
            address.append("\n").append(order.getCountry());
        }
        
        return address.toString();
    }
} 