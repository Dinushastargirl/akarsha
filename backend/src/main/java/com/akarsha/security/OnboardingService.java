package com.akarsha.security;

import com.akarsha.core.entity.Salon;
import com.akarsha.core.entity.User;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.repository.SalonRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.tenant.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class OnboardingService {

    private final UserRepository userRepository;
    private final SalonRepository salonRepository;
    private final ServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public OnboardingService(UserRepository userRepository, 
                             SalonRepository salonRepository,
                             ServiceRepository serviceRepository,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService) {
        this.userRepository = userRepository;
        this.salonRepository = salonRepository;
        this.serviceRepository = serviceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String registerUser(String fullName, String email, String password) {
        // Validate inputs
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("All fields are required");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Global check for email uniqueness
        TenantContext.setCurrentTenant("SYSTEM_BYPASS");
        try {
            if (userRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("Email is already registered");
            }

            User user = new User();
            user.setFullName(fullName);
            user.setUsername(email);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole("SALON_OWNER");
            user.setTenantId(null); // Null until salon is created

            userRepository.save(user);

            // Generate initial onboarding token
            return jwtService.generateToken(user.getEmail(), null, user.getRole());
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public String createSalon(String userEmail, String name, String subdomain, String phone, String address, String city, String businessType) {
        if (name == null || name.trim().isEmpty() ||
            subdomain == null || subdomain.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            address == null || address.trim().isEmpty() ||
            city == null || city.trim().isEmpty() ||
            businessType == null || businessType.trim().isEmpty()) {
            throw new IllegalArgumentException("All fields are required");
        }

        // Subdomains must be alphanumeric
        if (!subdomain.matches("^[a-zA-Z0-9-]+$")) {
            throw new IllegalArgumentException("Subdomain must contain only letters, numbers, or hyphens");
        }

        TenantContext.setCurrentTenant("SYSTEM_BYPASS");
        try {
            if (salonRepository.findBySubdomain(subdomain).isPresent()) {
                throw new IllegalArgumentException("Subdomain is already taken");
            }

            // Create new Salon
            Salon salon = new Salon();
            salon.setName(name);
            salon.setSubdomain(subdomain);
            salon.setPhone(phone);
            salon.setAddress(address);
            salon.setCity(city);
            salon.setBusinessType(businessType);
            salonRepository.save(salon);

            // Associate user with this salon/tenant
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                throw new IllegalStateException("User context not found");
            }
            User user = userOpt.get();
            user.setTenantId(subdomain);
            userRepository.save(user);

            // Return fresh JWT token containing the new tenant ID claim
            return jwtService.generateToken(user.getEmail(), subdomain, user.getRole());
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public void setupSalon(String tenantId, String openingTime, String closingTime,
                           String serviceName, BigDecimal servicePrice, Integer serviceDuration,
                           String staffName, String staffEmail) {
        
        // 1. Update Salon hours and mark setup completed
        TenantContext.setCurrentTenant("SYSTEM_BYPASS");
        try {
            Optional<Salon> salonOpt = salonRepository.findBySubdomain(tenantId);
            if (salonOpt.isEmpty()) {
                throw new IllegalArgumentException("Salon not found");
            }
            Salon salon = salonOpt.get();
            salon.setOpeningTime(openingTime);
            salon.setClosingTime(closingTime);
            salon.setSetupCompleted(true);
            salonRepository.save(salon);
        } finally {
            TenantContext.clear();
        }

        // Set active tenant for tenant-aware operations
        TenantContext.setCurrentTenant(tenantId);
        try {
            // 2. Add first service (optional)
            if (serviceName != null && !serviceName.trim().isEmpty() && servicePrice != null && serviceDuration != null) {
                ServiceEntity service = new ServiceEntity();
                service.setName(serviceName);
                service.setPrice(servicePrice);
                service.setDurationMinutes(serviceDuration);
                serviceRepository.save(service);
            }

            // 3. Add first staff member (optional)
            if (staffName != null && !staffName.trim().isEmpty() && staffEmail != null && !staffEmail.trim().isEmpty()) {
                // Ensure email uniqueness globally
                TenantContext.setCurrentTenant("SYSTEM_BYPASS");
                boolean emailTaken = userRepository.findByEmail(staffEmail).isPresent();
                TenantContext.setCurrentTenant(tenantId); // Restore tenant context

                if (emailTaken) {
                    throw new IllegalArgumentException("Staff email is already registered");
                }

                User staff = new User();
                staff.setFullName(staffName);
                staff.setUsername(staffEmail);
                staff.setEmail(staffEmail);
                staff.setPasswordHash(passwordEncoder.encode("TemporaryStaffPassword123!"));
                staff.setRole("STAFF");
                staff.setTenantId(tenantId);
                userRepository.save(staff);
            }
        } finally {
            TenantContext.clear();
        }
    }
}
