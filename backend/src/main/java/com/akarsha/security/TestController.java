package com.akarsha.security;

import com.akarsha.core.entity.User;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
public class TestController {

    private final UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<String>> getTenantUsers() {
        // This query is automatically filtered by TenantAspect and tenantFilter based on the TenantContext
        List<User> users = userRepository.findAll();
        List<String> usernames = users.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usernames);
    }

    @GetMapping("/tenant")
    public ResponseEntity<String> getTenantContext() {
        String tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(tenantId != null ? tenantId : "NULL");
    }
}
