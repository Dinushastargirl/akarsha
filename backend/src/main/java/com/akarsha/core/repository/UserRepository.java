package com.akarsha.core.repository;

import com.akarsha.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTenantIdAndEmail(String tenantId, String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
           "u.role <> 'PLATFORM_ADMIN' AND " +
           "(:query IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR u.phone LIKE CONCAT('%', :query, '%')) AND " +
           "(:active IS NULL OR u.active = :active)")
    org.springframework.data.domain.Page<User> searchStaff(
            @org.springframework.data.repository.query.Param("query") String query,
            @org.springframework.data.repository.query.Param("active") Boolean active,
            org.springframework.data.domain.Pageable pageable);

    // Dashboard: count active staff excluding platform admin (tenant-filtered automatically)
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true AND u.role <> 'PLATFORM_ADMIN'")
    long countActiveStaff();
}
