package com.akarsha.ai;

import org.springframework.stereotype.Service;

@Service
public class MockAiProvider implements AiProvider {

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public AiResponse generateResponse(AiContext context) {
        String input = context.getUserMessage().toLowerCase();
        AiLanguage lang = context.getLanguagePreference();
        
        if (input.contains("handoff") || input.contains("human") || input.contains("agent")) {
            return new AiResponse(getLocalizedMessage("I will transfer you to a human agent.", lang), true, lang);
        }
        
        if (input.contains("book") || input.contains("appointment")) {
            return new AiResponse(getLocalizedMessage("I can help you book an appointment. What service do you need?", lang), false, lang);
        }

        if (input.contains("price") || input.contains("cost") || input.contains("how much")) {
            return new AiResponse(getLocalizedMessage("Our haircut prices start at 2500 LKR. Would you like a full price list?", lang), false, lang);
        }

        return new AiResponse(getLocalizedMessage(context.getConfiguration().getGreeting(), lang), false, lang);
    }
    
    private String getLocalizedMessage(String englishFallback, AiLanguage lang) {
        switch (lang) {
            case SI:
                if (englishFallback.contains("transfer")) return "මම ඔබව අපේ කාර්ය මණ්ඩලයේ අයෙකුට සම්බන්ධ කරන්නම්.";
                if (englishFallback.contains("book")) return "මට ඔබට වේලාවක් වෙන් කිරීමට උදව් කළ හැකිය. ඔබට අවශ්‍ය සේවාව කුමක්ද?";
                if (englishFallback.contains("price")) return "අපගේ කොණ්ඩා කැපීමේ මිල රුපියල් 2500 කින් ආරම්භ වේ. ඔබට සම්පූර්ණ මිල ලැයිස්තුවක් අවශ්‍යද?";
                return "ආයුබෝවන්! මම ඔබේ කෘතිම බුද්ධි සහායකයා වෙමි. මට ඔබට උදව් කළ හැක්කේ කෙසේද?";
                
            case TA:
                if (englishFallback.contains("transfer")) return "நான் உங்களை எங்கள் பணியாளர் ஒருவருடன் இணைக்கிறேன்.";
                if (englishFallback.contains("book")) return "நான் உங்களுக்கு முன்பதிவு செய்ய உதவ முடியும். உங்களுக்கு என்ன சேவை தேவை?";
                if (englishFallback.contains("price")) return "எங்கள் முடி வெட்டும் விலை 2500 ரூபாயிலிருந்து தொடங்குகிறது. உங்களுக்கு முழுமையான விலை பட்டியல் வேண்டுமா?";
                return "வணக்கம்! நான் உங்கள் செயற்கை நுண்ணறிவு உதவியாளர். நான் உங்களுக்கு எப்படி உதவ முடியும்?";
                
            case SI_LATN: // Singlish
                if (englishFallback.contains("transfer")) return "Mama oyawa ape staff ekata connect karannam.";
                if (englishFallback.contains("book")) return "Mata oyata appointment ekak book karanna udaw karanna puluwan. Oyata one mona service ekada?";
                if (englishFallback.contains("price")) return "Ape haircut prices 2500 LKR walin patan gannawa. Oyata full price list eka oneda?";
                return "Ayubowan! Mama oyage AI assistant. Mata oyata udaw karanna puluwanda?";
                
            case TA_LATN: // Tanglish
                if (englishFallback.contains("transfer")) return "Naan ungalai enga staff kitta transfer panren.";
                if (englishFallback.contains("book")) return "Naan ungalukku appointment book panna help panren. Ungalukku enna service venum?";
                if (englishFallback.contains("price")) return "Enga haircut prices 2500 LKR-la irunthu start aaguthu. Ungalukku full price list venuma?";
                return "Vanakam! Naan unga AI assistant. Naan ungalukku eppadi help panrathu?";
                
            case EN:
            default:
                if (englishFallback.contains("transfer")) return "I will transfer you to a human agent.";
                if (englishFallback.contains("book")) return "I can help you book an appointment. What service do you need?";
                if (englishFallback.contains("price")) return "Our haircut prices start at 2500 LKR. Would you like a full price list?";
                return englishFallback;
        }
    }
}
