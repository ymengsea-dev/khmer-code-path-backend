package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.constant.RequestStatus;
import com.mengsea.khmercodepath.commons.domain.FacultyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyRequestRepository extends JpaRepository<FacultyRequest, Long> {

    List<FacultyRequest> findByDeletedFalseOrderByCreatedAtDesc();

    List<FacultyRequest> findByDeletedFalseAndStatusOrderByCreatedAtDesc(RequestStatus status);

    Optional<FacultyRequest> findByIdAndDeletedFalse(Long id);
}
