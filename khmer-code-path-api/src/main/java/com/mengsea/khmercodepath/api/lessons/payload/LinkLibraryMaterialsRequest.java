package com.mengsea.khmercodepath.api.lessons.payload;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class LinkLibraryMaterialsRequest {

    @NotEmpty
    private List<Long> sourceMaterialIds;
}
