package com.akarsha.whatsapp;

import com.akarsha.ai.AiInteraction;
import com.akarsha.ai.AiInteractionRepository;
import com.akarsha.ai.AiMessage;
import com.akarsha.ai.AiMessageRepository;
import com.akarsha.ai.AiOrchestratorService;
import com.akarsha.customer.Customer;
import com.akarsha.customer.CustomerRepository;
import com.akarsha.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WhatsAppWebhookProcessor {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookProcessor.class);

    private final WhatsAppConfigurationRepository configRepository;
    private final CustomerRepository customerRepository;
    private final AiInteractionRepository interactionRepository;
    private final AiMessageRepository messageRepository;
    private final AiOrchestratorService orchestratorService;
    private final WhatsAppClient whatsAppClient;

    public WhatsAppWebhookProcessor(WhatsAppConfigurationRepository configRepository,
                                    CustomerRepository customerRepository,
                                    AiInteractionRepository interactionRepository,
                                    AiMessageRepository messageRepository,
                                    AiOrchestratorService orchestratorService,
                                    WhatsAppClient whatsAppClient) {
        this.configRepository = configRepository;
        this.customerRepository = customerRepository;
        this.interactionRepository = interactionRepository;
        this.messageRepository = messageRepository;
        this.orchestratorService = orchestratorService;
        this.whatsAppClient = whatsAppClient;
    }

    public void process(Map<String, Object> payload) {
        try {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            if (entries == null || entries.isEmpty()) return;

            for (Map<String, Object> entry : entries) {
                List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");
                if (changes == null) continue;

                for (Map<String, Object> change : changes) {
                    Map<String, Object> value = (Map<String, Object>) change.get("value");
                    if (value == null) continue;

                    Map<String, Object> metadata = (Map<String, Object>) value.get("metadata");
                    if (metadata == null) continue;

                    String phoneNumberId = (String) metadata.get("phone_number_id");
                    if (phoneNumberId == null) continue;

                    // 1. Check for incoming messages
                    List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
                    if (messages != null && !messages.isEmpty()) {
                        for (Map<String, Object> message : messages) {
                            processSingleMessage(phoneNumberId, message);
                        }
                    }

                    // 2. Check for message delivery statuses
                    List<Map<String, Object>> statuses = (List<Map<String, Object>>) value.get("statuses");
                    if (statuses != null && !statuses.isEmpty()) {
                        for (Map<String, Object> statusObj : statuses) {
                            processSingleStatus(phoneNumberId, statusObj);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse WhatsApp payload", e);
        }
    }

    private void processSingleMessage(String phoneNumberId, Map<String, Object> message) {
        Optional<WhatsAppConfiguration> configOpt = configRepository.findByPhoneNumberIdSystemBypass(phoneNumberId);
        if (configOpt.isEmpty()) {
            logger.warn("Received WhatsApp message for unknown phone_number_id: {}", phoneNumberId);
            return;
        }

        WhatsAppConfiguration config = configOpt.get();
        if (!config.isEnabled()) {
            return;
        }

        try {
            TenantContext.setCurrentTenant(config.getTenantId());

            String externalId = (String) message.get("id");
            if (externalId != null && messageRepository.existsByExternalId(externalId)) {
                logger.info("Idempotency hit: WhatsApp message {} already processed", externalId);
                return;
            }

            String fromPhone = (String) message.get("from");
            if (fromPhone == null) return;

            String type = (String) message.get("type");
            String body = "";
            
            if ("text".equals(type)) {
                Map<String, Object> textObj = (Map<String, Object>) message.get("text");
                body = (String) textObj.get("body");
            } else if ("interactive".equals(type)) {
                Map<String, Object> interactive = (Map<String, Object>) message.get("interactive");
                String interactiveType = (String) interactive.get("type");
                if ("button_reply".equals(interactiveType)) {
                    Map<String, Object> buttonReply = (Map<String, Object>) interactive.get("button_reply");
                    body = (String) buttonReply.get("title");
                }
            } else {
                logger.info("Unsupported WhatsApp message type: {}", type);
                return;
            }

            // 1. Resolve Customer
            List<Customer> customers = customerRepository.findByPhone(fromPhone);
            Customer customer;
            if (customers.isEmpty()) {
                customer = new Customer();
                customer.setPhone(fromPhone);
                customer.setFullName("WhatsApp Guest");
                customer = customerRepository.save(customer);
            } else {
                customer = customers.get(0);
            }

            // 2. Resolve Interaction
            Optional<AiInteraction> existingInteraction = interactionRepository.findFirstByCustomerIdAndChannelOrderByLastActivityDesc(customer.getId(), "WHATSAPP");
            
            String sessionId;
            String language = "en"; // Default
            if (existingInteraction.isPresent()) {
                AiInteraction interaction = existingInteraction.get();
                sessionId = interaction.getSessionId();
                if (interaction.getLanguage() != null) {
                    language = interaction.getLanguage().getCode();
                }
            } else {
                AiInteraction newInteraction = new AiInteraction();
                newInteraction.setCustomer(customer);
                newInteraction.setGuestIdentifier(fromPhone);
                newInteraction.setChannel("WHATSAPP");
                newInteraction.setLanguagePreference("English");
                newInteraction = interactionRepository.save(newInteraction);
                sessionId = newInteraction.getSessionId();
            }

            // 3. Dispatch to AI
            AiMessage responseMsg = orchestratorService.processMessage(sessionId, fromPhone, body, language, externalId);

            // 4. Send outbound WhatsApp message if the response is from AI
            if (responseMsg.getSenderType() == com.akarsha.ai.MessageSender.AI && responseMsg.getContent() != null && !responseMsg.getContent().isEmpty()) {
                String sentMsgId = whatsAppClient.sendMessage(fromPhone, responseMsg.getContent(), config);
                if (sentMsgId != null) {
                    responseMsg.setExternalId(sentMsgId);
                    responseMsg.setDeliveryStatus("SENT");
                    messageRepository.save(responseMsg);
                }
            }

        } finally {
            TenantContext.clear();
        }
    }

    private void processSingleStatus(String phoneNumberId, Map<String, Object> statusObj) {
        Optional<WhatsAppConfiguration> configOpt = configRepository.findByPhoneNumberIdSystemBypass(phoneNumberId);
        if (configOpt.isEmpty()) return;

        WhatsAppConfiguration config = configOpt.get();
        try {
            TenantContext.setCurrentTenant(config.getTenantId());
            String externalId = (String) statusObj.get("id");
            String status = (String) statusObj.get("status");

            if (externalId != null && status != null) {
                AiMessage message = messageRepository.findByExternalId(externalId);
                if (message != null) {
                    message.setDeliveryStatus(status.toUpperCase());
                    messageRepository.save(message);
                    logger.info("Updated WhatsApp message {} status to {}", externalId, status);
                }
            }
        } finally {
            TenantContext.clear();
        }
    }
}
