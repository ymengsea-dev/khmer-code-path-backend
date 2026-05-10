package com.mengsea.khmercodepath.api.classes.payload;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RemoveStudentsRequest {

    @NotEmpty
    private List<String> studentIds;
}
