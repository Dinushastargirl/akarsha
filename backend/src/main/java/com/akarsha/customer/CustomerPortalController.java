package com.akarsha.customer;

import com.akarsha.appointment.Appointment;
import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.appointment.AppointmentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/portal")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerPortalController {

    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;

    public CustomerPortalController(CustomerRepository customerRepository, AppointmentRepository appointmentRepository) {
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments() {
        String emailOrPhone = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Find customer by phone or email. 
        // JwtService uses getPhone() as subject during login.
        List<Customer> customers = customerRepository.findByPhone(emailOrPhone);
        if (customers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Customer customer = customers.get(0);
        List<Appointment> appointments = appointmentRepository.findByCustomerIdOrderByAppointmentDateDesc(customer.getId());
        
        return ResponseEntity.ok(appointments);
    }
    
    @PostMapping("/my-appointments/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Customer> customers = customerRepository.findByPhone(phone);
        if (customers.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        Optional<Appointment> appOpt = appointmentRepository.findById(id);
        if (appOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        Appointment appt = appOpt.get();
        if (!appt.getCustomer().getId().equals(customers.get(0).getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (appt.getStatus() == AppointmentStatus.CANCELLED || appt.getStatus() == AppointmentStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Cannot cancel an appointment that is already " + appt.getStatus());
        }
        
        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appt);
        
        return ResponseEntity.ok("Appointment cancelled");
    }
}
