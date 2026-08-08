package com.akarsha.chat.ai;

import com.akarsha.appointment.Appointment;
import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.appointment.AppointmentStatus;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.entity.User;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.customer.Customer;
import com.akarsha.customer.CustomerRepository;
import com.akarsha.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AiToolsService {

    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public AiToolsService(CustomerRepository customerRepository,
                          ServiceRepository serviceRepository,
                          UserRepository userRepository,
                          AppointmentRepository appointmentRepository) {
        this.customerRepository = customerRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public String getSalonInfo() {
        return "Salon " + TenantContext.getCurrentTenant() + " is open from 9 AM to 6 PM.";
    }

    @Transactional(readOnly = true)
    public String getServices() {
        List<ServiceEntity> services = serviceRepository.findAll();
        if (services.isEmpty()) {
            return "No services found.";
        }
        return services.stream()
                .map(s -> s.getName() + " - " + s.getDurationMinutes() + " mins - $" + s.getPrice())
                .collect(Collectors.joining("\n"));
    }

    @Transactional(readOnly = true)
    public String getCustomerAppointments(String phoneOrEmail) {
        // Need to find customer by phone or email
        Customer customer = customerRepository.findAll().stream()
                .filter(c -> phoneOrEmail.equals(c.getPhone()) || phoneOrEmail.equals(c.getEmail()))
                .findFirst()
                .orElse(null);

        if (customer == null) {
            return "Customer not found.";
        }

        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getCustomer().getId().equals(customer.getId()))
                .collect(Collectors.toList());

        if (appointments.isEmpty()) {
            return "No appointments found for this customer.";
        }

        return appointments.stream()
                .map(a -> "Appointment on " + a.getAppointmentDate() + " at " + a.getStartTime() + " for " + a.getService().getName() + " with " + a.getStaff().getFullName())
                .collect(Collectors.joining("\n"));
    }

    @Transactional(readOnly = true)
    public String checkAvailability(String serviceName, LocalDate date) {
        return "Checking availability for " + serviceName + " on " + date + " is not fully implemented in mock.";
    }
}
