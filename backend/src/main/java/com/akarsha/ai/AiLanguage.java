package com.akarsha.ai;

public enum AiLanguage {
    EN("en", "English"),
    SI("si", "සිංහල"),
    TA("ta", "தமிழ்"),
    SI_LATN("si-Latn", "Singlish"),
    TA_LATN("ta-Latn", "Tanglish");

    private final String code;
    private final String displayName;

    AiLanguage(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AiLanguage fromCode(String code) {
        if (code == null) return EN;
        for (AiLanguage lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        throw new IllegalArgumentException("Unsupported language code: " + code);
    }
}
