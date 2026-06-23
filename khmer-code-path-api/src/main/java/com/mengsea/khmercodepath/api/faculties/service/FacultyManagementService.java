package com.mengsea.khmercodepath.api.faculties.service;

import com.mengsea.khmercodepath.api.faculties.payload.CreateFacultyRequest;
import com.mengsea.khmercodepath.api.faculties.payload.FacultyConfigPayload;
import com.mengsea.khmercodepath.api.faculties.payload.FacultySummaryPayload;
import com.mengsea.khmercodepath.api.faculties.payload.UpdateFacultyRequest;

import java.util.List;

import com.mengsea.khmercodepath.commons.domain.School;

public interface FacultyManagementService {

    FacultyConfigPayload getConfig();

    List<FacultySummaryPayload> listFaculties();

    FacultySummaryPayload createFaculty(CreateFacultyRequest request);

    FacultySummaryPayload updateFaculty(Long id, UpdateFacultyRequest request);

    FacultySummaryPayload getFacultySummary(School school, Long facultyId);
}
