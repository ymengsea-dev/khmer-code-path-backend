package com.mengsea.khmercodepath.api.llm.config;

import com.mengsea.khmercodepath.api.llm.service.RAGService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class DataIngestionConfig {
    @Bean
    CommandLineRunner ingestOnStartup(RAGService ragService) {
        return args -> {
            System.out.println("Ingesting documents on startup...");
            ragService.ingestDocument();
            System.out.println("Document ingestion completed! Application is ready.");
        };
    }
}
