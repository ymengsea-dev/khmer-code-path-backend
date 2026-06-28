package com.mengsea.khmercodepath.api.permissions.config;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionsCatalog {

    public record TeacherGrant(String authority, boolean defaultForTeacher) {
    }

    public record StudentGrant(String authority, boolean defaultForStudent) {
    }

    private static final List<TeacherGrant> TEACHER_GRANTS = List.of(
            new TeacherGrant("lms:usr:manage", false),
            new TeacherGrant("lms:cls:manage", true),
            new TeacherGrant("lms:crs:manage", true),
            new TeacherGrant("lms:lsn:manage", true),
            new TeacherGrant("lms:att:manage", true),
            new TeacherGrant("lms:grd:manage", true),
            new TeacherGrant("lms:ai:ingest", true),
            new TeacherGrant("lms:ops:manage", false),
            new TeacherGrant("lms:school:manage", false)
    );

    private static final List<StudentGrant> STUDENT_GRANTS = List.of(
            new StudentGrant("lms:ai:chat", true),
            new StudentGrant("lms:cls:read", true),
            new StudentGrant("lms:crs:read", true),
            new StudentGrant("lms:dash:read", true),
            new StudentGrant("lms:prog:read", true),
            new StudentGrant("lms:ai:ingest", false),
            new StudentGrant("lms:lsn:manage", false),
            new StudentGrant("lms:cls:manage", false)
    );

    public List<TeacherGrant> teacherGrants() {
        return TEACHER_GRANTS;
    }

    public List<StudentGrant> studentGrants() {
        return STUDENT_GRANTS;
    }

    public Set<String> teacherAuthorities() {
        return TEACHER_GRANTS.stream().map(TeacherGrant::authority).collect(Collectors.toSet());
    }

    public Set<String> studentAuthorities() {
        return STUDENT_GRANTS.stream().map(StudentGrant::authority).collect(Collectors.toSet());
    }

    public Optional<TeacherGrant> findTeacherGrant(String authority) {
        return TEACHER_GRANTS.stream()
                .filter(g -> g.authority().equals(authority))
                .findFirst();
    }

    public Optional<StudentGrant> findStudentGrant(String authority) {
        return STUDENT_GRANTS.stream()
                .filter(g -> g.authority().equals(authority))
                .findFirst();
    }
}
