package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.constant.CourseLevel;
import com.mengsea.khmercodepath.commons.domain.Course;
import org.springframework.data.jpa.domain.Specification;

public final class CourseSpecifications {

    private CourseSpecifications() {}

    public static Specification<Course> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Course> publishedOnly() {
        return (root, query, cb) -> cb.isTrue(root.get("published"));
    }

    public static Specification<Course> searchContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("institution")), pattern),
                cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern)
        );
    }

    public static Specification<Course> levelEquals(CourseLevel level) {
        if (level == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("level"), level);
    }
}
