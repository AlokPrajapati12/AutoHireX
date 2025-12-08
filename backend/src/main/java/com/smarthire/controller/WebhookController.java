package com.smarthire.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/webhook")
@CrossOrigin(origins = "*")
public class WebhookController {

    // 🔥 Your webhook.site URL - update this if needed
    private static final String WEBHOOK_URL = "https://webhook.site/92b7a908-f6dc-41ca-8e56-84f082e9da5c";
    private static final String WEBHOOK_VIEW_URL = "https://webhook.site/#!/view/92b7a908-f6dc-41ca-8e56-84f082e9da5c";

    @Autowired
    private RestTemplate restTemplate;

    @SuppressWarnings("null")
    @PostMapping("/post-job")
    public ResponseEntity<?> postJobToWebhook(@RequestBody Map<String, Object> jobData) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📤 POSTING JOB TO WEBHOOK.SITE");
        System.out.println("=".repeat(80));
        System.out.println("🌐 Webhook URL: " + WEBHOOK_URL);
        System.out.println("📦 Job Title: " + jobData.get("title"));
        System.out.println("🏢 Company: " + jobData.get("company"));
        System.out.println("🔗 Application URL: " + jobData.get("applicationUrl"));
        System.out.println("=".repeat(80));
        
        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");
            headers.set("User-Agent", "SmartHire-Backend/1.0");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(jobData, headers);
            
            // Post to webhook - webhook.site returns HTML, not JSON, but that's OK
            ResponseEntity<String> response = restTemplate.exchange(
                WEBHOOK_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            System.out.println("✅ Successfully posted to webhook.site");
            System.out.println("📊 Response status: " + response.getStatusCode());
            System.out.println("📄 View your data at: " + WEBHOOK_VIEW_URL);
            System.out.println("=".repeat(80) + "\n");
            
            // Always return success response for 2xx status codes
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Job posted to webhook.site successfully! ✅");
            result.put("webhookUrl", WEBHOOK_URL);
            result.put("viewUrl", WEBHOOK_VIEW_URL);
            result.put("status", response.getStatusCode().value());
            result.put("hint", "Open the view URL to see your posted data");
            
            return ResponseEntity.ok(result);
            
        } catch (HttpClientErrorException.Forbidden e) {
            // 403 Forbidden - URL might be expired
            System.err.println("❌ FORBIDDEN (403): " + e.getMessage());
            System.err.println("💡 The webhook.site URL may have expired.");
            System.err.println("📝 Get a new URL from: https://webhook.site");
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Webhook URL returned Forbidden (403)");
            error.put("statusCode", 403);
            error.put("error", "FORBIDDEN");
            error.put("hint", "The webhook.site URL may have expired. Get a new one from https://webhook.site");
            error.put("instructions", "1. Visit https://webhook.site\n2. Copy your new unique URL\n3. Update WEBHOOK_URL in WebhookController.java\n4. Restart backend");
            
            return ResponseEntity.status(HttpStatus.OK).body(error); // Return 200 but with error info
            
        } catch (HttpClientErrorException e) {
            // Other 4xx errors
            System.err.println("❌ CLIENT ERROR: " + e.getStatusCode() + " - " + e.getStatusText());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Webhook returned error: " + e.getStatusText());
            error.put("statusCode", e.getStatusCode().value());
            error.put("error", "CLIENT_ERROR");
            
            return ResponseEntity.ok(error); // Return 200 to avoid frontend error handling
            
        } catch (HttpServerErrorException e) {
            // 5xx errors
            System.err.println("❌ SERVER ERROR: " + e.getStatusCode() + " - " + e.getStatusText());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Webhook.site server error");
            error.put("statusCode", e.getStatusCode().value());
            error.put("error", "SERVER_ERROR");
            
            return ResponseEntity.ok(error); // Return 200 to avoid frontend error handling
            
        } catch (RestClientException e) {
            System.err.println("❌ NETWORK ERROR: " + e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Network error: " + e.getMessage());
            error.put("error", "NETWORK_ERROR");
            error.put("hint", "Check your internet connection and firewall settings");
            
            return ResponseEntity.ok(error); // Return 200 to avoid frontend error handling
            
        } catch (Exception e) {
            System.err.println("❌ UNEXPECTED ERROR: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Unexpected error: " + e.getMessage());
            error.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(error); // Return 200 to avoid frontend error handling
        }
    }

    @SuppressWarnings("null")
    @PostMapping("/post-application")
    public ResponseEntity<?> postApplicationToWebhook(@RequestBody Map<String, Object> applicationData) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📤 POSTING APPLICATION TO WEBHOOK.SITE");
        System.out.println("=".repeat(80));
        System.out.println("👤 Candidate: " + applicationData.get("candidateName"));
        System.out.println("📧 Email: " + applicationData.get("candidateEmail"));
        System.out.println("💼 Job: " + applicationData.get("jobTitle"));
        System.out.println("=".repeat(80));
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");
            headers.set("User-Agent", "SmartHire-Backend/1.0");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(applicationData, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                WEBHOOK_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            System.out.println("✅ Application posted successfully");
            System.out.println("📊 Status: " + response.getStatusCode());
            System.out.println("📄 View at: " + WEBHOOK_VIEW_URL);
            System.out.println("=".repeat(80) + "\n");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Application posted to webhook.site successfully!");
            result.put("webhookUrl", WEBHOOK_URL);
            result.put("viewUrl", WEBHOOK_VIEW_URL);
            result.put("status", response.getStatusCode().value());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to post application: " + e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to post application: " + e.getMessage());
            error.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(error); // Return 200 to avoid frontend error handling
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testWebhook() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🧪 TESTING WEBHOOK CONNECTION");
        System.out.println("=".repeat(80));
        System.out.println("🌐 Testing URL: " + WEBHOOK_URL);
        System.out.println("=".repeat(80));
        
        try {
            Map<String, Object> testData = Map.of(
                "event", "TEST_CONNECTION",
                "message", "Testing webhook from SmartHire backend",
                "timestamp", java.time.Instant.now().toString(),
                "source", "SmartHire Backend"
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "*/*");
            headers.set("User-Agent", "SmartHire-Backend/1.0");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(testData, headers);
            
            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(
                WEBHOOK_URL,
                HttpMethod.POST,
                request,
                String.class
            );
            
            System.out.println("✅ Test successful! Status: " + response.getStatusCode());
            System.out.println("📄 View at: " + WEBHOOK_VIEW_URL);
            System.out.println("=".repeat(80) + "\n");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Webhook connection successful! ✅");
            result.put("webhookUrl", WEBHOOK_URL);
            result.put("viewUrl", WEBHOOK_VIEW_URL);
            result.put("status", response.getStatusCode().value());
            
            return ResponseEntity.ok(result);
            
        } catch (HttpClientErrorException.Forbidden e) {
            System.err.println("❌ TEST FAILED: Forbidden (403)");
            System.err.println("💡 URL may have expired. Get new URL from https://webhook.site");
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Webhook test failed: Forbidden (403)");
            error.put("webhookUrl", WEBHOOK_URL);
            error.put("statusCode", 403);
            error.put("hint", "🔥 The webhook URL has expired! Get a new one from https://webhook.site");
            error.put("instructions", "1. Go to https://webhook.site\n2. Copy your unique URL\n3. Update WEBHOOK_URL in WebhookController.java\n4. Restart backend");
            
            return ResponseEntity.ok(error); // Return 200
            
        } catch (Exception e) {
            System.err.println("❌ Webhook test failed: " + e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Webhook test failed: " + e.getMessage());
            error.put("webhookUrl", WEBHOOK_URL);
            error.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.ok(error); // Return 200
        }
    }

    @GetMapping("/url")
    public ResponseEntity<?> getWebhookUrl() {
        Map<String, Object> response = new HashMap<>();
        response.put("webhookUrl", WEBHOOK_URL);
        response.put("viewUrl", WEBHOOK_VIEW_URL);
        response.put("message", "Use this URL to view posted data");
        
        return ResponseEntity.ok(response);
    }
}
