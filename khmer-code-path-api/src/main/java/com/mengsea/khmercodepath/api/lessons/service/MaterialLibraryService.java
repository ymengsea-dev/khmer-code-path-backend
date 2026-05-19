package com.mengsea.khmercodepath.api.lessons.service;

import com.mengsea.khmercodepath.api.lessons.payload.AssignLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LessonDetailPayload;
import com.mengsea.khmercodepath.api.lessons.payload.MaterialLibraryItemPayload;
import com.mengsea.khmercodepath.commons.constant.LibraryIconType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MaterialLibraryService {

    List<MaterialLibraryItemPayload> listLibrary(String search, String moduleTag);

    MaterialLibraryItemPayload createLibraryItem(CreateLibraryItemRequest request);

    void uploadLibraryMaterials(Long libraryItemId, List<MultipartFile> files);

    LessonDetailPayload assignToClass(Long libraryItemId, AssignLibraryItemRequest request);
}
