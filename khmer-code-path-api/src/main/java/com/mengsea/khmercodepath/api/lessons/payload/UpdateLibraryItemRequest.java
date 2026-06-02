package com.mengsea.khmercodepath.api.lessons.payload;

import com.mengsea.khmercodepath.commons.constant.LibraryIconType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLibraryItemRequest {

    @Size(max = 500)
    private String title;

    @Size(max = 65535)
    private String description;

    @Size(max = 128)
    private String moduleTag;

    private LibraryIconType iconType;

    @Size(max = 128)
    private String gradient;
}
