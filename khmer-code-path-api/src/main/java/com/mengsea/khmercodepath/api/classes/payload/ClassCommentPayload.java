package com.mengsea.khmercodepath.api.classes.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassCommentPayload {
    private Long id;
    private Long classId;
    private String className;
    private String authorId;
    private String authorName;
    private String authorRole;
    private String body;
    private LocalDateTime createdAt;
}
