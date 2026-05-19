package com.mengsea.khmercodepath.api.attendance;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.exception.BusinessException;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AttendanceSessionIds {

    private static final Pattern PATTERN = Pattern.compile("^(\\d+)_(\\d{4}-\\d{2}-\\d{2})$");

    private AttendanceSessionIds() {}

    public record Parsed(long classId, LocalDate sessionDate) {}

    public static String encode(long classId, LocalDate sessionDate) {
        return classId + "_" + sessionDate;
    }

    public static Parsed parse(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(ExceptionCode.INVALID_SESSION_ID);
        }
        Matcher matcher = PATTERN.matcher(sessionId.trim());
        if (!matcher.matches()) {
            throw new BusinessException(ExceptionCode.INVALID_SESSION_ID);
        }
        try {
            long classId = Long.parseLong(matcher.group(1));
            LocalDate date = LocalDate.parse(matcher.group(2));
            return new Parsed(classId, date);
        } catch (Exception ex) {
            throw new BusinessException(ExceptionCode.INVALID_SESSION_ID);
        }
    }
}
