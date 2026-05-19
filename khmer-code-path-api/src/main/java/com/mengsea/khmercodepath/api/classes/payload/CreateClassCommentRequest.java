package com.mengsea.khmercodepath.api.classes.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateClassCommentRequest {

    @NotBlank
    @Size(max = 4000)
    private String body;
}
