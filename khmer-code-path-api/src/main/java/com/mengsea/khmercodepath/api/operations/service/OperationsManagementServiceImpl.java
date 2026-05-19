package com.mengsea.khmercodepath.api.operations.service;

import com.mengsea.khmercodepath.api.operations.payload.CreatePhysicalAssetRequest;
import com.mengsea.khmercodepath.api.operations.payload.FacultyRequestPayload;
import com.mengsea.khmercodepath.api.operations.payload.InfrastructurePayload;
import com.mengsea.khmercodepath.api.operations.payload.InfrastructureRowPayload;
import com.mengsea.khmercodepath.api.operations.payload.PhysicalAssetPayload;
import com.mengsea.khmercodepath.api.operations.payload.UpdateFacultyRequestStatusRequest;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.RequestStatus;
import com.mengsea.khmercodepath.commons.domain.FacultyRequest;
import com.mengsea.khmercodepath.commons.domain.InfrastructureStatus;
import com.mengsea.khmercodepath.commons.domain.PhysicalAsset;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.FacultyRequestRepository;
import com.mengsea.khmercodepath.commons.repository.InfrastructureStatusRepository;
import com.mengsea.khmercodepath.commons.repository.PhysicalAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationsManagementServiceImpl implements OperationsManagementService {

    private final PhysicalAssetRepository physicalAssetRepository;
    private final FacultyRequestRepository facultyRequestRepository;
    private final InfrastructureStatusRepository infrastructureStatusRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PhysicalAssetPayload> listInventory() {
        return physicalAssetRepository.findByDeletedFalseOrderByNameAsc().stream()
                .map(this::toAssetPayload)
                .toList();
    }

    @Override
    @Transactional
    public PhysicalAssetPayload createAsset(CreatePhysicalAssetRequest request) {
        PhysicalAsset entity = new PhysicalAsset();
        entity.setName(request.getName().trim());
        entity.setCategory(request.getCategory().trim());
        entity.setStatus(request.getStatus());
        entity.setLocation(request.getLocation().trim());
        entity.setAssignedTo(blankToNull(request.getAssignedTo()));
        entity.setDeleted(false);
        physicalAssetRepository.save(entity);
        return toAssetPayload(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyRequestPayload> listRequests(RequestStatus status) {
        List<FacultyRequest> rows = status != null
                ? facultyRequestRepository.findByDeletedFalseAndStatusOrderByCreatedAtDesc(status)
                : facultyRequestRepository.findByDeletedFalseOrderByCreatedAtDesc();
        return rows.stream().map(this::toRequestPayload).toList();
    }

    @Override
    @Transactional
    public FacultyRequestPayload updateRequestStatus(Long id, UpdateFacultyRequestStatusRequest request) {
        FacultyRequest entity = facultyRequestRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.OPERATIONS_REQUEST_NOT_FOUND));
        if (request.getStatus() != RequestStatus.APPROVED && request.getStatus() != RequestStatus.REJECTED) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        entity.setStatus(request.getStatus());
        entity.setAdminComment(blankToNull(request.getAdminComment()));
        facultyRequestRepository.save(entity);
        return toRequestPayload(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public InfrastructurePayload getInfrastructure() {
        List<InfrastructureRowPayload> system = infrastructureStatusRepository
                .findByDeletedFalseAndCategoryOrderBySortOrderAsc("SYSTEM")
                .stream()
                .map(this::toInfraRow)
                .toList();
        List<InfrastructureRowPayload> facility = infrastructureStatusRepository
                .findByDeletedFalseAndCategoryOrderBySortOrderAsc("FACILITY")
                .stream()
                .map(this::toInfraRow)
                .toList();
        return InfrastructurePayload.builder()
                .systemHealth(system)
                .facilityStatus(facility)
                .build();
    }

    private PhysicalAssetPayload toAssetPayload(PhysicalAsset asset) {
        return PhysicalAssetPayload.builder()
                .id(asset.getId())
                .name(asset.getName())
                .category(asset.getCategory())
                .status(asset.getStatus())
                .location(asset.getLocation())
                .assignedTo(asset.getAssignedTo())
                .build();
    }

    private FacultyRequestPayload toRequestPayload(FacultyRequest req) {
        return FacultyRequestPayload.builder()
                .id(req.getId())
                .title(req.getTitle())
                .requester(req.getRequesterName())
                .detail(req.getDetail())
                .icon(req.getIconType())
                .status(req.getStatus())
                .build();
    }

    private InfrastructureRowPayload toInfraRow(InfrastructureStatus row) {
        return InfrastructureRowPayload.builder()
                .label(row.getLabel())
                .status(row.getStatusText())
                .variant(row.getVariant())
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
