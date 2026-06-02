package com.mengsea.khmercodepath.api.lessons.payload;

import com.mengsea.khmercodepath.commons.constant.LibraryIconType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MaterialLibraryConfigPayload {
    private List<LibraryViewPayload> views;
    private LibraryCreateDefaultsPayload createDefaults;
    private String uploadAccept;
    private String filePoolLabel;
}
