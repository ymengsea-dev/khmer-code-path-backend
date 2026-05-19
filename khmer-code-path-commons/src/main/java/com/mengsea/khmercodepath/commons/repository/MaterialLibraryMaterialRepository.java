package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.MaterialLibraryMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialLibraryMaterialRepository extends JpaRepository<MaterialLibraryMaterial, Long> {

    List<MaterialLibraryMaterial> findByLibraryItem_IdAndDeletedFalseOrderByCreatedAtAsc(Long libraryItemId);

    long countByLibraryItem_IdAndDeletedFalse(Long libraryItemId);

    Optional<MaterialLibraryMaterial> findByIdAndDeletedFalse(Long id);
}
