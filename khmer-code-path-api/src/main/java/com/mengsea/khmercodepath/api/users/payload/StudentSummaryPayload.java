package com.mengsea.khmercodepath.api.users.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryPayload {
    private String id;
    private String name;
    private String email;
    private String studentId;
    private boolean isActive;
    private String avatarUrl;
    private String enrolledClasses;
    private List<String> enrolledClassIds;
}
