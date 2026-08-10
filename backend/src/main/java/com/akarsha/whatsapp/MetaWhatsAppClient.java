package com.akarsha.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MetaWhatsAppClient implements WhatsAppClient {

    private static final Logger logger = LoggerFactory.getLogger(MetaWhatsAppClient.class);
    private static final String META_GRAPH_URL = "https://graph.facebook.com/v19.0/";

    private final RestTemplate restTemplate;

    public MetaWhatsAppClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String sendMessage(String toPhoneNumber, String message, WhatsAppConfiguration config) {
        if (config.getAccessToken() == null || config.getAccessToken().isEmpty()) {
            logger.error("Missing access token for salon WhatsApp configuration. Cannot send message.");
            return null;
        }

        try {
            String url = META_GRAPH_URL + config.getPhoneNumberId() + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getAccessToken());

            Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", toPhoneNumber,
                "type", "text",
                "text", Map.of("preview_url", false, "body", message)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            
            // Invoke the Meta Graph API and parse the response map
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            if (response != null && response.containsKey("messages")) {
                List<Map<String, Object>> messagesList = (List<Map<String, Object>>) response.get("messages");
                if (messagesList != null && !messagesList.isEmpty()) {
                    String msgId = (String) messagesList.get(0).get("id");
                    logger.info("META WHATSAPP OUTBOUND -> Sent to {}, MsgId: {}", toPhoneNumber, msgId);
                    return msgId;
                }
            }
            
            logger.warn("META WHATSAPP OUTBOUND -> Request succeeded but response format was unexpected");
            return "wamid.fallback_" + UUID.randomUUID().toString();
        } catch (Exception e) {
            logger.error("Failed to send Meta WhatsApp message to " + toPhoneNumber, e);
            return null;
        }
    }
}
