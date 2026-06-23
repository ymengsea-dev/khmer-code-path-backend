package com.mengsea.khmercodepath.api.schools.payload;

import com.mengsea.khmercodepath.commons.constant.SchoolStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSchoolRequest {

    @Size(min = 2, max = 255)
    private String name;

    @Size(min = 2, max = 128)
    private String slug;

    private SchoolStatus status;

    private Boolean registrationOpen;

    @Size(max = 512)
    private String tagline;
}
