package com.akarsha.core.repository;

import com.akarsha.core.entity.ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    @Query("SELECT s FROM ServiceEntity s WHERE " +
           "(:query = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:active IS NULL OR s.active = :active)")
    Page<ServiceEntity> searchServices(
            @Param("query") String query,
            @Param("active") Boolean active,
            Pageable pageable);

    List<ServiceEntity> findByActive(boolean active);

    // Dashboard: count active services (tenant-filtered automatically)
    long countByActive(boolean active);
}
