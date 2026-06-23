package com.mengsea.khmercodepath.commons.util;

import java.util.Locale;

public final class EmailDomainUtils {

    private EmailDomainUtils() {}

    public static String extractDomain(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "";
        }
        return normalizeDomain(email.substring(email.lastIndexOf('@') + 1));
    }

    public static String normalizeDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return "";
        }
        return domain.trim().toLowerCase(Locale.ROOT).replaceAll("^@+", "");
    }
}
