package com.akarsha.ai;

import com.akarsha.appointment.AppointmentRepository;
import com.akarsha.core.entity.Salon;
import com.akarsha.core.entity.ServiceEntity;
import com.akarsha.core.entity.User;
import com.akarsha.core.repository.SalonRepository;
import com.akarsha.core.repository.ServiceRepository;
import com.akarsha.core.repository.UserRepository;
import com.akarsha.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenAiCompatibleProvider implements AiProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCompatibleProvider.class);

    private final RestTemplate restTemplate;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final SalonRepository salonRepository;
    private final AppointmentRepository appointmentRepository;

    @Value("${akarsha.ai.llm.url:}")
    private String apiUrl;

    @Value("${akarsha.ai.llm.key:}")
    private String apiKey;

    @Value("${akarsha.ai.llm.model:gpt-4o}")
    private String modelName;

    @Value("${akarsha.ai.llm.temperature:0.3}")
    private double temperature;

    public OpenAiCompatibleProvider(RestTemplate restTemplate,
                                     ServiceRepository serviceRepository,
                                     UserRepository userRepository,
                                     SalonRepository salonRepository,
                                     AppointmentRepository appointmentRepository) {
        this.restTemplate = restTemplate;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.salonRepository = salonRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public String getProviderName() {
        return "production";
    }

    @Override
    public AiResponse generateResponse(AiContext context) {
        if (apiUrl == null || apiUrl.isEmpty() || apiKey == null || apiKey.isEmpty()) {
            logger.warn("LLM API credentials not configured. Falling back to Mock responses.");
            return fallbackMock(context);
        }

        try {
            String tenantId = TenantContext.getCurrentTenant();
            
            // 1. Gather Salon Context
            Optional<Salon> salonOpt = salonRepository.findBySubdomain(tenantId);
            String salonName = salonOpt.map(Salon::getName).orElse("Our Salon");
            String address = salonOpt.map(s -> s.getAddress() + ", " + s.getCity()).orElse("unknown location");
            String phone = salonOpt.map(Salon::getPhone).orElse("unknown phone");
            String businessHours = salonOpt.map(s -> s.getOpeningTime() + " to " + s.getClosingTime()).orElse("9 AM to 6 PM");

            // 2. Fetch Active Services
            List<ServiceEntity> services = serviceRepository.findAll();
            String servicesContext = services.stream()
                    .filter(ServiceEntity::isActive)
                    .map(s -> String.format("- %s: LKR %s (%d mins)", s.getName(), s.getPrice().toString(), s.getDurationMinutes()))
                    .collect(Collectors.joining("\n"));

            // 3. Fetch Active Staff
            List<User> staffList = userRepository.findAll().stream()
                    .filter(u -> u.isActive() && !"PLATFORM_ADMIN".equals(u.getRole()))
                    .collect(Collectors.toList());
            String staffContext = staffList.stream()
                    .map(User::getFullName)
                    .collect(Collectors.joining(", "));

            // 4. Construct System Prompt with Guardrails and Language Rules
            String systemInstructions = String.format(
                "You are %s, the professional AI receptionist for %s.\n\n" +
                "SALON INFO:\n" +
                "- Name: %s\n" +
                "- Address: %s\n" +
                "- Phone: %s\n" +
                "- Hours: %s\n" +
                "- Tone: %s\n" +
                "- Custom Context: %s\n\n" +
                "AVAILABLE SERVICES:\n%s\n\n" +
                "AVAILABLE STAFF MEMBERS:\n%s\n\n" +
                "POLICIES & BEHAVIOR:\n" +
                "- NEVER invent or hallucinate pricing, staff, services, or appointment availability. If asked for something not listed, state politely that you don't have that info and offer human assistance.\n" +
                "- Before finalizing any booking request, confirm service name, date, time, and customer name.\n" +
                "- If the customer wants to speak with a human or you cannot resolve their request, trigger a handoff by outputting the tag: [HANDOFF].\n\n" +
                "LANGUAGE DIRECTIONS (PERSIST CURRENT MODE):" +
                "- Current Language Preference: %s\n" +
                "- When using SI (Sinhala), write ONLY in actual Sinhala Unicode script (e.g. ඔබට හෙට සවස 4.00ට වේලාවක් තිබෙනවා). Do NOT write Sinhala using English letters.\n" +
                "- When using TA (Tamil), write ONLY in actual Tamil Unicode script (e.g. உங்களுக்கு நாளை மாலை 4 மணிக்கு நேரம் கிடைக்கிறது). Do NOT write Tamil using English letters.\n" +
                "- When using SI_LATN (Singlish), write Sinhala words in Latin characters (e.g. kohomada, oyata appointment ekak panna help karannada).\n" +
                "- When using TA_LATN (Tanglish), write Tamil words in Latin characters (e.g. ungalukku appointment book panna help panren).\n" +
                "- When using EN (English), write in normal professional English.\n",
                context.getConfiguration().getAssistantName(),
                salonName,
                salonName,
                address,
                phone,
                businessHours,
                context.getConfiguration().getTone(),
                context.getConfiguration().getBusinessContext() != null ? context.getConfiguration().getBusinessContext() : "",
                servicesContext,
                staffContext,
                context.getLanguagePreference().getDisplayName()
            );

            // 5. Build Message Payload
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemInstructions));

            for (AiMessage msg : context.getHistory()) {
                String role = msg.getSenderType() == MessageSender.USER ? "user" : "assistant";
                messages.add(Map.of("role", role, "content", msg.getContent()));
            }
            messages.add(Map.of("role", "user", "content", context.getUserMessage()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = Map.of(
                "model", modelName,
                "messages", messages,
                "temperature", temperature
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);
            
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> responseMsg = (Map<String, Object>) choice.get("message");
                    String content = (String) responseMsg.get("content");
                    
                    boolean handoff = false;
                    if (content.contains("[HANDOFF]") || content.toLowerCase().contains("handoff")) {
                        content = content.replace("[HANDOFF]", "").trim();
                        handoff = true;
                    }
                    
                    return new AiResponse(content, handoff, context.getLanguagePreference());
                }
            }
            
            logger.warn("Received empty or malformed response from LLM API.");
            return fallbackMock(context);
        } catch (Exception e) {
            logger.error("Error communicating with real LLM API. Falling back to Mock.", e);
            return fallbackMock(context);
        }
    }

    private AiResponse fallbackMock(AiContext context) {
        MockAiProvider mockProvider = new MockAiProvider();
        return mockProvider.generateResponse(context);
    }
}
