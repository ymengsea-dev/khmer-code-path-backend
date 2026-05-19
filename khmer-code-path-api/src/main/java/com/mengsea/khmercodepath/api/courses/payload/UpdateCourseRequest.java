package com.mengsea.khmercodepath.api.courses.payload;

import com.mengsea.khmercodepath.commons.constant.CourseLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCourseRequest {

    @Size(max = 500)
    private String title;

    @Size(max = 255)
    private String institution;

    @Size(max = 64)
    private String institutionLogo;

    @Size(max = 16)
    private String institutionColor;

    private CourseLevel level;

    private Integer pts;

    @Size(max = 128)
    private String bgColor;

    @Size(max = 2048)
    private String imageUrl;

    @Size(max = 10_000)
    private String description;

    @Valid
    private List<CourseTechnologyPayload> technologies;

    @Size(max = 500)
    private String prerequisite;

    @Size(max = 255)
    private String achievement;

    private Boolean locked;

    private Boolean published;
}
