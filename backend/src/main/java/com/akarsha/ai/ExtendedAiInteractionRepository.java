package com.akarsha.ai;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtendedAiInteractionRepository extends JpaRepository<AiInteraction, Long> {
    
    @Query("SELECT i FROM AiInteraction i WHERE i.tenantId = :tenantId " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:unread IS NULL OR (:unread = true AND i.unreadCount > 0) OR (:unread = false AND i.unreadCount = 0)) " +
           "AND (:channel IS NULL OR i.channel = :channel) " +
           "ORDER BY i.lastActivity DESC")
    Page<AiInteraction> findFiltered(
            @Param("tenantId") String tenantId, 
            @Param("status") InteractionStatus status, 
            @Param("unread") Boolean unread, 
            @Param("channel") String channel, 
            Pageable pageable);
}
