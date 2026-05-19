package com.mengsea.khmercodepath.api.courses.service;

import com.mengsea.khmercodepath.api.courses.payload.CoursePagePayload;
import com.mengsea.khmercodepath.api.courses.payload.CourseSummaryPayload;
import com.mengsea.khmercodepath.api.courses.payload.CreateCourseRequest;
import com.mengsea.khmercodepath.api.courses.payload.UpdateCourseRequest;
import com.mengsea.khmercodepath.commons.constant.CourseLevel;
import org.springframework.data.domain.Pageable;

public interface CourseManagementService {

    CoursePagePayload listCourses(String search, CourseLevel level, Pageable pageable);

    CourseSummaryPayload getCourse(Long id);

    CourseSummaryPayload createCourse(CreateCourseRequest request);

    CourseSummaryPayload updateCourse(Long id, UpdateCourseRequest request);

    void deleteCourse(Long id);
}
