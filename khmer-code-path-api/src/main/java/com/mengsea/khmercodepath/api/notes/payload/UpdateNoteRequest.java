package com.mengsea.khmercodepath.api.notes.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateNoteRequest {

    @NotBlank
    @Size(max = 500)
    private String title;

    private String bodyHtml;

    @Size(max = 255)
    private String sourceLabel;

    private Long lessonId;

    private Long materialId;

    private List<@Size(max = 50) String> tags;

    private Boolean favorite;
}
