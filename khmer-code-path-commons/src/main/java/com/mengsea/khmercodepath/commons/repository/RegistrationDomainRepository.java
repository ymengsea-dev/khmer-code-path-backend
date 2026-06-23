package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.RegistrationDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationDomainRepository extends JpaRepository<RegistrationDomain, Long> {

    List<RegistrationDomain> findBySchool_IdAndDeletedFalseOrderByDomainAsc(Long schoolId);

    Optional<RegistrationDomain> findByIdAndSchool_IdAndDeletedFalse(Long id, Long schoolId);

    Optional<RegistrationDomain> findByDomainIgnoreCaseAndDeletedFalse(String domain);

    boolean existsByDomainIgnoreCaseAndDeletedFalseAndIdNot(String domain, Long id);

    boolean existsByDomainIgnoreCaseAndDeletedFalse(String domain);

    boolean existsBySchool_IdAndDeletedFalse(Long schoolId);
}
