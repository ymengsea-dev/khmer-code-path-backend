package com.mengsea.khmercodepath.api.classes.payload;

import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateClassRequest {

    @NotBlank
    @Size(max = 128)
    private String code;

    @NotBlank
    @Size(max = 500)
    private String name;

    @Size(max = 10_000)
    private String description;

    @NotBlank
    private String teacherId;

    @Size(max = 128)
    private String semester;

    private Integer academicYear;

    @Size(max = 255)
    private String schedule;

    @Size(max = 128)
    private String roomNumber;

    /** Optional; defaults to ACTIVE when null. */
    private ClassStatus status;
}
