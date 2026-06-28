package com.mengsea.khmercodepath.api.exam.service;

import com.mengsea.khmercodepath.api.exam.payload.CreateExamRequest;
import com.mengsea.khmercodepath.api.exam.payload.ExamAttemptResultDto;
import com.mengsea.khmercodepath.api.exam.payload.ExamDto;
import com.mengsea.khmercodepath.api.exam.payload.ExamResultsDto;
import com.mengsea.khmercodepath.api.exam.payload.SubmitExamAnswersRequest;

import java.util.List;

public interface ExamService {

    ExamDto create(CreateExamRequest request);

    List<ExamDto> listForTeacher(Long classId);

    List<ExamDto> listAssigned();

    ExamDto getExam(Long examId);

    ExamAttemptResultDto submit(Long examId, SubmitExamAnswersRequest request);

    void fail(Long examId, String reason);

    ExamResultsDto getResults(Long examId);

    void deleteExam(Long examId);
}
