package com.mengsea.khmercodepath.api.courses.service;

import com.mengsea.khmercodepath.api.courses.mapper.CourseMapper;
import com.mengsea.khmercodepath.api.courses.payload.CoursePagePayload;
import com.mengsea.khmercodepath.api.courses.payload.CourseSummaryPayload;
import com.mengsea.khmercodepath.api.courses.payload.CreateCourseRequest;
import com.mengsea.khmercodepath.api.courses.payload.UpdateCourseRequest;
import com.mengsea.khmercodepath.commons.constant.CourseLevel;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.Course;
import com.mengsea.khmercodepath.commons.domain.CourseEnrollment;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.CourseEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.CourseRepository;
import com.mengsea.khmercodepath.commons.repository.CourseSpecifications;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CourseManagementServiceImpl implements CourseManagementService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional(readOnly = true)
    public CoursePagePayload listCourses(String search, CourseLevel level, Pageable pageable) {
        User me = SecurityUtils.requireCurrentUser();

        Specification<Course> spec = Specification.where(CourseSpecifications.notDeleted())
                .and(CourseSpecifications.searchContains(search))
                .and(CourseSpecifications.levelEquals(level));

        if (me.getRole() == Role.STUDENT) {
            spec = spec.and(CourseSpecifications.publishedOnly());
        }

        Page<Course> page = courseRepository.findAll(spec, pageable);
        Map<Long, Integer> progressByCourseId = loadProgressMap(me);

        List<CourseSummaryPayload> items = page.getContent().stream()
                .map(c -> courseMapper.toSummary(c, progressByCourseId.get(c.getId())))
                .toList();

        return CoursePagePayload.builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseSummaryPayload getCourse(Long id) {
        Course course = requireCourse(id);
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() == Role.STUDENT && !course.isPublished()) {
            throw new BusinessException(ExceptionCode.COURSE_NOT_FOUND);
        }
        Integer progress = resolveProgress(me, course.getId());
        return courseMapper.toSummary(course, progress);
    }

    @Override
    @Transactional
    public CourseSummaryPayload createCourse(CreateCourseRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        assertCanManage(me, null);

        Course course = new Course();
        applyCreate(course, request);
        course.setCreatedBy(me);
        course.setDeleted(false);
        courseRepository.save(course);
        return courseMapper.toSummary(course, null);
    }

    @Override
    @Transactional
    public CourseSummaryPayload updateCourse(Long id, UpdateCourseRequest request) {
        Course course = requireCourse(id);
        User me = SecurityUtils.requireCurrentUser();
        assertCanManage(me, course);

        if (request.getTitle() != null) {
            course.setTitle(request.getTitle().trim());
        }
        if (request.getInstitution() != null) {
            course.setInstitution(request.getInstitution().trim());
        }
        if (request.getInstitutionLogo() != null) {
            course.setInstitutionLogo(blankToNull(request.getInstitutionLogo()));
        }
        if (request.getInstitutionColor() != null) {
            course.setInstitutionColor(request.getInstitutionColor().trim());
        }
        if (request.getLevel() != null) {
            course.setLevel(request.getLevel());
        }
        if (request.getPts() != null) {
            course.setPts(request.getPts());
        }
        if (request.getBgColor() != null) {
            course.setBgColor(request.getBgColor().trim());
        }
        if (request.getImageUrl() != null) {
            course.setImageUrl(blankToNull(request.getImageUrl()));
        }
        if (request.getDescription() != null) {
            course.setDescription(blankToNull(request.getDescription()));
        }
        if (request.getTechnologies() != null) {
            course.setTechnologies(courseMapper.toTechnologies(request.getTechnologies()));
        }
        if (request.getPrerequisite() != null) {
            course.setPrerequisite(blankToNull(request.getPrerequisite()));
        }
        if (request.getAchievement() != null) {
            course.setAchievement(blankToNull(request.getAchievement()));
        }
        if (request.getLocked() != null) {
            course.setLocked(request.getLocked());
        }
        if (request.getPublished() != null) {
            course.setPublished(request.getPublished());
        }

        courseRepository.save(course);
        return courseMapper.toSummary(course, null);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = requireCourse(id);
        User me = SecurityUtils.requireCurrentUser();
        assertCanManage(me, course);
        course.setDeleted(true);
        courseRepository.save(course);
    }

    private Course requireCourse(Long id) {
        return courseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.COURSE_NOT_FOUND));
    }

    private void assertCanManage(User me, Course course) {
        if (me.getRole() == Role.ADMIN) {
            return;
        }
        if (me.getRole() == Role.TEACHER) {
            if (course == null) {
                return;
            }
            if (course.getCreatedBy() != null
                    && Objects.equals(course.getCreatedBy().getUuid(), me.getUuid())) {
                return;
            }
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
        throw new BusinessException(ExceptionCode.ACCESS_DENIED);
    }

    private void applyCreate(Course course, CreateCourseRequest request) {
        course.setTitle(request.getTitle().trim());
        course.setInstitution(request.getInstitution().trim());
        course.setInstitutionLogo(blankToNull(request.getInstitutionLogo()));
        course.setInstitutionColor(
                request.getInstitutionColor() != null && !request.getInstitutionColor().isBlank()
                        ? request.getInstitutionColor().trim()
                        : "#8b5cf6"
        );
        course.setLevel(request.getLevel());
        course.setPts(request.getPts() != null ? request.getPts() : 150);
        course.setBgColor(
                request.getBgColor() != null && !request.getBgColor().isBlank()
                        ? request.getBgColor().trim()
                        : "from-slate-900 to-slate-700"
        );
        course.setImageUrl(blankToNull(request.getImageUrl()));
        course.setDescription(blankToNull(request.getDescription()));
        course.setTechnologies(courseMapper.toTechnologies(request.getTechnologies()));
        course.setPrerequisite(blankToNull(request.getPrerequisite()));
        course.setAchievement(blankToNull(request.getAchievement()));
        course.setLocked(Boolean.TRUE.equals(request.getLocked()));
        course.setPublished(request.getPublished() == null || request.getPublished());
    }

    private Map<Long, Integer> loadProgressMap(User me) {
        if (me.getRole() != Role.STUDENT) {
            return Map.of();
        }
        Map<Long, Integer> map = new HashMap<>();
        for (CourseEnrollment enrollment : courseEnrollmentRepository.findByStudent_Uuid(me.getUuid())) {
            map.put(enrollment.getCourse().getId(), enrollment.getProgressPct());
        }
        return map;
    }

    private Integer resolveProgress(User me, Long courseId) {
        if (me.getRole() != Role.STUDENT) {
            return null;
        }
        return courseEnrollmentRepository
                .findByCourse_IdAndStudent_Uuid(courseId, me.getUuid())
                .map(CourseEnrollment::getProgressPct)
                .orElse(null);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
