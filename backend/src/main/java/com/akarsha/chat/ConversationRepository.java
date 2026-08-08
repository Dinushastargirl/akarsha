package com.akarsha.chat;

import com.akarsha.chat.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByCustomerIdAndChannel(Long customerId, com.akarsha.chat.domain.ChannelType channel);
}
