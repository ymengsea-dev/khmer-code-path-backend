package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.MaterialLibraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaterialLibraryItemRepository extends JpaRepository<MaterialLibraryItem, Long> {

    List<MaterialLibraryItem> findByTeacher_UuidAndDeletedFalseOrderByUpdatedAtDesc(String teacherUuid);

    @Query("""
            SELECT i FROM MaterialLibraryItem i
            WHERE i.deleted = false
            AND i.teacher.uuid = :teacherUuid
            AND (:moduleTag IS NULL OR :moduleTag = '' OR LOWER(i.moduleTag) = LOWER(:moduleTag))
            AND (:search IS NULL OR :search = ''
                OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(i.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY i.updatedAt DESC
            """)
    List<MaterialLibraryItem> searchByTeacher(
            @Param("teacherUuid") String teacherUuid,
            @Param("search") String search,
            @Param("moduleTag") String moduleTag
    );

    Optional<MaterialLibraryItem> findByIdAndDeletedFalse(Long id);

    Optional<MaterialLibraryItem> findByIdAndTeacher_UuidAndDeletedFalse(Long id, String teacherUuid);
}
