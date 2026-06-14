package com.mengsea.khmercodepath.api.search.service;

import com.mengsea.khmercodepath.api.search.payload.GlobalSearchResultPayload;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.Lesson;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.Quiz;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.domain.UserNote;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private static final int LIMIT_PER_TYPE = 5;

    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<GlobalSearchResultPayload> search(String query) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.length() < 2) {
            return List.of();
        }
        User me = SecurityUtils.requireCurrentUser();
        String pattern = "%" + trimmed.toLowerCase() + "%";
        List<GlobalSearchResultPayload> results = new ArrayList<>();
        results.addAll(searchClasses(me, pattern));
        results.addAll(searchLessons(me, pattern));
        results.addAll(searchQuizzes(me, pattern));
        results.addAll(searchNotes(me, pattern));
        return results;
    }

    private List<GlobalSearchResultPayload> searchClasses(User me, String pattern) {
        String access = classAccessWhere("c", me);
        return entityManager.createQuery("""
                        SELECT c FROM LmsClass c
                        WHERE c.deleted = false
                        AND (LOWER(c.name) LIKE :q OR LOWER(c.code) LIKE :q OR LOWER(COALESCE(c.description, '')) LIKE :q)
                        AND %s
                        ORDER BY c.updatedAt DESC
                        """.formatted(access), LmsClass.class)
                .setParameter("q", pattern)
                .setMaxResults(LIMIT_PER_TYPE)
                .getResultStream()
                .map(c -> GlobalSearchResultPayload.builder()
                        .id("class-" + c.getId())
                        .type("Class")
                        .title(c.getName())
                        .subtitle(c.getCode())
                        .targetView("lessons")
                        .targetParams(Map.of("course", String.valueOf(c.getId()), "lesson", c.getName()))
                        .build())
                .toList();
    }

    private List<GlobalSearchResultPayload> searchLessons(User me, String pattern) {
        String access = classAccessWhere("l.lmsClass", me);
        return entityManager.createQuery("""
                        SELECT l FROM Lesson l
                        JOIN FETCH l.lmsClass c
                        WHERE l.deleted = false AND c.deleted = false
                        AND (LOWER(l.title) LIKE :q OR LOWER(COALESCE(l.description, '')) LIKE :q)
                        AND %s
                        ORDER BY l.updatedAt DESC
                        """.formatted(access), Lesson.class)
                .setParameter("q", pattern)
                .setMaxResults(LIMIT_PER_TYPE)
                .getResultStream()
                .map(l -> GlobalSearchResultPayload.builder()
                        .id("lesson-" + l.getId())
                        .type("Lesson")
                        .title(l.getTitle())
                        .subtitle(l.getLmsClass().getName())
                        .targetView("lessons")
                        .targetParams(Map.of(
                                "course", String.valueOf(l.getLmsClass().getId()),
                                "lesson", l.getLmsClass().getName(),
                                "lessonId", String.valueOf(l.getId())
                        ))
                        .build())
                .toList();
    }

    private List<GlobalSearchResultPayload> searchQuizzes(User me, String pattern) {
        String access = classAccessWhere("q.lmsClass", me);
        return entityManager.createQuery("""
                        SELECT q FROM Quiz q
                        JOIN FETCH q.lmsClass c
                        WHERE q.deleted = false AND c.deleted = false
                        AND (LOWER(q.title) LIKE :q OR LOWER(COALESCE(q.description, '')) LIKE :q)
                        AND %s
                        ORDER BY q.createdAt DESC
                        """.formatted(access), Quiz.class)
                .setParameter("q", pattern)
                .setMaxResults(LIMIT_PER_TYPE)
                .getResultStream()
                .map(q -> GlobalSearchResultPayload.builder()
                        .id("quiz-" + q.getId())
                        .type("Quiz")
                        .title(q.getTitle())
                        .subtitle(q.getLmsClass().getName())
                        .targetView("tasks")
                        .targetParams(Map.of("quizId", String.valueOf(q.getId())))
                        .build())
                .toList();
    }

    private List<GlobalSearchResultPayload> searchNotes(User me, String pattern) {
        return entityManager.createQuery("""
                        SELECT n FROM UserNote n
                        WHERE n.deleted = false AND n.user.uuid = :userUuid
                        AND (LOWER(n.title) LIKE :q OR LOWER(n.preview) LIKE :q)
                        ORDER BY n.updatedAt DESC
                        """, UserNote.class)
                .setParameter("userUuid", me.getUuid())
                .setParameter("q", pattern)
                .setMaxResults(LIMIT_PER_TYPE)
                .getResultStream()
                .map(n -> GlobalSearchResultPayload.builder()
                        .id("note-" + n.getId())
                        .type("Notebook")
                        .title(n.getTitle())
                        .subtitle(n.getSourceLabel())
                        .targetView("notebook")
                        .targetParams(Map.of("note", String.valueOf(n.getId())))
                        .build())
                .toList();
    }

    private String classAccessWhere(String classAlias, User me) {
        if (me.getRole() == Role.ADMIN) {
            return "1 = 1";
        }
        if (me.getRole() == Role.TEACHER) {
            return classAlias + ".teacher.uuid = '" + me.getUuid() + "'";
        }
        return "EXISTS (SELECT e FROM ClassEnrollment e WHERE e.lmsClass.id = "
                + classAlias + ".id AND e.student.uuid = '" + me.getUuid() + "')";
    }
}
