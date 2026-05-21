package com.mengsea.khmercodepath.api.notes.payload;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class NoteListPayload {
    List<NoteSummaryPayload> items;
    int total;
}
