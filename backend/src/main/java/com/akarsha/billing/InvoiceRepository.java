package com.akarsha.billing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Page<Invoice> findByCustomerId(Long customerId, Pageable pageable);
    Optional<Invoice> findByAppointmentId(Long appointmentId);
}
