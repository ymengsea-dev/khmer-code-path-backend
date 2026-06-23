package com.mengsea.khmercodepath.api.classes.service;

import com.mengsea.khmercodepath.api.classes.payload.PublicCoursesConfigPayload;
import com.mengsea.khmercodepath.api.classes.payload.PublicCoursesPagePayload;
import org.springframework.data.domain.Pageable;

public interface PublicCoursesService {

    PublicCoursesConfigPayload getConfig();

    PublicCoursesPagePayload listPublicCourses(String search, Pageable pageable);

    void selfEnroll(Long classId);
}
