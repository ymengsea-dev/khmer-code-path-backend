package com.mengsea.khmercodepath.api.assignment.service;

import com.mengsea.khmercodepath.api.assignment.payload.AssignmentDto;
import com.mengsea.khmercodepath.api.assignment.payload.AssignmentSubmissionDto;
import com.mengsea.khmercodepath.api.assignment.payload.CreateAssignmentRequest;
import com.mengsea.khmercodepath.api.assignment.payload.SubmitAssignmentRequest;
import com.mengsea.khmercodepath.api.assignmentsexams.payload.TaskContentBlockDto;
import com.mengsea.khmercodepath.api.assignmentsexams.service.TaskContentBlockService;
import com.mengsea.khmercodepath.api.grades.service.ClassWeightedGradeService;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.Assignment;
import com.mengsea.khmercodepath.commons.domain.AssignmentSubmission;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.AssignmentRepository;
import com.mengsea.khmercodepath.commons.repository.AssignmentSubmissionRepository;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final LmsClassRepository lmsClassRepository;
    private final UserRepository userRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final TaskContentBlockService taskContentBlockService;
    private final ClassWeightedGradeService classWeightedGradeService;

    @Override
    @Transactional
    public AssignmentDto create(CreateAssignmentRequest request) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();

        LmsClass lmsClass = lmsClassRepository.findByIdAndDeletedFalse(request.getClassId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));

        if (!lmsClass.getTeacher().getUuid().equals(teacherUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        taskContentBlockService.validateForTeacher(request.getContentBlocks(), teacherUuid);

        Assignment assignment = new Assignment();
        assignment.setLmsClass(lmsClass);
        assignment.setTitle(request.getTitle().trim());
        assignment.setDescription(request.getDescription());
        assignment.setInstructions(request.getInstructions());
        assignment.setDueAt(request.getDueAt());
        assignment.setContentBlocksJson(taskContentBlockService.serialize(request.getContentBlocks()));
        assignment.setStatus("PUBLISHED");
        assignment = assignmentRepository.save(assignment);

        return toDtoWithTeacherCounts(assignment, null, null, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentDto> listForTeacher(Long classId) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();
        List<Assignment> assignments = (classId != null && classId > 0)
                ? assignmentRepository.findByClassAndTeacher(classId, teacherUuid)
                : assignmentRepository.findAllByTeacherUuid(teacherUuid);
        return assignments.stream()
                .map(a -> toDtoWithTeacherCounts(a, null, null, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentDto> listAssigned() {
        String studentUuid = SecurityUtils.requireCurrentUser().getUuid();
        List<Assignment> assignments = assignmentRepository.findPublishedForStudent(studentUuid);
        return assignments.stream().map(a -> {
            var submission = assignmentSubmissionRepository
                    .findByAssignment_IdAndStudent_Uuid(a.getId(), studentUuid);
            String subStatus = submission.map(AssignmentSubmission::getStatus).orElse(null);
            BigDecimal scorePercent = submission.map(AssignmentSubmission::getScorePercent).orElse(null);
            return toDto(a, subStatus, null, false, scorePercent);
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentDto getAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findByIdAndDeletedFalse(assignmentId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.ASSIGNMENT_NOT_FOUND));

        String currentUuid = SecurityUtils.requireCurrentUser().getUuid();
        boolean isTeacher = assignment.getLmsClass().getTeacher().getUuid().equals(currentUuid);

        String subStatus = null;
        String submittedContent = null;
        if (!isTeacher) {
            assertStudentEnrolled(assignment, currentUuid);
            var submission = assignmentSubmissionRepository
                    .findByAssignment_IdAndStudent_Uuid(assignmentId, currentUuid);
            if (submission.isEmpty()) {
                assertBeforeDeadline(assignment);
            } else {
                subStatus = submission.get().getStatus();
                submittedContent = submission.get().getContent();
                return toDto(
                        assignment,
                        subStatus,
                        submittedContent,
                        false,
                        submission.get().getScorePercent()
                );
            }
        }

        if (isTeacher) {
            return toDtoWithTeacherCounts(assignment, subStatus, submittedContent, true);
        }
        return toDto(assignment, subStatus, submittedContent, false, null);
    }

    @Override
    @Transactional
    public AssignmentDto submit(Long assignmentId, SubmitAssignmentRequest request) {
        String studentUuid = SecurityUtils.requireCurrentUser().getUuid();

        Assignment assignment = assignmentRepository.findByIdAndDeletedFalse(assignmentId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.ASSIGNMENT_NOT_FOUND));

        assertStudentEnrolled(assignment, studentUuid);
        assertBeforeDeadline(assignment);

        if (assignmentSubmissionRepository.existsByAssignment_IdAndStudent_Uuid(assignmentId, studentUuid)) {
            throw new BusinessException(ExceptionCode.ASSIGNMENT_ALREADY_SUBMITTED);
        }

        User student = userRepository.findByUuidAndDeletedFalse(studentUuid)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setContent(request.getContent());
        submission.setStatus("SUBMITTED");
        submission.setScorePercent(BigDecimal.valueOf(100));
        assignmentSubmissionRepository.save(submission);

        classWeightedGradeService.recalculate(assignment.getLmsClass().getId(), studentUuid);

        return toDto(assignment, "SUBMITTED", request.getContent(), false, submission.getScorePercent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionDto> getSubmissions(Long assignmentId) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();

        Assignment assignment = assignmentRepository.findByIdAndDeletedFalse(assignmentId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.ASSIGNMENT_NOT_FOUND));

        if (!assignment.getLmsClass().getTeacher().getUuid().equals(teacherUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        return assignmentSubmissionRepository.findByAssignmentIdWithStudent(assignmentId).stream()
                .map(this::toSubmissionDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAssignment(Long assignmentId) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();

        Assignment assignment = assignmentRepository.findByIdAndDeletedFalse(assignmentId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.ASSIGNMENT_NOT_FOUND));

        if (!assignment.getLmsClass().getTeacher().getUuid().equals(teacherUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        assignment.setDeleted(true);
        assignmentRepository.save(assignment);
    }

    private AssignmentDto toDto(
            Assignment assignment,
            String submissionStatus,
            String submittedContent,
            boolean includeAiAnswers,
            BigDecimal submissionScorePercent
    ) {
        List<TaskContentBlockDto> blocks = taskContentBlockService.enrichBlocks(
                assignment.getContentBlocksJson(),
                assignment.getId(),
                null,
                includeAiAnswers
        );
        return AssignmentDto.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .instructions(assignment.getInstructions())
                .classId(assignment.getLmsClass().getId())
                .className(assignment.getLmsClass().getName())
                .status(assignment.getStatus())
                .createdAt(assignment.getCreatedAt())
                .dueAt(assignment.getDueAt())
                .pastDue(isPastDue(assignment))
                .submissionStatus(submissionStatus)
                .submittedContent(submittedContent)
                .submissionScorePercent(submissionScorePercent)
                .contentBlocks(blocks.isEmpty() ? null : blocks)
                .build();
    }

    private AssignmentDto toDto(
            Assignment assignment,
            String submissionStatus,
            String submittedContent,
            boolean includeAiAnswers
    ) {
        return toDto(assignment, submissionStatus, submittedContent, includeAiAnswers, null);
    }

    private AssignmentDto toDtoWithTeacherCounts(
            Assignment assignment,
            String submissionStatus,
            String submittedContent,
            boolean includeAiAnswers
    ) {
        long enrolled = classEnrollmentRepository.countByLmsClass_Id(assignment.getLmsClass().getId());
        long submitted = assignmentSubmissionRepository.countSubmittedByAssignmentId(assignment.getId());
        return toDto(assignment, submissionStatus, submittedContent, includeAiAnswers).toBuilder()
                .enrolledStudents(enrolled)
                .submittedCount(submitted)
                .build();
    }

    private AssignmentSubmissionDto toSubmissionDto(AssignmentSubmission submission) {
        return AssignmentSubmissionDto.builder()
                .submissionId(submission.getId())
                .studentId(submission.getStudent().getUuid())
                .studentName(submission.getStudent().getUsername())
                .studentEmail(submission.getStudent().getEmail())
                .status(submission.getStatus())
                .content(submission.getContent())
                .feedback(submission.getFeedback())
                .grade(submission.getGrade())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

    private void assertStudentEnrolled(Assignment assignment, String studentUuid) {
        if (!classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(
                assignment.getLmsClass().getId(), studentUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
    }

    private void assertBeforeDeadline(Assignment assignment) {
        if (isPastDue(assignment)) {
            throw new BusinessException(ExceptionCode.ASSIGNMENT_DEADLINE_PASSED);
        }
    }

    private static boolean isPastDue(Assignment assignment) {
        return assignment.getDueAt() != null && LocalDateTime.now().isAfter(assignment.getDueAt());
    }
}
