package com.akarsha.core.controller;

import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;

    public ServiceController(ServiceRepository serviceRepository, AppointmentRepository appointmentRepository) {
        this.serviceRepository = serviceRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping
    public ResponseEntity<Page<ServiceEntity>> getServices(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        // Always pass non-null string — empty string means "no filter" per the JPQL query sentinel
        String queryParam = (query != null && !query.trim().isEmpty()) ? query.trim() : "";
        return ResponseEntity.ok(serviceRepository.searchServices(queryParam, active, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntity> getServiceById(@PathVariable Long id) {
        Optional<ServiceEntity> opt = serviceRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ServiceEntity svc = opt.get();
        String tenant = TenantContext.getCurrentTenant();
        if (tenant != null && !tenant.equals(svc.getTenantId())) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(svc);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> createService(@RequestBody ServiceRequest request) {
        String tenant = TenantContext.getCurrentTenant();

        // Validation
        if (request.getName() == null || request.getName().trim().isEmpty())
            return ResponseEntity.badRequest().body("Service name is required");
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0)
            return ResponseEntity.badRequest().body("Price must be zero or greater");
        if (request.getDurationMinutes() <= 0)
            return ResponseEntity.badRequest().body("Duration must be greater than zero");

        ServiceEntity svc = new ServiceEntity();
        svc.setName(request.getName().trim());
        svc.setPrice(request.getPrice());
        svc.setDurationMinutes(request.getDurationMinutes());
        svc.setActive(request.isActive());
        svc.setTenantId(tenant);

        return ResponseEntity.status(HttpStatus.CREATED).body(serviceRepository.save(svc));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> updateService(@PathVariable Long id, @RequestBody ServiceRequest request) {
        Optional<ServiceEntity> opt = serviceRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ServiceEntity svc = opt.get();
        String tenant = TenantContext.getCurrentTenant();
        if (tenant != null && !tenant.equals(svc.getTenantId())) return ResponseEntity.notFound().build();

        if (request.getName() == null || request.getName().trim().isEmpty())
            return ResponseEntity.badRequest().body("Service name is required");
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0)
            return ResponseEntity.badRequest().body("Price must be zero or greater");
        if (request.getDurationMinutes() <= 0)
            return ResponseEntity.badRequest().body("Duration must be greater than zero");

        svc.setName(request.getName().trim());
        svc.setPrice(request.getPrice());
        svc.setDurationMinutes(request.getDurationMinutes());
        svc.setActive(request.isActive());

        return ResponseEntity.ok(serviceRepository.save(svc));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> patchStatus(@PathVariable Long id, @RequestParam boolean active) {
        Optional<ServiceEntity> opt = serviceRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ServiceEntity svc = opt.get();
        String tenant = TenantContext.getCurrentTenant();
        if (tenant != null && !tenant.equals(svc.getTenantId())) return ResponseEntity.notFound().build();

        svc.setActive(active);
        return ResponseEntity.ok(serviceRepository.save(svc));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER')")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        Optional<ServiceEntity> opt = serviceRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ServiceEntity svc = opt.get();
        String tenant = TenantContext.getCurrentTenant();
        if (tenant != null && !tenant.equals(svc.getTenantId())) return ResponseEntity.notFound().build();

        // If service has appointment history, archive (deactivate) rather than delete
        if (appointmentRepository.existsByServiceId(id)) {
            svc.setActive(false);
            serviceRepository.save(svc);
            return ResponseEntity.ok("Service archived — it has appointment history and cannot be permanently deleted.");
        }

        serviceRepository.delete(svc);
        return ResponseEntity.ok("Service deleted successfully.");
    }
}
