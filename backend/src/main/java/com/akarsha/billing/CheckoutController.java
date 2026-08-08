package com.akarsha.billing;

import com.akarsha.appointment.Appointment;
import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.appointment.AppointmentStatus;
import com.akarsha.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    public CheckoutController(
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            AppointmentRepository appointmentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @PostMapping("/appointment/{id}")
    @PreAuthorize("hasAnyRole('SALON_OWNER', 'MANAGER', 'RECEPTIONIST', 'STAFF')")
    @Transactional
    public ResponseEntity<?> checkoutAppointment(@PathVariable Long id, @RequestBody CheckoutRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        
        Optional<Appointment> optApp = appointmentRepository.findById(id);
        if (optApp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }
        
        Appointment appointment = optApp.get();
        if (tenantId != null && !tenantId.equals(appointment.getTenantId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Appointment is already completed/checked out");
        }

        // Validate line items
        if (request.getLineItems() == null || request.getLineItems().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("At least one line item is required for checkout");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        
        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setCustomer(appointment.getCustomer());
        invoice.setAppointment(appointment);
        invoice.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        invoice.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        invoice.setNotes(request.getNotes());
        invoice.setStatus(InvoiceStatus.PAID);

        for (CheckoutLineItemRequest itemReq : request.getLineItems()) {
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Line item quantity must be greater than zero");
            }
            if (itemReq.getUnitPrice() == null || itemReq.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Line item unit price cannot be negative");
            }
            
            BigDecimal lineTotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            InvoiceLineItem lineItem = new InvoiceLineItem();
            lineItem.setItemType(itemReq.getItemType() != null ? itemReq.getItemType() : ItemType.CUSTOM);
            lineItem.setReferenceId(itemReq.getReferenceId());
            lineItem.setDescription(itemReq.getDescription());
            lineItem.setQuantity(itemReq.getQuantity());
            lineItem.setUnitPrice(itemReq.getUnitPrice());
            lineItem.setTotalPrice(lineTotal);

            invoice.addLineItem(lineItem);
        }

        invoice.setSubtotal(subtotal);
        BigDecimal total = subtotal.add(invoice.getTaxAmount()).subtract(invoice.getDiscountAmount());
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        invoice.setTotalAmount(total);

        // Save Invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Create Payment
        if (request.getPaymentMethod() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment method is required");
        }

        Payment payment = new Payment();
        payment.setTenantId(tenantId);
        payment.setInvoice(savedInvoice);
        payment.setAmount(total);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        // Mark appointment as COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedInvoice);
    }
}
