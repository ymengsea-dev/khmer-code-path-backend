package com.mengsea.khmercodepath.api.notes.payload;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

/** Read-only note opened via share link (any authenticated app user). */
@Value
@Builder
public class SharedNotePayload {
    Long id;
    String title;
    String bodyHtml;
    String preview;
    List<String> tags;
    String sourceLabel;
    String ownerDisplayName;
    LocalDateTime updatedAt;
}
