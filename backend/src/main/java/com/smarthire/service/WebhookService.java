package com.smarthire.service;

import com.smarthire.model.Job;
import com.smarthire.model.JobApplication;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    // Your webhook.site URL
    private static final String WEBHOOK_URL = "https://webhook.site/92b7a908-f6dc-41ca-8e56-84f082e9da5c";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public WebhookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Send Job posted event to webhook.site
     */
    public Map<String, Object> postJobToWebhook(Job job) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("📤 Posting job to webhook...");

            Map<String, Object> payload = new HashMap<>();
            payload.put("event", "JOB_POSTED");
            payload.put("jobId", job.getId());
            payload.put("title", job.getTitle());
            payload.put("company", job.getCompany());
            payload.put("location", job.getLocation() != null ? job.getLocation() : "Remote");
            payload.put("description", job.getDescription());
            payload.put("employmentType", job.getEmploymentType());
            payload.put("experienceLevel", job.getExperienceLevel());
            payload.put("salaryRange", job.getSalaryRange() != null ? job.getSalaryRange() : "Competitive Salary");
            payload.put("requiredSkills", job.getRequiredSkills() != null ? job.getRequiredSkills() : "");

            String applicationUrl = frontendUrl + "/apply/" + job.getId();
            payload.put("applicationUrl", applicationUrl);

            String postedAt = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            payload.put("postedAt", postedAt);
            payload.put("status", job.getStatus() != null ? job.getStatus().toString() : "OPEN");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("User-Agent", "SmartHire-Backend");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    WEBHOOK_URL, HttpMethod.POST, request, String.class);

            System.out.println("✅ Webhook POST status: " + response.getStatusCode());

            result.put("success", response.getStatusCode().is2xxSuccessful());
            result.put("payload", payload);

            return result;

        } catch (Exception e) {
            System.err.println("❌ Webhook post failed: " + e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
}
