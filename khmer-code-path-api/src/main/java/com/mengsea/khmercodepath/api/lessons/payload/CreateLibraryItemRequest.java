package com.mengsea.khmercodepath.api.lessons.payload;

import com.mengsea.khmercodepath.commons.constant.LibraryIconType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLibraryItemRequest {

    @NotBlank
    @Size(max = 500)
    private String title;

    @Size(max = 128)
    private String moduleTag;

    @Size(max = 5000)
    private String description;

    private LibraryIconType iconType;

    @Size(max = 128)
    private String gradient;
}
