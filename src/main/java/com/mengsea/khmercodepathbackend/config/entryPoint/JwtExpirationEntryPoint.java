package com.mengsea.khmercodepathbackend.config.entryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengsea.khmercodepathbackend.constant.LmsStatusCode;
import com.mengsea.khmercodepathbackend.dto.advices.ApiResponse;
import com.mengsea.khmercodepathbackend.dto.advices.ApiResponses;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtExpirationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ApiResponse<Void> body = ApiResponses.of("SYS-2000", LmsStatusCode.UNAUTHORIZED, authException.getMessage(), null);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
