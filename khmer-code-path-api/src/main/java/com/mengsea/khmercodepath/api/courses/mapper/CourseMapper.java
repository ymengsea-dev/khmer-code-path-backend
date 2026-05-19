package com.mengsea.khmercodepath.api.courses.mapper;

import com.mengsea.khmercodepath.api.courses.payload.CourseSummaryPayload;
import com.mengsea.khmercodepath.api.courses.payload.CourseTechnologyPayload;
import com.mengsea.khmercodepath.commons.domain.Course;
import com.mengsea.khmercodepath.commons.domain.CourseTechnology;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseMapper {

    public CourseSummaryPayload toSummary(Course course, Integer progress) {
        return CourseSummaryPayload.builder()
                .id(course.getId())
                .title(course.getTitle())
                .institution(course.getInstitution())
                .institutionLogo(course.getInstitutionLogo())
                .institutionColor(course.getInstitutionColor())
                .level(course.getLevel())
                .pts(course.getPts())
                .bgColor(course.getBgColor())
                .imageUrl(course.getImageUrl())
                .description(course.getDescription())
                .technologies(toTechPayloads(course.getTechnologies()))
                .prerequisite(course.getPrerequisite())
                .achievement(course.getAchievement())
                .locked(course.isLocked())
                .published(course.isPublished())
                .progress(progress)
                .build();
    }

    public List<CourseTechnology> toTechnologies(List<CourseTechnologyPayload> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return Collections.emptyList();
        }
        return payloads.stream()
                .map(p -> new CourseTechnology(p.getName().trim(), p.getColor().trim()))
                .collect(Collectors.toList());
    }

    private List<CourseTechnologyPayload> toTechPayloads(List<CourseTechnology> technologies) {
        if (technologies == null || technologies.isEmpty()) {
            return List.of();
        }
        return technologies.stream()
                .map(t -> CourseTechnologyPayload.builder()
                        .name(t.getName())
                        .color(t.getColor())
                        .build())
                .toList();
    }
}
