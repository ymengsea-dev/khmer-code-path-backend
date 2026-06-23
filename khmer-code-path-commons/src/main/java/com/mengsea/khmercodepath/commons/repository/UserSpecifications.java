package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

    private UserSpecifications() {}

    public static Specification<User> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<User> deletedFlag(boolean includeDeleted) {
        if (includeDeleted) {
            return (root, query, cb) -> cb.conjunction();
        }
        return notDeleted();
    }

    public static Specification<User> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("username")), pattern);
    }

    public static Specification<User> emailContains(String email) {
        if (email == null || email.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + email.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), pattern);
    }

    public static Specification<User> roleEquals(Role role) {
        if (role == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> activeEquals(Boolean isActive) {
        if (isActive == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("isActive"), isActive);
    }

    public static Specification<User> schoolIdEquals(Long schoolId) {
        if (schoolId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.join("school").get("id"), schoolId);
    }
}
