package com.mengsea.khmercodepath.api.lessons.payload;

import com.mengsea.khmercodepath.commons.constant.LibraryIconType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MaterialLibraryItemPayload {
    private Long id;
    private String title;
    private String moduleTag;
    private String description;
    private LibraryIconType iconType;
    private String gradient;
    private long assetCount;
    private List<LibraryMaterialPayload> materials;
    private LocalDateTime updatedAt;
}
