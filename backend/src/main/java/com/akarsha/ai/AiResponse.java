package com.akarsha.ai;

public class AiResponse {
    private String message;
    private boolean handoffRequested;
    private String toolCall; // For future JSON parsing if needed
    private AiLanguage language;

    public AiResponse(String message, boolean handoffRequested) {
        this.message = message;
        this.handoffRequested = handoffRequested;
        this.language = AiLanguage.EN; // Default
    }

    public AiResponse(String message, boolean handoffRequested, AiLanguage language) {
        this.message = message;
        this.handoffRequested = handoffRequested;
        this.language = language;
    }

    public String getMessage() { return message; }
    public boolean isHandoffRequested() { return handoffRequested; }
    public String getToolCall() { return toolCall; }
    public AiLanguage getLanguage() { return language; }
}
