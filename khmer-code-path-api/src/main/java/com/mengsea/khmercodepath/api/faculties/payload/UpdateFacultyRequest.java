package com.mengsea.khmercodepath.api.faculties.payload;

import com.mengsea.khmercodepath.commons.constant.FacultyStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFacultyRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 512)
    private String tagline;

    private FacultyStatus status;
}
