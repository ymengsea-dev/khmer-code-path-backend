package com.mengsea.khmercodepath.api.lessons.payload;

import com.mengsea.khmercodepath.commons.constant.LibraryIconType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LibraryCreateDefaultsPayload {
    private String title;
    private LibraryIconType iconType;
    private String gradient;
}
