package br.gov.md.parla_md_backend.config.old;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDBHealthCheck {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public CommandLineRunner mongoDbHealthCheck() {
        return args -> {
            System.out.println("Checking MongoDB connection...");
            System.out.println("MongoDB URI: " + mongoUri); // Log the URI for debugging
            try (MongoClient mongoClient = MongoClients.create(mongoUri)) {
                for (int i = 0; i < 30; i++) { // Try for 5 minutes
                    try {
                        // Execute a simple command to check if MongoDB is up
                        mongoClient.getDatabase("admin").runCommand(new Document("ping", 1));
                        System.out.println("Successfully connected to MongoDB");
                        return;
                    } catch (Exception e) {
                        System.out.println("Cannot connect to MongoDB, retrying in 10 seconds...");
                        System.out.println("Error: " + e.getMessage()); // Log the error message
                        Thread.sleep(10000); // Wait for 10 seconds before retrying
                    }
                }
                throw new RuntimeException("Could not connect to MongoDB after 30 attempts");
            }
        };
    }
}