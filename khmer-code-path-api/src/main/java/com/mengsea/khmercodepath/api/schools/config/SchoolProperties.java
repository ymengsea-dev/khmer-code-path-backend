package com.mengsea.khmercodepath.api.schools.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "lms.school")
public class SchoolProperties {

    private String publicBaseUrl = "http://localhost:3000";
    private String registrationPathPrefix = "/register/";
}
