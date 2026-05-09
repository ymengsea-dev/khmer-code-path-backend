package com.mengsea.khmercodepath.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.mengsea.khmercodepath.commons.domain")
@EnableJpaRepositories(basePackages = "com.mengsea.khmercodepath.commons.repository")
@ComponentScan(basePackages = {
        "com.mengsea.khmercodepath.commons",
        "com.mengsea.khmercodepath.api"
})
public class KhmerCodePathApplication {

    public static void main(String[] args) {
        SpringApplication.run(KhmerCodePathApplication.class, args);
    }
}
