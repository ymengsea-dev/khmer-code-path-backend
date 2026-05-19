package com.mengsea.khmercodepath.api.courses.payload;

import com.mengsea.khmercodepath.commons.constant.CourseLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryPayload {
    private Long id;
    private String title;
    private String institution;
    private String institutionLogo;
    private String institutionColor;
    private CourseLevel level;
    private int pts;
    private String bgColor;
    private String imageUrl;
    private String description;
    private List<CourseTechnologyPayload> technologies;
    private String prerequisite;
    private String achievement;
    private boolean locked;
    private boolean published;
    /** Student enrollment progress 0–100; null when not enrolled or not a student view. */
    private Integer progress;
}
