package com.mengsea.khmercodepath.api.classes.payload;

import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateClassRequest {

    @Size(max = 128)
    private String code;

    @Size(max = 500)
    private String name;

    @Size(max = 10_000)
    private String description;

    private String teacherId;

    @Size(max = 128)
    private String semester;

    private Integer academicYear;

    @Size(max = 255)
    private String schedule;

    @Size(max = 128)
    private String roomNumber;

    private ClassStatus status;
}
