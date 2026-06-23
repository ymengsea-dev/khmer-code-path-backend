package com.mengsea.khmercodepath.api.schools.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolRegistrationInfoPayload {
    private Long schoolId;
    private String name;
    private String slug;
    private boolean registrationOpen;
    private String tagline;
    private String coverUrl;
    private boolean domainRequired;
    private List<String> allowedDomains;
}
