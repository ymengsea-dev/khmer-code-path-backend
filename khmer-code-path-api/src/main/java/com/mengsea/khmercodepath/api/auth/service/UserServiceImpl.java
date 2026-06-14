package com.mengsea.khmercodepath.api.auth.service;

import com.mengsea.khmercodepath.api.auth.mapper.UserMapper;
import com.mengsea.khmercodepath.api.auth.payload.AuthResponse;
import com.mengsea.khmercodepath.api.auth.payload.UserResponse;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Provider;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.CustomUserDetail;
import com.mengsea.khmercodepath.commons.domain.PasswordResetToken;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.PasswordResetTokenRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetMailer passwordResetMailer;

    /** Explicitly revoked refresh tokens (logout). Valid JWTs refresh even if not in {@link #refreshTokenStore}. */
    private final Set<String> revokedRefreshTokens = ConcurrentHashMap.newKeySet();
    private final java.util.Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void register(String username, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(ExceptionCode.USER_ALREADY_EXISTS);
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider(Provider.LOCAL);
        user.setRole(Role.STUDENT);
        user.setDeleted(false);
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(String email, String password) {
        if (userRepository.findByEmailAndDeletedFalse(email).isEmpty()) {
            throw new BusinessException(ExceptionCode.USER_NOT_FOUND);
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );

            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);
            refreshTokenStore.put(refreshToken, userDetails.getUsername());

            UserResponse userResponse = userMapper.toResponse(userDetails.getUser());

            return AuthResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getAccessTokenTtlSeconds())
                    .user(userResponse)
                    .build();
        } catch (BadCredentialsException exception) {
            throw new BusinessException(ExceptionCode.INVALID_CREDENTIAL);
        }
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ExceptionCode.REFRESH_TOKEN_NOT_FOUND);
        }
        if (revokedRefreshTokens.contains(refreshToken)) {
            throw new BusinessException(ExceptionCode.REFRESH_TOKEN_REVOKED);
        }
        if (jwtService.isExpiration(refreshToken)) {
            revokedRefreshTokens.add(refreshToken);
            refreshTokenStore.remove(refreshToken);
            throw new BusinessException(ExceptionCode.REFRESH_TOKEN_EXPIRED);
        }

        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

        CustomUserDetail userDetails = new CustomUserDetail(user);
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new BusinessException(ExceptionCode.REFRESH_TOKEN_REVOKED);
        }

        refreshTokenStore.putIfAbsent(refreshToken, email);

        String newAccessToken = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenTtlSeconds())
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            revokedRefreshTokens.add(refreshToken);
            refreshTokenStore.remove(refreshToken);
        }
    }

    @Override
    public UserResponse me(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateProfile(String email, String userName) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        user.setUsername(userName.trim());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        if (user.getPassword() == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ExceptionCode.INVALID_CREDENTIAL);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmailAndDeletedFalse(email)
                .ifPresent(user -> {
                    passwordResetTokenRepository.deleteByUser_UuidAndUsedAtIsNull(user.getUuid());
                    PasswordResetToken reset = new PasswordResetToken();
                    reset.setUser(user);
                    reset.setToken(generateResetToken());
                    reset.setExpiresAt(Instant.now().plusSeconds(3600));
                    passwordResetTokenRepository.save(reset);
                    passwordResetMailer.sendResetLink(user.getEmail(), reset.getToken());
                });
    }

    @Override
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        PasswordResetToken reset = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ExceptionCode.PASSWORD_RESET_TOKEN_INVALID));
        if (reset.getUsedAt() != null) {
            throw new BusinessException(ExceptionCode.PASSWORD_RESET_TOKEN_INVALID);
        }
        if (reset.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(ExceptionCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }
        User user = userRepository.findByEmailAndDeletedFalse(reset.getUser().getEmail())
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        reset.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(reset);
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
