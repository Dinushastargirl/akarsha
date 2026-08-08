package com.akarsha.chat.ai;

import java.util.List;

public interface AiProvider {
    /**
     * Generates a response based on the conversation history.
     * @param history The list of previous messages in the conversation (including system prompts)
     * @return The AI's response text
     */
    String generateResponse(List<AiMessage> history);
}
