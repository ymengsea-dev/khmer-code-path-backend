package com.mengsea.khmercodepath.commons.constant;

public final class QuizKind {

    public static final String ASSIGNMENT = "ASSIGNMENT";
    public static final String EXAM = "EXAM";

    private QuizKind() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return ASSIGNMENT;
        }
        String upper = value.trim().toUpperCase();
        if (EXAM.equals(upper)) {
            return EXAM;
        }
        return ASSIGNMENT;
    }

    public static boolean isExam(String kind) {
        return EXAM.equals(normalize(kind));
    }
}
