package com.mengsea.khmercodepath.api.classes.payload;

import com.mengsea.khmercodepath.commons.constant.InvitationStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ClassInvitationPayload {
    Long id;
    Long classId;
    String className;
    String classCode;
    String teacherName;
    String studentId;
    String studentName;
    String invitedByName;
    InvitationStatus status;
    LocalDateTime createdAt;
}
