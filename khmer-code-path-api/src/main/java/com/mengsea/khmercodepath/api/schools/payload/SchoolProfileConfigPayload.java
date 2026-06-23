package com.mengsea.khmercodepath.api.schools.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolProfileConfigPayload {
    private String profileSectionTitle;
    private String profileSectionDescription;
    private String nameLabel;
    private String slugLabel;
    private String taglineLabel;
    private String taglinePlaceholder;
    private String registrationOpenLabel;
    private String saveProfileLabel;
    private String coverImageLabel;
    private String coverImageDescription;
    private String uploadCoverLabel;
    private String removeCoverLabel;
    private String registrationUrlLabel;
    private String copyUrlLabel;
    private String copiedUrlMessage;
    private String registrationPathPrefix;
}
