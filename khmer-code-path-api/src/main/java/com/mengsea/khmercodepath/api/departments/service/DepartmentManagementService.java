package com.mengsea.khmercodepath.api.departments.service;

import com.mengsea.khmercodepath.api.departments.payload.CreateDepartmentRequest;
import com.mengsea.khmercodepath.api.departments.payload.DepartmentDetailPayload;
import com.mengsea.khmercodepath.api.departments.payload.DepartmentOptionPayload;
import com.mengsea.khmercodepath.api.departments.payload.DepartmentSummaryPayload;
import com.mengsea.khmercodepath.api.departments.payload.UpdateDepartmentRequest;

import com.mengsea.khmercodepath.commons.domain.School;

import java.util.List;

public interface DepartmentManagementService {

    List<DepartmentSummaryPayload> listDepartments();

    List<DepartmentOptionPayload> listDepartmentOptions();

    DepartmentDetailPayload getDepartment(Long id);

    DepartmentSummaryPayload createDepartment(CreateDepartmentRequest request);

    DepartmentSummaryPayload updateDepartment(Long id, UpdateDepartmentRequest request);

    com.mengsea.khmercodepath.commons.domain.Department requireDepartmentForSchool(Long departmentId, School school);
}
