package com.mengsea.khmercodepath.api.courses.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseTechnologyPayload {

    @NotBlank
    @Size(max = 128)
    private String name;

    @NotBlank
    @Size(max = 16)
    private String color;
}
