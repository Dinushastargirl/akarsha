package com.akarsha.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

import com.akarsha.appointment.AppointmentRepository;
import java.math.BigDecimal;
import java.util.List;

import com.akarsha.billing.Invoice;
import com.akarsha.billing.InvoiceRepository;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;

    public CustomerController(CustomerRepository customerRepository, AppointmentRepository appointmentRepository, InvoiceRepository invoiceRepository) {
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Customer>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<Customer> customers = customerRepository.findAll(pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Customer customer = customerOpt.get();
        String currentTenant = com.akarsha.tenant.TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(customer.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Customer>> searchCustomers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<Customer> results = customerRepository.findByFullNameContainingIgnoreCaseOrPhoneContaining(query, query, pageable);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<?> createCustomer(@RequestBody Customer request) {
        String validationError = validateCustomer(request);
        if (validationError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
        }

        Customer customer = new Customer();
        customer.setFullName(request.getFullName().trim());
        customer.setPhone(request.getPhone().trim());
        customer.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        customer.setBirthday(request.getBirthday());
        customer.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        Customer saved = customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody Customer request) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }
        Customer customer = customerOpt.get();
        String currentTenant = com.akarsha.tenant.TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(customer.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }

        String validationError = validateCustomer(request);
        if (validationError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
        }

        customer.setFullName(request.getFullName().trim());
        customer.setPhone(request.getPhone().trim());
        customer.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        customer.setBirthday(request.getBirthday());
        customer.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        Customer saved = customerRepository.save(customer);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER', 'RECEPTIONIST')")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }
        Customer customer = customerOpt.get();
        String currentTenant = com.akarsha.tenant.TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(customer.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }
        customerRepository.delete(customer);
        return ResponseEntity.ok("Customer deleted successfully");
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getCustomerStats(@PathVariable Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }
        Customer customer = customerOpt.get();
        String currentTenant = com.akarsha.tenant.TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(customer.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }

        long totalVisits = appointmentRepository.countTotalVisitsByCustomerId(id);
        long completedVisits = appointmentRepository.countCompletedVisitsByCustomerId(id);
        long noShowCount = appointmentRepository.countNoShowsByCustomerId(id);
        List<Invoice> invoices = invoiceRepository.findByCustomerIdOrderByCreatedAtDesc(id);
        BigDecimal totalRevenue = invoices.stream()
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate lastVisitDate = appointmentRepository.findLastVisitDateByCustomerId(id);

        CustomerStats stats = new CustomerStats(totalVisits, completedVisits, noShowCount, totalRevenue, lastVisitDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<?> getCustomerInvoices(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }
        Customer customer = customerOpt.get();
        String currentTenant = com.akarsha.tenant.TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(customer.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Invoice> invoices = invoiceRepository.findByCustomerId(id, pageable);
        return ResponseEntity.ok(invoices);
    }


    private String validateCustomer(Customer customer) {
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            return "Name is required";
        }
        if (customer.getFullName().length() > 100) {
            return "Name must be under 100 characters";
        }
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            return "Phone number is required";
        }
        // Validate phone format: digits, spaces, hyphens, and plus signs allowed
        if (!customer.getPhone().matches("^[0-9+ -]{7,20}$")) {
            return "Invalid phone number format";
        }
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (!customer.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return "Invalid email address format";
            }
        }
        return null;
    }
}
