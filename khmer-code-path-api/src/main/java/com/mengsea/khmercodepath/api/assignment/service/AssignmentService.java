package com.mengsea.khmercodepath.api.assignment.service;

import com.mengsea.khmercodepath.api.assignment.payload.AssignmentDto;
import com.mengsea.khmercodepath.api.assignment.payload.AssignmentSubmissionDto;
import com.mengsea.khmercodepath.api.assignment.payload.CreateAssignmentRequest;
import com.mengsea.khmercodepath.api.assignment.payload.SubmitAssignmentRequest;

import java.util.List;

public interface AssignmentService {

    AssignmentDto create(CreateAssignmentRequest request);

    List<AssignmentDto> listForTeacher(Long classId);

    List<AssignmentDto> listAssigned();

    AssignmentDto getAssignment(Long assignmentId);

    AssignmentDto submit(Long assignmentId, SubmitAssignmentRequest request);

    List<AssignmentSubmissionDto> getSubmissions(Long assignmentId);

    void deleteAssignment(Long assignmentId);
}
