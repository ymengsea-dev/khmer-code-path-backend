package com.mengsea.khmercodepath.api.schools.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRegistrationDomainRequest {

    @NotBlank
    @Size(min = 3, max = 255)
    private String domain;
}
