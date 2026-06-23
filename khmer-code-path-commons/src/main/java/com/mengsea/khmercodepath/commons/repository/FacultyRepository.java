package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    List<Faculty> findBySchool_IdAndDeletedFalseOrderByNameAsc(Long schoolId);

    Optional<Faculty> findByIdAndSchool_IdAndDeletedFalse(Long id, Long schoolId);

    boolean existsBySchool_IdAndNameIgnoreCaseAndDeletedFalse(Long schoolId, String name);

    boolean existsBySchool_IdAndNameIgnoreCaseAndDeletedFalseAndIdNot(Long schoolId, String name, Long id);
}
