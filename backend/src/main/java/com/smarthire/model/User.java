package com.smarthire.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users") // MongoDB collection name
public class User {

    @Id
    private String id; // MongoDB uses String/ObjectId instead of Long

    @Indexed(unique = true)
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String phoneNumber;
    private String company;
    private String resumeText;
    private String skills;
    private String experience;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Manual lifecycle methods
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum UserRole {
        ADMIN,
        EMPLOYER,
        CANDIDATE
    }
}
