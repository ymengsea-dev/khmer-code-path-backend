package com.mengsea.khmercodepathbackend.config.OauthConfig;

import com.mengsea.khmercodepathbackend.entities.CustomOauthUser;
import com.mengsea.khmercodepathbackend.entities.CustomUserDetail;
import com.mengsea.khmercodepathbackend.services.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOauthUser oauthUser = (CustomOauthUser) authentication.getPrincipal();
        CustomUserDetail userDetail = new CustomUserDetail(oauthUser.getUser());

        String accessToken = jwtService.generateToken(userDetail);
        String refreshToken = jwtService.generateRefreshToken(userDetail);

        addRefreshCookie(response, refreshToken);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/login")
                .queryParam("token", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("ailms_refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }
}

