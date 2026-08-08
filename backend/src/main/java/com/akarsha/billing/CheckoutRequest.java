package com.akarsha.billing;

import java.math.BigDecimal;
import java.util.List;

public class CheckoutRequest {
    
    private Long appointmentId;
    private PaymentMethod paymentMethod;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private List<CheckoutLineItemRequest> lineItems;
    private String notes;

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public List<CheckoutLineItemRequest> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<CheckoutLineItemRequest> lineItems) {
        this.lineItems = lineItems;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
