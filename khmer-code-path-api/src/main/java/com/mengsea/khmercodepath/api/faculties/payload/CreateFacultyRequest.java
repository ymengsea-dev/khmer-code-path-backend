package com.mengsea.khmercodepath.api.faculties.payload;

import com.mengsea.khmercodepath.commons.constant.FacultyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateFacultyRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    private FacultyStatus status;
}
