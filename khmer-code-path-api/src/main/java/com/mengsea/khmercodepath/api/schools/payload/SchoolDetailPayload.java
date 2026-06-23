package com.mengsea.khmercodepath.api.schools.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDetailPayload {
    private Long id;
    private String name;
    private String slug;
    private String status;
    private boolean registrationOpen;
    private String tagline;
    private String coverUrl;
    private String registrationPath;
    private String registrationUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
