package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {

    Optional<School> findByIdAndDeletedFalse(Long id);

    Optional<School> findBySlugAndDeletedFalse(String slug);

    boolean existsBySlugIgnoreCaseAndDeletedFalseAndIdNot(String slug, Long id);

    boolean existsBySlugIgnoreCaseAndDeletedFalse(String slug);
}
