package com.mengsea.khmercodepath.api.grades;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GradeLetterUtil {

    private GradeLetterUtil() {}

    public static String toLetter(BigDecimal numeric) {
        if (numeric == null) {
            return null;
        }
        double v = numeric.setScale(2, RoundingMode.HALF_UP).doubleValue();
        if (v >= 93) return "A";
        if (v >= 90) return "A-";
        if (v >= 87) return "B+";
        if (v >= 83) return "B";
        if (v >= 80) return "B-";
        if (v >= 77) return "C+";
        if (v >= 73) return "C";
        if (v >= 70) return "C-";
        if (v >= 67) return "D+";
        if (v >= 60) return "D";
        return "F";
    }
}
