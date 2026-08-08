package com.akarsha.core.config;

import com.akarsha.core.entity.User;
import com.akarsha.core.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"test", "demo"})
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        updatePassword("owner@alpha.com", "Owner123!");
        updatePassword("manager@alpha.com", "Manager123!");
        updatePassword("receptionist@alpha.com", "Receptionist123!");
        updatePassword("staff@alpha.com", "Staff123!");
    }

    private void updatePassword(String email, String newPassword) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            System.out.println("Updated password for demo account: " + email);
        });
    }
}
