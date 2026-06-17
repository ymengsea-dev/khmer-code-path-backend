package com.mengsea.khmercodepath.commons.config;

import com.mengsea.khmercodepath.commons.config.oauth.OAuth2FailureHandler;
import com.mengsea.khmercodepath.commons.config.oauth.OAuth2SuccessHandler;
import com.mengsea.khmercodepath.commons.filter.JwtAuthFilter;
import com.mengsea.khmercodepath.commons.security.AccessDeniedEntryPoint;
import com.mengsea.khmercodepath.commons.security.JwtExpirationEntryPoint;
import com.mengsea.khmercodepath.commons.security.UserDetailService;
import com.mengsea.khmercodepath.commons.security.oauth2.CustomOAuth2UserService;
import jakarta.servlet.DispatcherType;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * HTTP-level rules: public vs authenticated only. Role and fine-grained checks use
 * {@link org.springframework.security.access.prepost.PreAuthorize} on controllers (AI-LMS spec).
 */
@Configuration
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final UserDetailService userDetailService;
    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtExpirationEntryPoint jwtExpirationEntryPoint;
    private final AccessDeniedEntryPoint accessDeniedEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Keep JWT auth on the initial SSE request; async continuations must not re-authorize (no token on dispatch).
                .securityContext(sc -> sc.requireExplicitSave(false))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtExpirationEntryPoint)
                        .accessDeniedHandler(accessDeniedEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/password-reset/request",
                                "/api/v1/auth/password-reset/confirm",
                                "/api/v1/auth/google",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/api/v1/files/**",
                                "/api/v1/profile/avatar/*"
                        ).permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        .anyRequest().authenticated()
                ).oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
