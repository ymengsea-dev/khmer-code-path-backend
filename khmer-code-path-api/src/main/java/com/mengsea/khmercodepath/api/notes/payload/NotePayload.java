package com.mengsea.khmercodepath.api.notes.payload;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class NotePayload {
    Long id;
    String title;
    String bodyHtml;
    String preview;
    List<String> tags;
    String sourceLabel;
    Long lessonId;
    Long materialId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
