package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.constant.MaterialSourceType;
import com.mengsea.khmercodepath.commons.domain.MaterialRagIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialRagIndexRepository extends JpaRepository<MaterialRagIndex, Long> {

    Optional<MaterialRagIndex> findBySourceTypeAndSourceId(MaterialSourceType sourceType, Long sourceId);
}
