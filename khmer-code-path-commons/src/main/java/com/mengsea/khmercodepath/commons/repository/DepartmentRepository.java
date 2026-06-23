package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findBySchool_IdAndDeletedFalseOrderByNameAsc(Long schoolId);

    Optional<Department> findByIdAndSchool_IdAndDeletedFalse(Long id, Long schoolId);

    Optional<Department> findByIdAndDeletedFalse(Long id);

    long countByDeletedFalse();

    long countByFacultyEntity_IdAndDeletedFalse(Long facultyId);
}
