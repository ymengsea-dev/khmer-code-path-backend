package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.LmsClass;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LmsClassRepository extends JpaRepository<LmsClass, Long>, JpaSpecificationExecutor<LmsClass> {

    @EntityGraph(attributePaths = "teacher")
    Optional<LmsClass> findByIdAndDeletedFalse(Long id);

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndDeletedFalseAndIdNot(String code, Long id);

    long countByDeletedFalse();

    long countByTeacher_UuidAndDeletedFalseAndStatus(
            String teacherUuid,
            com.mengsea.khmercodepath.commons.constant.ClassStatus status
    );
}
