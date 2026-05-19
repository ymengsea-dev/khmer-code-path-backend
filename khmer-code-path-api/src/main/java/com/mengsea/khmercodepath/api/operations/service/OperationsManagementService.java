package com.mengsea.khmercodepath.api.operations.service;

import com.mengsea.khmercodepath.api.operations.payload.CreatePhysicalAssetRequest;
import com.mengsea.khmercodepath.api.operations.payload.FacultyRequestPayload;
import com.mengsea.khmercodepath.api.operations.payload.InfrastructurePayload;
import com.mengsea.khmercodepath.api.operations.payload.PhysicalAssetPayload;
import com.mengsea.khmercodepath.api.operations.payload.UpdateFacultyRequestStatusRequest;
import com.mengsea.khmercodepath.commons.constant.RequestStatus;

import java.util.List;

public interface OperationsManagementService {

    List<PhysicalAssetPayload> listInventory();

    PhysicalAssetPayload createAsset(CreatePhysicalAssetRequest request);

    List<FacultyRequestPayload> listRequests(RequestStatus status);

    FacultyRequestPayload updateRequestStatus(Long id, UpdateFacultyRequestStatusRequest request);

    InfrastructurePayload getInfrastructure();
}
