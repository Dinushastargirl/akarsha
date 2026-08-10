package com.akarsha.ai;

public interface AiProvider {
    String getProviderName();
    AiResponse generateResponse(AiContext context);
}
