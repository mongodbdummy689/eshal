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

    @GetMapping("/status")
    public ResponseEntity<?> checkAuthStatus(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        
        boolean isAuthenticated = auth != null && 
                                auth.isAuthenticated() && 
                                !auth.getName().equals("anonymousUser") &&
                                session.getAttribute("SPRING_SECURITY_CONTEXT") != null;
        
        response.put("authenticated", isAuthenticated);
        
        if (isAuthenticated) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                response.put("user", Map.of(
                    "email", user.getEmail(),
                    "role", user.getRole()
                ));
            }
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(HttpSession session) {
        return checkAuthStatus(session);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        String password = request.get("password");
        String fullName = request.get("fullName");
        String mobileNumber = request.get("mobileNumber");
        
        // Shipping address fields
        String flatNo = request.get("flatNo");
        String apartmentName = request.get("apartmentName");
        String floor = request.get("floor");
        String streetName = request.get("streetName");
        String nearbyLandmark = request.get("nearbyLandmark");
        String city = request.get("city");
        String state = request.get("state");
        String country = request.get("country");
        String pincode = request.get("pincode");

        // Validate required fields
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
        
        // Validate shipping address fields
        if (flatNo == null || flatNo.trim().isEmpty()) {
            response.put("error", "Flat/Unit number is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (apartmentName == null || apartmentName.trim().isEmpty()) {
            response.put("error", "Apartment/Building name is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (floor == null || floor.trim().isEmpty()) {
            response.put("error", "Floor is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (streetName == null || streetName.trim().isEmpty()) {
            response.put("error", "Street name is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (nearbyLandmark == null || nearbyLandmark.trim().isEmpty()) {
            response.put("error", "Nearby landmark is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (city == null || city.trim().isEmpty()) {
            response.put("error", "City is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (state == null || state.trim().isEmpty()) {
            response.put("error", "State is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (country == null || country.trim().isEmpty()) {
            response.put("error", "Country is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (pincode == null || pincode.trim().isEmpty()) {
            response.put("error", "Pincode is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (!pincode.matches("^[0-9]{6}$")) {
            response.put("error", "Invalid pincode format. Please enter a 6-digit number.");
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
            
            // Set shipping address fields
            user.setFlatNo(flatNo.trim());
            user.setApartmentName(apartmentName.trim());
            user.setFloor(floor.trim());
            user.setStreetName(streetName.trim());
            user.setNearbyLandmark(nearbyLandmark.trim());
            user.setCity(city.trim());
            user.setState(state.trim());
            user.setCountry(country.trim());
            user.setPincode(pincode.trim());

            // Debug: Print user details (without password)
            System.out.println("Registering user: " + user.getEmail() + ", role: " + user.getRole());
            System.out.println("Password encoded: " + (user.getPassword() != null && user.getPassword().startsWith("$2a$")));

            // Save user to database
            User savedUser = userRepository.save(user);
            
            System.out.println("User registered successfully with ID: " + savedUser.getId());
            
            response.put("message", "Registration successful");
            response.put("userId", savedUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            System.out.println("Login attempt for email: " + loginRequest.getEmail());
            
            // Validate input
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                System.out.println("Login failed: Email is empty");
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Email is required"));
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                System.out.println("Login failed: Password is empty");
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Password is required"));
            }

            // Check if user exists
            User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
            if (user == null) {
                System.out.println("Login failed: User not found with email: " + loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
            }

            System.out.println("User found: " + user.getEmail() + ", role: " + user.getRole());
            System.out.println("Stored password starts with $2a$: " + (user.getPassword() != null && user.getPassword().startsWith("$2a$")));

            // Attempt authentication
            System.out.println("Attempting authentication...");
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            System.out.println("Authentication successful for: " + authentication.getName());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Set session attributes
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            session.setAttribute("user", userDetails);
            
            System.out.println("Session attributes set, returning success response");
            
            return ResponseEntity.ok(new AuthResponse(
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList())
            ));
        } catch (BadCredentialsException e) {
            System.out.println("Login failed: Bad credentials for email: " + loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid email or password"));
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Return complete user data including shipping address
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getFullName());
        userData.put("mobileNumber", user.getMobileNumber());
        userData.put("role", user.getRole());
        userData.put("flatNo", user.getFlatNo());
        userData.put("apartmentName", user.getApartmentName());
        userData.put("floor", user.getFloor());
        userData.put("streetName", user.getStreetName());
        userData.put("nearbyLandmark", user.getNearbyLandmark());
        userData.put("city", user.getCity());
        userData.put("state", user.getState());
        userData.put("country", user.getCountry());
        userData.put("pincode", user.getPincode());

        // Debug: Print user data being returned
        System.out.println("Returning user data for: " + user.getEmail());
        System.out.println("Shipping address fields:");
        System.out.println("  flatNo: " + user.getFlatNo());
        System.out.println("  apartmentName: " + user.getApartmentName());
        System.out.println("  floor: " + user.getFloor());
        System.out.println("  streetName: " + user.getStreetName());
        System.out.println("  nearbyLandmark: " + user.getNearbyLandmark());
        System.out.println("  city: " + user.getCity());
        System.out.println("  state: " + user.getState());
        System.out.println("  country: " + user.getCountry());
        System.out.println("  pincode: " + user.getPincode());

        return ResponseEntity.ok(userData);
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