package com.akarsha.whatsapp;

public interface WhatsAppClient {
    String sendMessage(String toPhoneNumber, String message, WhatsAppConfiguration config);
}
