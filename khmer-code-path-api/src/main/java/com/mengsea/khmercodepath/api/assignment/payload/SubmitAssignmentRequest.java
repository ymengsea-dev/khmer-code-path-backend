package com.mengsea.khmercodepath.api.assignment.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAssignmentRequest {

    @NotBlank
    private String content;
}
