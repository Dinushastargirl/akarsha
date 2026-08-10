package com.akarsha.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiInteractionRepository extends JpaRepository<AiInteraction, Long> {
    Optional<AiInteraction> findBySessionId(String sessionId);
    Optional<AiInteraction> findFirstByCustomerIdAndChannelOrderByLastActivityDesc(Long customerId, String channel);
}
