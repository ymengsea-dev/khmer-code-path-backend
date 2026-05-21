package com.mengsea.khmercodepath.api.notes.payload;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class NoteSummaryPayload {
    Long id;
    String title;
    String preview;
    List<String> tags;
    String sourceLabel;
    LocalDateTime updatedAt;
}
