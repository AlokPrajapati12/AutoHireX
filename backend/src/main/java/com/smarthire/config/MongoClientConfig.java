package com.smarthire.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Configuration
public class MongoClientConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @SuppressWarnings("null")
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @SuppressWarnings("null")
    @Override
    public MongoClient mongoClient() {
        try {
            // Create trust manager that accepts all certificates (for development only!)
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            ConnectionString connectionString = new ConnectionString(mongoUri);
            
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToSslSettings(builder -> {
                        builder.enabled(true);
                        builder.invalidHostNameAllowed(true);
                        builder.context(sslContext);
                    })
                    .applyToConnectionPoolSettings(builder -> 
                        builder.maxConnectionIdleTime(60000, TimeUnit.MILLISECONDS)
                               .maxSize(50)
                               .minSize(5)
                    )
                    .applyToSocketSettings(builder -> 
                        builder.connectTimeout(30000, TimeUnit.MILLISECONDS)
                               .readTimeout(30000, TimeUnit.MILLISECONDS)
                    )
                    .applyToClusterSettings(builder ->
                        builder.serverSelectionTimeout(30000, TimeUnit.MILLISECONDS)
                    )
                    .build();

            System.out.println("✅ MongoClient configured with custom SSL settings");
            return MongoClients.create(settings);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to configure MongoDB client: " + e.getMessage());
            e.printStackTrace();
            // Fallback to default configuration
            return MongoClients.create(mongoUri);
        }
    }

    @SuppressWarnings("null")
    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}
