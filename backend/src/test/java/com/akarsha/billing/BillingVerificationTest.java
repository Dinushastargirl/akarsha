package com.akarsha.billing;

import com.akarsha.appointment.Appointment;
import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.appointment.AppointmentStatus;
import com.akarsha.customer.Customer;
import com.akarsha.customer.CustomerRepository;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.entity.User;
import com.akarsha.core.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.akarsha.tenant.TenantContext;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BillingVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.akarsha.security.JwtService jwtService;

    private String token;
    private Appointment savedAppointment;
    private Customer savedCustomer;
    private User staff;
    private ServiceEntity service;
    private final String TENANT_ID = "tenant-test";

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(TENANT_ID);
        paymentRepository.deleteAll();
        invoiceRepository.deleteAll();
        appointmentRepository.deleteAll();
        customerRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();

        // Create user
        User owner = new User();
        owner.setEmail("owner@billingtest.com");
        owner.setUsername("owner@billingtest.com");
        owner.setPasswordHash(passwordEncoder.encode("password123"));
        owner.setFullName("Test Owner");
        owner.setRole("SALON_OWNER");
        owner.setTenantId(TENANT_ID);
        owner.setActive(true);
        userRepository.save(owner);

        token = "Bearer " + jwtService.generateToken(owner.getEmail(), owner.getTenantId(), owner.getRole());

        // Create Staff
        staff = new User();
        staff.setEmail("staff@billingtest.com");
        staff.setUsername("staff@billingtest.com");
        staff.setPasswordHash(passwordEncoder.encode("password123"));
        staff.setFullName("Test Staff");
        staff.setRole("STAFF");
        staff.setTenantId(TENANT_ID);
        staff.setActive(true);
        staff = userRepository.save(staff);

        // Create Customer
        Customer c = new Customer();
        c.setFullName("Billing Test Customer");
        c.setPhone("1234567890");
        c.setTenantId(TENANT_ID);
        savedCustomer = customerRepository.save(c);

        // Create Service
        ServiceEntity s = new ServiceEntity();
        s.setName("Haircut");
        s.setDurationMinutes(30);
        s.setPrice(new BigDecimal("25.00"));
        s.setTenantId(TENANT_ID);
        s.setActive(true);
        service = serviceRepository.save(s);

        // Create Appointment
        Appointment a = new Appointment();
        a.setCustomer(savedCustomer);
        a.setService(service);
        a.setStaff(staff);
        a.setAppointmentDate(LocalDate.now());
        a.setStartTime(LocalTime.of(10, 0));
        a.setEndTime(LocalTime.of(10, 30));
        a.setStatus(AppointmentStatus.BOOKED);
        a.setTenantId(TENANT_ID);
        savedAppointment = appointmentRepository.save(a);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testCannotManuallyPatchToCompleted() throws Exception {
        mockMvc.perform(patch("/api/v1/appointments/" + savedAppointment.getId() + "/status")
                .param("status", "COMPLETED")
                .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Status cannot be manually set to COMPLETED. Use the checkout endpoint instead."));
        
        Appointment a = appointmentRepository.findById(savedAppointment.getId()).get();
        assertEquals(AppointmentStatus.BOOKED, a.getStatus());
    }

    @Test
    void testSuccessfulCheckout() throws Exception {
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentMethod(PaymentMethod.CARD);
        request.setTaxAmount(new BigDecimal("2.50"));
        request.setDiscountAmount(BigDecimal.ZERO);
        request.setNotes("Great service");

        CheckoutLineItemRequest item1 = new CheckoutLineItemRequest();
        item1.setItemType(ItemType.SERVICE);
        item1.setReferenceId(service.getId());
        item1.setDescription("Haircut");
        item1.setQuantity(1);
        item1.setUnitPrice(new BigDecimal("25.00"));

        request.setLineItems(List.of(item1));

        String res = mockMvc.perform(post("/api/v1/checkout/appointment/" + savedAppointment.getId())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Invoice invoice = objectMapper.readValue(res, Invoice.class);
        assertNotNull(invoice.getId());
        assertEquals(new BigDecimal("25.00"), invoice.getSubtotal());
        assertEquals(new BigDecimal("27.50"), invoice.getTotalAmount());
        
        // Restore context after MockMvc clears it
        TenantContext.setCurrentTenant(TENANT_ID);

        // Verify appointment status
        Appointment a = appointmentRepository.findById(savedAppointment.getId()).get();
        assertEquals(AppointmentStatus.COMPLETED, a.getStatus());

        // Verify payment creation
        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
        assertEquals(1, payments.size());
        assertEquals(PaymentMethod.CARD, payments.get(0).getPaymentMethod());
        assertEquals(new BigDecimal("27.50"), payments.get(0).getAmount());
    }
}
