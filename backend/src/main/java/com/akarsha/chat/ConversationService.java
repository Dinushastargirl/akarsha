package com.akarsha.chat;

import com.akarsha.chat.domain.*;
import com.akarsha.customer.Customer;
import com.akarsha.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final CustomerRepository customerRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ConversationMessageRepository conversationMessageRepository,
                               CustomerRepository customerRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationMessageRepository = conversationMessageRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Conversation getOrCreateConversation(String phoneOrEmail, ChannelType channel, LanguageMode language) {
        // Customer identity verification
        Customer customer = customerRepository.findAll().stream()
                .filter(c -> phoneOrEmail.equals(c.getPhone()) || phoneOrEmail.equals(c.getEmail()))
                .findFirst()
                .orElseGet(() -> {
                    // Create minimal customer record if none exists
                    Customer newCustomer = new Customer();
                    newCustomer.setFullName("Guest");
                    newCustomer.setEmail(phoneOrEmail.contains("@") ? phoneOrEmail : null);
                    newCustomer.setPhone(!phoneOrEmail.contains("@") ? phoneOrEmail : null);
                    return customerRepository.save(newCustomer);
                });

        Optional<Conversation> existing = conversationRepository.findByCustomerIdAndChannel(customer.getId(), channel);
        if (existing.isPresent()) {
            Conversation conv = existing.get();
            // Update language if it changed
            if (conv.getLanguage() != language) {
                conv.setLanguage(language);
                conversationRepository.save(conv);
            }
            return conv;
        }

        Conversation newConv = new Conversation();
        newConv.setCustomer(customer);
        newConv.setChannel(channel);
        newConv.setLanguage(language);
        newConv.setStatus(ConversationStatus.AI);
        return conversationRepository.save(newConv);
    }

    @Transactional
    public ConversationMessage saveMessage(Conversation conversation, String text, SenderType senderType) {
        ConversationMessage message = new ConversationMessage();
        message.setConversation(conversation);
        message.setMessage(text);
        message.setSenderType(senderType);
        message.setMessageTimestamp(LocalDateTime.now());
        return conversationMessageRepository.save(message);
    }
    
    @Transactional(readOnly = true)
    public List<ConversationMessage> getMessages(Long conversationId) {
        return conversationMessageRepository.findByConversationIdOrderByMessageTimestampAsc(conversationId);
    }
}
