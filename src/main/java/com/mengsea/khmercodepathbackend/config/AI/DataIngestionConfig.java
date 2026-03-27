package com.mengsea.khmercodepathbackend.config.AI;

import com.mengsea.khmercodepathbackend.services.ai.RAGService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataIngestionConfig {
    @Bean
    CommandLineRunner ingestOnStartup(RAGService ragService){
        return args -> {
            System.out.println("Ingesting documents on startup...");
            ragService.ingestDocument();
            System.out.println("Document ingestion completed! Application is ready.");
        };
    }
}
