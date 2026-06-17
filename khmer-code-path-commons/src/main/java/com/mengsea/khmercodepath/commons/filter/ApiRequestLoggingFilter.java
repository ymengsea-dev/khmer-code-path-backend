package com.mengsea.khmercodepath.commons.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Logs each HTTP API call: principal, method, path, query, request body, HTTP status, duration, and response body (truncated).
 * Uses caching wrappers so bodies can be read after the controller runs (same idea as typical request/response logging filters).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestLoggingFilter.class);

    private static final int MAX_PAYLOAD_CHARS = 4096;

    private static final Pattern SENSITIVE_JSON = Pattern.compile(
            "(?i)\"(password|newPassword|confirmPassword|accessToken|refreshToken|token)\"\\s*:\\s*\"[^\"]*\"");

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!shouldLog(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String query = StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : "";
        log.info(">> {} {}{}", request.getMethod(), request.getRequestURI(), query);

        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_CHARS);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        Exception failure = null;
        try {
            filterChain.doFilter(cachingRequest, cachingResponse);
        } catch (Exception ex) {
            failure = ex;
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            int status = cachingResponse.getStatus();
            String user = resolveUser();
            String outcome = failure != null ? "ERROR" : (status >= 200 && status < 400 ? "OK" : "FAIL");
            String path = cachingRequest.getRequestURI();
            String queryPart = StringUtils.hasText(cachingRequest.getQueryString())
                    ? "?" + cachingRequest.getQueryString()
                    : "";

            String reqBody = extractRequestPayload(cachingRequest);
            String respBody = extractResponsePayload(cachingResponse);

            log.info(
                    "HTTP api method={} path={}{} user={} outcome={} status={} durationMs={} reqBody={} respBody={}",
                    cachingRequest.getMethod(),
                    path,
                    queryPart,
                    user,
                    outcome,
                    status,
                    durationMs,
                    truncate(maskSensitive(reqBody)),
                    truncate(maskSensitive(respBody))
            );

            try {
                cachingResponse.copyBodyToResponse();
            } catch (IOException e) {
                log.warn("Could not copy cached response body: {}", e.getMessage());
            }
        }
    }

    private static boolean shouldLog(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return true;
        }
        // SSE endpoints produce a long-lived streamed response — the ContentCachingResponseWrapper
        // buffers every write and only flushes when copyBodyToResponse() is called after the filter
        // chain returns. For async SSE this means chunks are buffered and never forwarded to the client.
        if (path.endsWith("/stream")) {
            return false;
        }
        // Binary downloads (avatars, lesson materials) exceed the response cache limit and break if wrapped.
        if (path.contains("/profile/avatar/") || path.endsWith("/download")) {
            return false;
        }
        return !path.startsWith("/swagger-ui")
                && !path.startsWith("/v3/api-docs")
                && !path.startsWith("/swagger-ui.html")
                && !path.equals("/favicon.ico")
                && !path.startsWith("/oauth2/")
                && !path.startsWith("/login/oauth2/")
                && !path.startsWith("/actuator");
    }

    private static String resolveUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .filter(name -> !"anonymousUser".equalsIgnoreCase(name))
                .orElse("anonymous");
    }

    private static String extractRequestPayload(ContentCachingRequestWrapper request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            return "(multipart form)";
        }
        if (contentType != null && contentType.startsWith(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return "(event stream)";
        }
        byte[] buf = request.getContentAsByteArray();
        if (buf.length == 0) {
            return "";
        }
        return new String(buf, StandardCharsets.UTF_8);
    }

    private static String extractResponsePayload(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        if (contentType != null && contentType.startsWith(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return "(event stream)";
        }
        byte[] buf = response.getContentAsByteArray();
        if (buf.length == 0) {
            return "";
        }
        return new String(buf, StandardCharsets.UTF_8);
    }

    private static String maskSensitive(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        return SENSITIVE_JSON.matcher(raw).replaceAll("\"$1\":\"***\"");
    }

    private static String truncate(String s) {
        if (s == null) {
            return "";
        }
        String t = s.replaceAll("\\s+", " ").trim();
        if (t.length() <= MAX_PAYLOAD_CHARS) {
            return t;
        }
        return t.substring(0, MAX_PAYLOAD_CHARS) + "...(truncated)";
    }
}
