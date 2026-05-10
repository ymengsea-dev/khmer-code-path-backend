package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import org.springframework.data.jpa.domain.Specification;

public final class LmsClassSpecifications {

    private LmsClassSpecifications() {}

    public static Specification<LmsClass> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<LmsClass> teacherUuidEquals(String teacherUuid) {
        if (teacherUuid == null || teacherUuid.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.join("teacher").get("uuid"), teacherUuid);
    }

    public static Specification<LmsClass> searchContains(String search) {
        if (search == null || search.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("code")), pattern)
        );
    }

    public static Specification<LmsClass> semesterEquals(String semester) {
        if (semester == null || semester.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("semester"), semester);
    }

    public static Specification<LmsClass> academicYearEquals(Integer academicYear) {
        if (academicYear == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("academicYear"), academicYear);
    }

    public static Specification<LmsClass> statusEquals(ClassStatus status) {
        if (status == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
