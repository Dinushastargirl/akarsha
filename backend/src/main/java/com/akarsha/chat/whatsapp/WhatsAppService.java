package com.akarsha.chat.whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class WhatsAppService {

    @Value("${whatsapp.verify.token:akarsha_verify_123}")
    private String verifyToken;

    @Value("${whatsapp.app.secret:dummy_secret}")
    private String appSecret;

    public boolean verifyWebhook(String mode, String token) {
        return "subscribe".equals(mode) && verifyToken.equals(token);
    }

    public boolean verifySignature(String payload, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false; // Typically we'd return false, but for local mock we could bypass
        }
        
        // For demo/mock environments where appSecret is default, bypass strict check to allow testing
        if ("dummy_secret".equals(appSecret)) {
            return true;
        }

        try {
            String expectedSignature = signatureHeader.substring(7);
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            String calculatedSignature = sb.toString();
            
            return expectedSignature.equals(calculatedSignature);
        } catch (Exception e) {
            return false;
        }
    }

    public void sendMessage(String phoneNumber, String message) {
        // Mock implementation of sending message via WhatsApp Graph API
        System.out.println("WHATSAPP MOCK OUTBOUND -> to: " + phoneNumber + " msg: " + message);
    }
}
