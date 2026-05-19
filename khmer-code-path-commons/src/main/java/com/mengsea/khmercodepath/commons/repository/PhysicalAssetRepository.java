package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.PhysicalAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhysicalAssetRepository extends JpaRepository<PhysicalAsset, Long> {

    List<PhysicalAsset> findByDeletedFalseOrderByNameAsc();
}
