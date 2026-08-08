package com.akarsha.core.platform;

import com.akarsha.core.entity.Salon;
import com.akarsha.core.repository.SalonRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/platform")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformController {

    private final PlatformService platformService;
    private final SalonRepository salonRepository;

    public PlatformController(PlatformService platformService, SalonRepository salonRepository) {
        this.platformService = platformService;
        this.salonRepository = salonRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<SuperAdminDashboardDTO> getDashboard() {
        return ResponseEntity.ok(platformService.getDashboardMetrics());
    }

    @GetMapping("/salons")
    public ResponseEntity<List<Salon>> getSalons() {
        return ResponseEntity.ok(salonRepository.findAll());
    }

    @PostMapping("/salons/{id}/suspend")
    public ResponseEntity<Void> suspendSalon(@PathVariable Long id) {
        platformService.suspendSalon(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/salons/{id}/reactivate")
    public ResponseEntity<Void> reactivateSalon(@PathVariable Long id) {
        platformService.reactivateSalon(id);
        return ResponseEntity.ok().build();
    }
}
