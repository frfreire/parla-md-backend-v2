package br.gov.md.parla_md_backend.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongoClientConfig {

    @Bean
    public MongoClientSettings mongoClientSettings() {
        ConnectionString connectionString = new ConnectionString("mongodb://mongodb:27017/parlamd");
        return MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings(builder ->
                        builder.maxSize(100)
                                .minSize(5)
                                .maxWaitTime(120000, TimeUnit.MILLISECONDS)
                                .maxConnectionLifeTime(0, TimeUnit.MILLISECONDS)
                                .maxConnectionIdleTime(0, TimeUnit.MILLISECONDS)
                                .maintenanceInitialDelay(0, TimeUnit.MILLISECONDS)
                                .maintenanceFrequency(60000, TimeUnit.MILLISECONDS)
                )
                .applyToClusterSettings(builder ->
                        builder.serverSelectionTimeout(30000, TimeUnit.MILLISECONDS)
                )
                .build();
    }
}
