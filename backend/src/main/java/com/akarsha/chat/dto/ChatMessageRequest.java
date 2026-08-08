package com.akarsha.chat.dto;

import com.akarsha.chat.domain.LanguageMode;
import lombok.Data;

@Data
public class ChatMessageRequest {
    private String identifier; // phone or email for guest/customer mapping
    private String message;
    private LanguageMode language;
}
