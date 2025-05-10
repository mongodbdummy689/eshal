package com.hajjumrah.controller;

import com.hajjumrah.model.User;
import com.hajjumrah.repository.UserRepository;
import com.hajjumrah.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Boolean> response = new HashMap<>();
        
        // Check both session and SecurityContext
        boolean isAuthenticated = auth != null && 
                                auth.isAuthenticated() && 
                                !auth.getName().equals("anonymousUser") &&
                                session.getAttribute("SPRING_SECURITY_CONTEXT") != null;
        
        response.put("authenticated", isAuthenticated);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        String password = request.get("password");
        String fullName = request.get("fullName");
        String mobileNumber = request.get("mobileNumber");

        // Validate input
        if (email == null || email.trim().isEmpty()) {
            response.put("error", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (password == null || password.trim().isEmpty()) {
            response.put("error", "Password is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            response.put("error", "Full name is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            response.put("error", "Mobile number is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (!mobileNumber.matches("^[0-9]{10}$")) {
            response.put("error", "Invalid mobile number format. Please enter a 10-digit number.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            response.put("error", "Email already exists");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if mobile number already exists
        if (userRepository.existsByMobileNumber(mobileNumber)) {
            response.put("error", "Mobile number already exists");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Create new user
            User user = new User();
            user.setEmail(email.trim());
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName.trim());
            user.setMobileNumber(mobileNumber.trim());
            user.setRole("USER");

            // Save user to database
            User savedUser = userRepository.save(user);
            
            response.put("message", "Registration successful");
            response.put("userId", savedUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Set session attributes
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            session.setAttribute("user", userDetails);
            
            return ResponseEntity.ok(new AuthResponse(
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList())
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid email or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return ResponseEntity.ok(new AuthResponse(
            userDetails.getUsername(),
            userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        ));
    }
}

class LoginRequest {
    private String email;
    private String password;

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class AuthResponse {
    private String username;
    private List<String> roles;

    public AuthResponse(String username, List<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}

class ErrorResponse {
    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    // Getters and setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
} 