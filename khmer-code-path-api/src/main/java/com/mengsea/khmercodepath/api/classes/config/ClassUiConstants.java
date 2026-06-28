package com.mengsea.khmercodepath.api.classes.config;

import com.mengsea.khmercodepath.commons.constant.ClassVisibility;

import java.util.List;

public final class ClassUiConstants {

    public static final String ALL_SEMESTERS_LABEL = "All Semesters";

    public static final List<String> CARD_GRADIENTS = List.of(
            "from-indigo-500 to-purple-600",
            "from-blue-600 to-sky-700",
            "from-emerald-600 to-teal-700",
            "from-amber-500 to-orange-600",
            "from-rose-500 to-pink-600",
            "from-violet-600 to-fuchsia-700"
    );

    public static final List<ClassVisibility> VISIBILITY_VALUES = List.of(
            ClassVisibility.PRIVATE,
            ClassVisibility.PUBLIC
    );

    private ClassUiConstants() {
    }
}
