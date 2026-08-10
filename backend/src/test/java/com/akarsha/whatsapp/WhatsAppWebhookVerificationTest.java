package com.akarsha.whatsapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.Mockito;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "whatsapp.verify-token=test_token")
public class WhatsAppWebhookVerificationTest {

    @MockBean
    private WhatsAppConfigurationRepository configRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testWebhookVerificationSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/public/whatsapp/webhook")
                .param("hub.mode", "subscribe")
                .param("hub.verify_token", "test_token") // Assumes properties has this set or default is empty
                .param("hub.challenge", "12345"))
                .andExpect(status().isOk());
    }

    @Test
    public void testIncomingMessageStructure() throws Exception {
        String payload = """
        {
          "object": "whatsapp_business_account",
          "entry": [
            {
              "id": "WABA_ID",
              "changes": [
                {
                  "value": {
                    "messaging_product": "whatsapp",
                    "metadata": {
                      "display_phone_number": "16505551111",
                      "phone_number_id": "999999999"
                    },
                    "contacts": [
                      {
                        "profile": {
                          "name": "Test User"
                        },
                        "wa_id": "16315551234"
                      }
                    ],
                    "messages": [
                      {
                        "from": "16315551234",
                        "id": "wamid.TEST1234",
                        "timestamp": "1602320448",
                        "text": {
                          "body": "Hi there"
                        },
                        "type": "text"
                      }
                    ]
                  },
                  "field": "messages"
                }
              ]
            }
          ]
        }
        """;
        
        WhatsAppConfiguration mockConfig = new WhatsAppConfiguration();
        mockConfig.setTenantId("test_tenant");
        mockConfig.setEnabled(true);
        Mockito.when(configRepository.findByPhoneNumberIdSystemBypass("999999999"))
               .thenReturn(Optional.of(mockConfig));

        mockMvc.perform(post("/api/v1/public/whatsapp/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("EVENT_RECEIVED"));
    }
}
