package com.akarsha.whatsapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WhatsAppConfig {

    @Value("${whatsapp.client.mock:true}")
    private boolean useMockClient;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WhatsAppClient whatsAppClient(RestTemplate restTemplate) {
        if (useMockClient) {
            return new MockWhatsAppClient();
        }
        return new MetaWhatsAppClient(restTemplate);
    }
}
