package com.akarsha.dashboard;

import com.akarsha.appointment.Appointment;
import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.appointment.AppointmentStatus;
import com.akarsha.core.entity.Salon;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.entity.User;
import com.akarsha.core.repository.SalonRepository;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.customer.Customer;
import com.akarsha.customer.CustomerRepository;
import com.akarsha.security.JwtService;
import com.akarsha.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class DashboardVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Salon salonAlpha;
    private Salon salonBeta;
    private User staffAlpha;
    private ServiceEntity serviceAlpha;
    private Customer customerAlpha;

    @BeforeEach
    public void setUp() {
        salonAlpha = new Salon();
        salonAlpha.setName("Salon Alpha");
        salonAlpha.setSubdomain("dash-alpha-" + UUID.randomUUID().toString().substring(0, 5));
        salonAlpha.setPhone("0112222222");
        salonAlpha.setAddress("Colombo 3");
        salonAlpha.setCity("Colombo");
        salonAlpha.setBusinessType("Salon");
        salonAlpha = salonRepository.save(salonAlpha);

        salonBeta = new Salon();
        salonBeta.setName("Salon Beta");
        salonBeta.setSubdomain("dash-beta-" + UUID.randomUUID().toString().substring(0, 5));
        salonBeta.setPhone("0113333333");
        salonBeta.setAddress("Kandy");
        salonBeta.setCity("Kandy");
        salonBeta.setBusinessType("Salon");
        salonBeta = salonRepository.save(salonBeta);

        // Staff for Alpha (User does not require TenantContext pre-persist)
        staffAlpha = new User();
        staffAlpha.setFullName("Priya Alpha");
        staffAlpha.setUsername("priya_alpha_" + UUID.randomUUID().toString().substring(0, 5));
        staffAlpha.setEmail("priya_" + UUID.randomUUID().toString().substring(0, 5) + "@alpha.com");
        staffAlpha.setPasswordHash("hash");
        staffAlpha.setRole("STAFF");
        staffAlpha.setActive(true);
        staffAlpha.setTenantId(salonAlpha.getSubdomain());
        staffAlpha = userRepository.save(staffAlpha);

        // Service + Customer require TenantContext for TenantEntityListener
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());

        serviceAlpha = new ServiceEntity();
        serviceAlpha.setName("Haircut");
        serviceAlpha.setPrice(BigDecimal.valueOf(1500));
        serviceAlpha.setDurationMinutes(30);
        serviceAlpha.setActive(true);
        serviceAlpha.setTenantId(salonAlpha.getSubdomain());
        serviceAlpha = serviceRepository.save(serviceAlpha);

        customerAlpha = new Customer();
        customerAlpha.setFullName("Nilmini Alpha");
        customerAlpha.setPhone("0771234567");
        customerAlpha.setTenantId(salonAlpha.getSubdomain());
        customerAlpha = customerRepository.save(customerAlpha);

        TenantContext.clear();
    }

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    private String auth(String email, String tenantId) {
        return "Bearer " + jwtService.generateToken(email, tenantId, "SALON_OWNER");
    }

    /** Create a simple appointment with a given status, dated today. */
    private Appointment makeAppointment(AppointmentStatus status, LocalTime start, LocalTime end) {
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        Appointment a = new Appointment();
        a.setCustomer(customerAlpha);
        a.setService(serviceAlpha);
        a.setStaff(staffAlpha);
        a.setAppointmentDate(LocalDate.now());
        a.setStartTime(start);
        a.setEndTime(end);
        a.setStatus(status);
        a.setTenantId(salonAlpha.getSubdomain());
        Appointment saved = appointmentRepository.save(a);
        TenantContext.clear();
        return saved;
    }

    // ─── 1. EMPTY DASHBOARD ──────────────────────────────────────────────────

    @Test
    public void testEmptyDashboard() throws Exception {
        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@alpha.com", salonAlpha.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayTotal").value(0))
                .andExpect(jsonPath("$.todayCompleted").value(0))
                .andExpect(jsonPath("$.todayCancelled").value(0))
                .andExpect(jsonPath("$.todayEstimatedRevenue").value(0))
                .andExpect(jsonPath("$.todayTimeline", hasSize(0)))
                .andExpect(jsonPath("$.upcomingAppointments", hasSize(0)));
    }

    // ─── 2. TODAY APPOINTMENT COUNTS ─────────────────────────────────────────

    @Test
    public void testTodayAppointmentCounts() throws Exception {
        makeAppointment(AppointmentStatus.BOOKED,     LocalTime.of(9, 0),  LocalTime.of(9, 30));
        makeAppointment(AppointmentStatus.CONFIRMED,  LocalTime.of(10, 0), LocalTime.of(10, 30));
        makeAppointment(AppointmentStatus.COMPLETED,  LocalTime.of(11, 0), LocalTime.of(11, 30));
        makeAppointment(AppointmentStatus.CANCELLED,  LocalTime.of(12, 0), LocalTime.of(12, 30));
        makeAppointment(AppointmentStatus.NO_SHOW,    LocalTime.of(13, 0), LocalTime.of(13, 30));

        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@alpha.com", salonAlpha.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayTotal").value(5))
                .andExpect(jsonPath("$.todayCompleted").value(1))
                .andExpect(jsonPath("$.todayCancelled").value(2));  // CANCELLED + NO_SHOW
    }

    // ─── 3. REVENUE: ONLY ACTIVE BOOKINGS CONTRIBUTE ─────────────────────────

    @Test
    public void testRevenueBehavior() throws Exception {
        // 3 revenue-contributing appointments (1500 each = 4500)
        makeAppointment(AppointmentStatus.BOOKED,    LocalTime.of(9, 0),  LocalTime.of(9, 30));
        makeAppointment(AppointmentStatus.CONFIRMED, LocalTime.of(10, 0), LocalTime.of(10, 30));
        makeAppointment(AppointmentStatus.COMPLETED, LocalTime.of(11, 0), LocalTime.of(11, 30));
        // Non-contributing
        makeAppointment(AppointmentStatus.CANCELLED, LocalTime.of(12, 0), LocalTime.of(12, 30));
        makeAppointment(AppointmentStatus.NO_SHOW,   LocalTime.of(13, 0), LocalTime.of(13, 30));

        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@alpha.com", salonAlpha.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayEstimatedRevenue").value(4500));
    }

    // ─── 4. CANCELLED/NO-SHOW EXCLUDED FROM REVENUE ──────────────────────────

    @Test
    public void testCancelledNoShowExcludedFromRevenue() throws Exception {
        makeAppointment(AppointmentStatus.CANCELLED, LocalTime.of(9, 0),  LocalTime.of(9, 30));
        makeAppointment(AppointmentStatus.NO_SHOW,   LocalTime.of(10, 0), LocalTime.of(10, 30));

        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@alpha.com", salonAlpha.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayEstimatedRevenue").value(0));
    }

    // ─── 5. UPCOMING: EXCLUDES CANCELLED/NO-SHOW ─────────────────────────────

    @Test
    public void testUpcomingAppointmentsExcludesCancelled() throws Exception {
        makeAppointment(AppointmentStatus.BOOKED,    LocalTime.of(9, 0),  LocalTime.of(9, 30));
        makeAppointment(AppointmentStatus.CANCELLED, LocalTime.of(10, 0), LocalTime.of(10, 30));
        makeAppointment(AppointmentStatus.NO_SHOW,   LocalTime.of(11, 0), LocalTime.of(11, 30));

        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@alpha.com", salonAlpha.getSubdomain())))
                .andExpect(status().isOk())
                // Only BOOKED appears in upcoming
                .andExpect(jsonPath("$.upcomingAppointments", hasSize(1)))
                .andExpect(jsonPath("$.upcomingAppointments[0].status").value("BOOKED"));
    }

    // ─── 6. TODAY TIMELINE INCLUDES ALL STATUSES ORDERED BY TIME ─────────────

    @Test
    public void testTodayTimelineOrdering() throws Exception {
        makeAppointment(AppointmentStatus.COMPLETED, LocalTime.of(11, 0), LocalTime.of(11, 30));
        makeAppointment(AppointmentStatus.BOOKED,    LocalTime.of(9, 0),  LocalTime.of(9, 30));
        makeAppointment(AppointmentStatus.CANCELLED, LocalTime.of(10, 0), LocalTime.of(10, 30));

        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@alpha.com", salonAlpha.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayTimeline", hasSize(3)))
                .andExpect(jsonPath("$.todayTimeline[0].startTime", startsWith("09:00")))
                .andExpect(jsonPath("$.todayTimeline[1].startTime", startsWith("10:00")))
                .andExpect(jsonPath("$.todayTimeline[2].startTime", startsWith("11:00")));
    }


    // ─── 7. ARCHIVED SERVICE: HISTORICAL APPOINTMENT STILL IN REVENUE ─────────

    @Test
    public void testArchivedServiceHistoryKeptInRevenue() throws Exception {
        // Create appointment with active service (revenue should be included)
        makeAppointment(AppointmentStatus.COMPLETED, LocalTime.of(9, 0), LocalTime.of(9, 30));

        // Archive the service
        serviceAlpha.setActive(false);
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        serviceRepository.save(serviceAlpha);
        TenantContext.clear();

        // Dashboard should still count the historical appointment in revenue
        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@alpha.com", salonAlpha.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayEstimatedRevenue").value(1500))
                .andExpect(jsonPath("$.todayCompleted").value(1))
                // Archived service should not appear in active service count
                .andExpect(jsonPath("$.activeServices").value(0));
    }

    // ─── 8. TENANT ISOLATION ─────────────────────────────────────────────────

    @Test
    public void testTenantIsolationAlphaCannotSeeBeta() throws Exception {
        // Create appointment for Alpha tenant
        makeAppointment(AppointmentStatus.BOOKED, LocalTime.of(9, 0), LocalTime.of(9, 30));

        // Beta authenticated user should see 0 appointments, not Alpha's data
        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@beta.com", salonBeta.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayTotal").value(0))
                .andExpect(jsonPath("$.todayTimeline", hasSize(0)))
                .andExpect(jsonPath("$.upcomingAppointments", hasSize(0)));
    }

    @Test
    public void testTenantIsolationRevenueIsScoped() throws Exception {
        makeAppointment(AppointmentStatus.COMPLETED, LocalTime.of(9, 0), LocalTime.of(9, 30));

        // Beta cannot see Alpha's revenue
        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@beta.com", salonBeta.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todayEstimatedRevenue").value(0));
    }

    // ─── 9. CUSTOMER COUNT IS TENANT-SCOPED ──────────────────────────────────

    @Test
    public void testCustomerCountIsTenantScoped() throws Exception {
        // Add a second customer to Alpha
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        Customer c2 = new Customer();
        c2.setFullName("Kamala Alpha");
        c2.setPhone("0779876543");
        c2.setTenantId(salonAlpha.getSubdomain());
        customerRepository.save(c2);

        // Add a customer to Beta (should not be counted by Alpha)
        TenantContext.setCurrentTenant(salonBeta.getSubdomain());
        Customer cBeta = new Customer();
        cBeta.setFullName("Beta Customer");
        cBeta.setPhone("0771111111");
        cBeta.setTenantId(salonBeta.getSubdomain());
        customerRepository.save(cBeta);
        TenantContext.clear();

        // Alpha should see 2 customers
        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@alpha.com", salonAlpha.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(2));

        // Beta should see only 1 customer
        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@beta.com", salonBeta.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(1));
    }

    // ─── 10. ACTIVE STAFF COUNT ──────────────────────────────────────────────

    @Test
    public void testActiveStaffCount() throws Exception {
        // staffAlpha is already created in setUp (active=true, role=STAFF)
        // Add an inactive staff member — should not be counted
        User inactive = new User();
        inactive.setFullName("Inactive Staff");
        inactive.setUsername("inactive_" + UUID.randomUUID().toString().substring(0, 5));
        inactive.setEmail("inactive_" + UUID.randomUUID().toString().substring(0, 5) + "@alpha.com");
        inactive.setPasswordHash("hash");
        inactive.setRole("STAFF");
        inactive.setActive(false);
        inactive.setTenantId(salonAlpha.getSubdomain());
        userRepository.save(inactive);

        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@alpha.com", salonAlpha.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeStaff").value(greaterThanOrEqualTo(1)));
    }

    // ─── 11. ACTIVE SERVICE COUNT ────────────────────────────────────────────

    @Test
    public void testActiveServiceCount() throws Exception {
        // serviceAlpha is active
        // Deactivate it
        serviceAlpha.setActive(false);
        TenantContext.setCurrentTenant(salonAlpha.getSubdomain());
        serviceRepository.save(serviceAlpha);

        // Add two active services
        ServiceEntity s2 = new ServiceEntity();
        s2.setName("Manicure");
        s2.setPrice(BigDecimal.valueOf(800));
        s2.setDurationMinutes(45);
        s2.setActive(true);
        s2.setTenantId(salonAlpha.getSubdomain());
        serviceRepository.save(s2);

        ServiceEntity s3 = new ServiceEntity();
        s3.setName("Pedicure");
        s3.setPrice(BigDecimal.valueOf(1000));
        s3.setDurationMinutes(60);
        s3.setActive(true);
        s3.setTenantId(salonAlpha.getSubdomain());
        serviceRepository.save(s3);
        TenantContext.clear();

        mockMvc.perform(get("/dashboard")
                .header("Authorization", auth("owner@alpha.com", salonAlpha.getSubdomain())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeServices").value(2));
    }

    // ─── 12. UNAUTHENTICATED ACCESS ──────────────────────────────────────────

    @Test
    public void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isForbidden());
    }
}
