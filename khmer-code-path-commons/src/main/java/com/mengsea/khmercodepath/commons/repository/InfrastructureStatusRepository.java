package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.InfrastructureStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InfrastructureStatusRepository extends JpaRepository<InfrastructureStatus, Long> {

    List<InfrastructureStatus> findByDeletedFalseAndCategoryOrderBySortOrderAsc(String category);
}
