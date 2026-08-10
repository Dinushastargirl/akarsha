package com.akarsha.whatsapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public class MockWhatsAppClient implements WhatsAppClient {

    private static final Logger logger = LoggerFactory.getLogger(MockWhatsAppClient.class);

    @Override
    public String sendMessage(String toPhoneNumber, String message, WhatsAppConfiguration config) {
        String msgId = "wamid.mock_" + UUID.randomUUID().toString();
        logger.info("MOCK WHATSAPP OUTBOUND -> To: {}, Message: '{}', MsgId: {}", toPhoneNumber, message, msgId);
        return msgId;
    }
}
