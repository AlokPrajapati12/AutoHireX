package com.smarthire.config;

import com.smarthire.model.Job;
import com.smarthire.model.JobApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

@Configuration
public class MongoConfig {

    /**
     * Event listener for Job entity to set timestamps automatically
     */
    @Bean
    public AbstractMongoEventListener<Job> jobBeforeConvertListener() {
        return new AbstractMongoEventListener<Job>() {
            @Override
            public void onBeforeConvert(@SuppressWarnings("null") BeforeConvertEvent<Job> event) {
                Job job = event.getSource();
                if (job.getId() == null) {
                    // New document - set creation timestamp
                    job.onCreate();
                } else {
                    // Existing document - update timestamp
                    job.onUpdate();
                }
            }
        };
    }

    /**
     * Event listener for JobApplication entity to set timestamps automatically
     */
    @Bean
    public AbstractMongoEventListener<JobApplication> applicationBeforeConvertListener() {
        return new AbstractMongoEventListener<JobApplication>() {
            @Override
            public void onBeforeConvert(@SuppressWarnings("null") BeforeConvertEvent<JobApplication> event) {
                JobApplication application = event.getSource();
                if (application.getId() == null) {
                    // New document - set creation timestamp
                    application.onCreate();
                } else {
                    // Existing document - update timestamp
                    application.onUpdate();
                }
            }
        };
    }
}
