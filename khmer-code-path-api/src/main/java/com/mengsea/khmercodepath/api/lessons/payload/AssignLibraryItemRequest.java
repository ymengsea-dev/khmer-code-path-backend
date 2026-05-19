package com.mengsea.khmercodepath.api.lessons.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignLibraryItemRequest {

    @NotNull
    private Long targetClassId;
}
