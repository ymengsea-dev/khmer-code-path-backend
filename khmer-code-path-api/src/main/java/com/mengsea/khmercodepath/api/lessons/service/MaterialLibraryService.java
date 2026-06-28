package com.mengsea.khmercodepath.api.lessons.service;

import com.mengsea.khmercodepath.api.lessons.payload.AssignLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LessonDetailPayload;
import com.mengsea.khmercodepath.api.lessons.payload.LinkLibraryMaterialsRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LibraryMaterialPayload;
import com.mengsea.khmercodepath.api.lessons.payload.MaterialLibraryItemPayload;
import com.mengsea.khmercodepath.api.lessons.payload.UpdateLibraryItemRequest;
import com.mengsea.khmercodepath.commons.constant.LibraryIconType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MaterialLibraryService {

    List<MaterialLibraryItemPayload> listLibrary(String search, String moduleTag);

    MaterialLibraryItemPayload createLibraryItem(CreateLibraryItemRequest request);

    MaterialLibraryItemPayload getLibraryItem(Long libraryItemId);

    MaterialLibraryItemPayload updateLibraryItem(Long libraryItemId, UpdateLibraryItemRequest request);

    void uploadLibraryMaterials(Long libraryItemId, List<MultipartFile> files);

    List<LibraryMaterialPayload> listLibraryMaterials(Long libraryItemId);

    LessonDetailPayload assignToClass(Long libraryItemId, AssignLibraryItemRequest request);

    void deleteLibraryItem(Long libraryItemId);

    void deleteLibraryMaterial(Long libraryItemId, Long materialId);

    List<LibraryMaterialPayload> linkLibraryMaterials(Long targetLibraryItemId, LinkLibraryMaterialsRequest request);

    List<LibraryMaterialPayload> listPoolFiles(String search);

    void uploadPoolFiles(List<MultipartFile> files);

    void deletePoolFile(Long materialId);
}
