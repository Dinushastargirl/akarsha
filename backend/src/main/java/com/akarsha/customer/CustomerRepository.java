package com.akarsha.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findByFullNameContainingIgnoreCaseOrPhoneContaining(String fullName, String phone, Pageable pageable);

    // count() inherited from JpaRepository is automatically tenant-filtered by TenantAspect + Hibernate @Filter
    long countByCreatedAtBetween(java.time.Instant start, java.time.Instant end);
}
