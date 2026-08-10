package com.akarsha.ai;

import java.util.List;

public class AiContext {
    private AiConfiguration configuration;
    private List<AiMessage> history;
    private String userMessage;
    private AiLanguage languagePreference;

    public AiContext(AiConfiguration configuration, List<AiMessage> history, String userMessage, AiLanguage languagePreference) {
        this.configuration = configuration;
        this.history = history;
        this.userMessage = userMessage;
        this.languagePreference = languagePreference;
    }

    public AiConfiguration getConfiguration() { return configuration; }
    public List<AiMessage> getHistory() { return history; }
    public String getUserMessage() { return userMessage; }
    public AiLanguage getLanguagePreference() { return languagePreference; }
}
