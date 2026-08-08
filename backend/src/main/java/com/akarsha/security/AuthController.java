package com.akarsha.security;

import com.akarsha.core.entity.User;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final OnboardingService onboardingService;

    public AuthController(UserRepository userRepository, 
                          JwtService jwtService, 
                          PasswordEncoder passwordEncoder,
                          OnboardingService onboardingService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.onboardingService = onboardingService;
    }

    @PostMapping("/public/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Query database globally using SYSTEM_BYPASS context to look up user across all tenants
        TenantContext.setCurrentTenant("SYSTEM_BYPASS");
        try {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Validate password
                if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                    String token = jwtService.generateToken(user.getEmail(), user.getTenantId(), user.getRole());
                    return ResponseEntity.ok(new AuthResponse(token));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email, password, or tenant");
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/public/auth/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            String token = onboardingService.registerUser(request.getFullName(), request.getEmail(), request.getPassword());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/onboarding/create-salon")
    public ResponseEntity<?> createSalon(@RequestBody CreateSalonRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            String token = onboardingService.createSalon(
                    email, 
                    request.getName(), 
                    request.getSubdomain(), 
                    request.getPhone(), 
                    request.getAddress(), 
                    request.getCity(), 
                    request.getBusinessType()
            );
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/onboarding/setup")
    public ResponseEntity<?> setupSalon(@RequestBody SetupSalonRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Lookup authenticated user's tenant context globally
        TenantContext.setCurrentTenant("SYSTEM_BYPASS");
        String tenantId;
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty() || userOpt.get().getTenantId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tenant context not initialized. Create a salon first.");
            }
            tenantId = userOpt.get().getTenantId();
        } finally {
            TenantContext.clear();
        }

        try {
            onboardingService.setupSalon(
                    tenantId,
                    request.getOpeningTime(),
                    request.getClosingTime(),
                    request.getFirstServiceName(),
                    request.getFirstServicePrice(),
                    request.getFirstServiceDuration(),
                    request.getFirstStaffName(),
                    request.getFirstStaffEmail()
            );
            return ResponseEntity.ok(Map.of("message", "Salon setup completed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // DTO Classes
    public static class LoginRequest {
        private String email;
        private String password;
        private String tenantId;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    }

    public static class SignupRequest {
        private String fullName;
        private String email;
        private String password;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class CreateSalonRequest {
        private String name;
        private String subdomain;
        private String phone;
        private String address;
        private String city;
        private String businessType;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSubdomain() { return subdomain; }
        public void setSubdomain(String subdomain) { this.subdomain = subdomain; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
    }

    public static class SetupSalonRequest {
        private String openingTime;
        private String closingTime;
        private String firstServiceName;
        private BigDecimal firstServicePrice;
        private Integer firstServiceDuration;
        private String firstStaffName;
        private String firstStaffEmail;

        public String getOpeningTime() { return openingTime; }
        public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }
        public String getClosingTime() { return closingTime; }
        public void setClosingTime(String closingTime) { this.closingTime = closingTime; }
        public String getFirstServiceName() { return firstServiceName; }
        public void setFirstServiceName(String firstServiceName) { this.firstServiceName = firstServiceName; }
        public BigDecimal getFirstServicePrice() { return firstServicePrice; }
        public void setFirstServicePrice(BigDecimal firstServicePrice) { this.firstServicePrice = firstServicePrice; }
        public Integer getFirstServiceDuration() { return firstServiceDuration; }
        public void setFirstServiceDuration(Integer firstServiceDuration) { this.firstServiceDuration = firstServiceDuration; }
        public String getFirstStaffName() { return firstStaffName; }
        public void setFirstStaffName(String firstStaffName) { this.firstStaffName = firstStaffName; }
        public String getFirstStaffEmail() { return firstStaffEmail; }
        public void setFirstStaffEmail(String firstStaffEmail) { this.firstStaffEmail = firstStaffEmail; }
    }

    public static class AuthResponse {
        private final String token;

        public AuthResponse(String token) {
            this.token = token;
        }

        public String getToken() { return token; }
    }
}
