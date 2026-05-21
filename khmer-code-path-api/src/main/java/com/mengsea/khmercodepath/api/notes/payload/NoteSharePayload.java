package com.mengsea.khmercodepath.api.notes.payload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NoteSharePayload {
    String shareToken;
    String sharePath;
}
