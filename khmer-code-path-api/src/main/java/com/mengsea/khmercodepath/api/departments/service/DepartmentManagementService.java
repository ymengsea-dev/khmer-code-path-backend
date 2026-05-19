package com.mengsea.khmercodepath.api.departments.service;

import com.mengsea.khmercodepath.api.departments.payload.CreateDepartmentRequest;
import com.mengsea.khmercodepath.api.departments.payload.DepartmentDetailPayload;
import com.mengsea.khmercodepath.api.departments.payload.DepartmentSummaryPayload;
import com.mengsea.khmercodepath.api.departments.payload.UpdateDepartmentRequest;

import java.util.List;

public interface DepartmentManagementService {

    List<DepartmentSummaryPayload> listDepartments();

    DepartmentDetailPayload getDepartment(Long id);

    DepartmentSummaryPayload createDepartment(CreateDepartmentRequest request);

    DepartmentSummaryPayload updateDepartment(Long id, UpdateDepartmentRequest request);
}
