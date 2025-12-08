package com.smarthire.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mongodb.client.result.UpdateResult;

import java.util.*;

@RestController
@RequestMapping("/api/fix")
@CrossOrigin(origins = "*")
public class DatabaseFixController {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * FIX DATABASE: Change all "published" status to "OPEN"
     * Access this endpoint to fix the database issue
     */
    @PostMapping("/database-status")
    public ResponseEntity<?> fixDatabaseStatus() {
        try {
            System.out.println("========================================");
            System.out.println("🔧 FIXING DATABASE STATUS VALUES");
            System.out.println("========================================");

            Map<String, Object> results = new HashMap<>();
            
            // Fix 1: published -> OPEN
            Query query1 = new Query(Criteria.where("status").is("published"));
            Update update1 = new Update().set("status", "OPEN");
            UpdateResult result1 = mongoTemplate.updateMulti(query1, update1, "jobs");
            long count1 = result1.getModifiedCount();
            System.out.println("✓ Changed 'published' to 'OPEN': " + count1 + " documents");
            results.put("published_to_OPEN", count1);

            // Fix 2: open -> OPEN
            Query query2 = new Query(Criteria.where("status").is("open"));
            Update update2 = new Update().set("status", "OPEN");
            UpdateResult result2 = mongoTemplate.updateMulti(query2, update2, "jobs");
            long count2 = result2.getModifiedCount();
            System.out.println("✓ Changed 'open' to 'OPEN': " + count2 + " documents");
            results.put("open_to_OPEN", count2);

            // Fix 3: closed -> CLOSED
            Query query3 = new Query(Criteria.where("status").is("closed"));
            Update update3 = new Update().set("status", "CLOSED");
            UpdateResult result3 = mongoTemplate.updateMulti(query3, update3, "jobs");
            long count3 = result3.getModifiedCount();
            System.out.println("✓ Changed 'closed' to 'CLOSED': " + count3 + " documents");
            results.put("closed_to_CLOSED", count3);

            // Fix 4: draft -> DRAFT
            Query query4 = new Query(Criteria.where("status").is("draft"));
            Update update4 = new Update().set("status", "DRAFT");
            UpdateResult result4 = mongoTemplate.updateMulti(query4, update4, "jobs");
            long count4 = result4.getModifiedCount();
            System.out.println("✓ Changed 'draft' to 'DRAFT': " + count4 + " documents");
            results.put("draft_to_DRAFT", count4);

            long totalFixed = count1 + count2 + count3 + count4;
            results.put("total_fixed", totalFixed);

            System.out.println("========================================");
            System.out.println("✅ DATABASE FIX COMPLETE!");
            System.out.println("Total documents fixed: " + totalFixed);
            System.out.println("========================================");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Database status values fixed successfully",
                "results", results,
                "totalFixed", totalFixed
            ));

        } catch (Exception e) {
            System.err.println("❌ Error fixing database: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to fix database",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * CHECK DATABASE: See what status values exist
     */
    @GetMapping("/check-status")
    public ResponseEntity<?> checkDatabaseStatus() {
        try {
            System.out.println("🔍 Checking database status values...");
            
            List<Map<String, Object>> statusCounts = mongoTemplate.getDb()
                .getCollection("jobs")
                .aggregate(Arrays.asList(
                    new org.bson.Document("$group", 
                        new org.bson.Document("_id", "$status")
                            .append("count", new org.bson.Document("$sum", 1))
                    )
                ))
                .into(new ArrayList<>())
                .stream()
                .map(doc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", doc.get("_id"));
                    map.put("count", doc.get("count"));
                    return map;
                })
                .toList();

            System.out.println("Current status values: " + statusCounts);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "statusCounts", statusCounts
            ));

        } catch (Exception e) {
            System.err.println("❌ Error checking database: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
