package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByDeletedFalseOrderByNameAsc();

    Optional<Department> findByIdAndDeletedFalse(Long id);

    long countByDeletedFalse();
}
