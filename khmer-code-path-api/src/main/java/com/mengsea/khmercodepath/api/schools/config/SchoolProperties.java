package com.mengsea.khmercodepath.api.schools.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "lms.school")
public class SchoolProperties {

    private String publicBaseUrl = "http://localhost:3000";
    private String profileSectionTitle = "School profile";
    private String profileSectionDescription =
            "Configure your public student registration portal — share the link, branding, and signup settings.";
    private String nameLabel = "School name";
    private String slugLabel = "Portal URL slug";
    private String taglineLabel = "Tagline";
    private String taglinePlaceholder = "Short welcome message on your registration page";
    private String registrationOpenLabel = "Registration open";
    private String saveProfileLabel = "Save profile";
    private String coverImageLabel = "Cover image";
    private String coverImageDescription =
            "Shown as the background when students open your registration link.";
    private String uploadCoverLabel = "Upload cover";
    private String removeCoverLabel = "Remove cover";
    private String registrationUrlLabel = "Student registration link";
    private String copyUrlLabel = "Copy link";
    private String copiedUrlMessage = "Link copied!";
    private String registrationPathPrefix = "/register/";
}
