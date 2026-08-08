package com.akarsha.chat.dto;

import com.akarsha.chat.domain.SenderType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatMessageResponse {
    private String message;
    private SenderType senderType;
    private LocalDateTime timestamp;
}
