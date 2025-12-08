package com.smarthire.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * PythonAIClient - Spring service to call Python FastAPI Smart Hire backend.
 *
 * Configure in application.properties:
 *   fastapi.base.url=http://localhost:5001
 *
 * Usage:
 *   @Autowired PythonAIClient client;
 *   Map<String,Object> jd = client.generateJobDescription("Acme","Software Engineer");
 *
 * This class keeps responses as Maps for flexibility. Convert to DTOs if you prefer typed models.
 */
@Service
public class PythonAIClient {

    @Value("${fastapi.base.url:http://localhost:5001}")
    private String fastApiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("null")
    private HttpHeaders defaultJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "SmartHire-Spring-Client/1.0");
        return headers;
    }

    // -----------------------
    // Health & Webhook helpers
    // -----------------------
    @SuppressWarnings({ "null", "deprecation" })
    public Map<String, Object> healthCheck() {
        String url = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl).path("/health").toUriString();
        try {
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                return objectMapper.readValue(res.getBody(), new TypeReference<>() {});
            } else {
                return Map.of("status", "unreachable", "code", res.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    public Map<String, Object> testWebhook() {
        String url = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl).path("/test-webhook").toUriString();
        try {
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                return objectMapper.readValue(res.getBody(), new TypeReference<>() {});
            } else {
                return Map.of("status", "failed", "code", res.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    // -----------------------
    // Generate Job Description
    // -----------------------
    /**
     * Call /generate-jd endpoint.
     * jobData map must contain company_name or companyName and job_role or jobTitle.
     */
    public Map<String, Object> generateJobDescription(Map<String, Object> jobData) {
        String url = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl).path("/generate-jd").toUriString();
        try {
            HttpHeaders headers = defaultJsonHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jobData, headers);
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                // response_model = JDGenerateResponse { job_description, company_name, job_role }
                return objectMapper.readValue(res.getBody(), new TypeReference<>() {});
            } else {
                return Map.of("error", "Non-200 from FastAPI", "code", res.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Map.of("error", "generateJobDescription failed", "message", e.getMessage());
        }
    }

    // -----------------------
    // Post Job Description
    // -----------------------
    /**
     * Call /post-jd endpoint. Accepts same payload as generate but will post.
     */
    public Map<String, Object> postJobDescription(Map<String, Object> jobData) {
        String url = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl).path("/post-jd").toUriString();
        try {
            HttpHeaders headers = defaultJsonHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jobData, headers);
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                return objectMapper.readValue(res.getBody(), new TypeReference<>() {});
            } else {
                return Map.of("error", "Non-200 from FastAPI", "code", res.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Map.of("error", "postJobDescription failed", "message", e.getMessage());
        }
    }

    // -----------------------
    // Applications (fetch resumes)
    // -----------------------
    public Map<String, Object> getApplications() {
        String url = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl).path("/applications").toUriString();
        try {
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                return objectMapper.readValue(res.getBody(), new TypeReference<>() {});
            } else {
                return Map.of("error", "Non-200 from FastAPI", "code", res.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Map.of("error", "getApplications failed", "message", e.getMessage());
        }
    }

    // -----------------------
    // Monitor status
    // -----------------------
    public Map<String, Object> getMonitorStatus() {
        String url = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl).path("/monitor-status").toUriString();
        try {
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                return objectMapper.readValue(res.getBody(), new TypeReference<>() {});
            } else {
                return Map.of("error", "Non-200 from FastAPI", "code", res.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Map.of("error", "getMonitorStatus failed", "message", e.getMessage());
        }
    }

    // -----------------------
    // Shortlist candidates
    // -----------------------
    /**
     * Call /shortlist with ShortlistRequest { job_description: "..." }
     * returns ShortlistResponse { shortlist: [...], count: n }
     */
    public Map<String, Object> shortlistCandidates(String jobDescription) {
        String url = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl).path("/shortlist").toUriString();
        try {
            Map<String, Object> payload = Map.of("job_description", jobDescription);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, defaultJsonHeaders());
            ResponseEntity<String> res = restTemplate.postForEntity(url, entity, String.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                return objectMapper.readValue(res.getBody(), new TypeReference<>() {});
            } else {
                return Map.of("error", "Non-200 from FastAPI", "code", res.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Map.of("error", "shortlistCandidates failed", "message", e.getMessage());
        }
    }

    // -----------------------
    // Schedule interviews
    // -----------------------
    /**
     * Call /schedule-interviews with InterviewRequest { shortlist: [...], job_role: "..." }
     * shortlist should be the list returned from /shortlist.
     */
    public Map<String, Object> scheduleInterviews(List<Map<String, Object>> shortlist, String jobRole) {
        String url = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl).path("/schedule-interviews").toUriString();
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("shortlist", shortlist);
            payload.put("job_role", jobRole);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, defaultJsonHeaders());
            ResponseEntity<String> res = restTemplate.postForEntity(url, entity, String.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                return objectMapper.readValue(res.getBody(), new TypeReference<>() {});
            } else {
                return Map.of("error", "Non-200 from FastAPI", "code", res.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Map.of("error", "scheduleInterviews failed", "message", e.getMessage());
        }
    }

    // -----------------------
    // Run full workflow
    // -----------------------
    /**
     * Call /run-workflow with WorkflowRequest { company_name, job_role }
     * returns WorkflowResponse with job_description, posting_result, shortlist, interviews, ...
     */
    public Map<String, Object> runFullWorkflow(String companyName, String jobRole) {
        String url = UriComponentsBuilder.fromHttpUrl(fastApiBaseUrl).path("/run-workflow").toUriString();
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("company_name", companyName);
            payload.put("job_role", jobRole);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, defaultJsonHeaders());
            ResponseEntity<String> res = restTemplate.postForEntity(url, entity, String.class);
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                return objectMapper.readValue(res.getBody(), new TypeReference<>() {});
            } else {
                return Map.of("error", "Non-200 from FastAPI", "code", res.getStatusCodeValue());
            }
        } catch (Exception e) {
            return Map.of("error", "runFullWorkflow failed", "message", e.getMessage());
        }
    }

    // -----------------------
    // Utility helpers (parsing responses)
    // -----------------------
    /**
     * Helper to extract a nested field (if you need it)
     */
    public Optional<JsonNode> getJsonNode(String json, String path) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String[] parts = path.split("\\.");
            JsonNode cur = root;
            for (String p : parts) {
                if (cur == null) return Optional.empty();
                cur = cur.path(p);
            }
            return Optional.ofNullable(cur.isMissingNode() ? null : cur);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
