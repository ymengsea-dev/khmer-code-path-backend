package com.mengsea.khmercodepathbackend.services.serviceImpl;

import com.mengsea.khmercodepathbackend.constant.ExceptionCode;
import com.mengsea.khmercodepathbackend.constant.Provider;
import com.mengsea.khmercodepathbackend.constant.Role;
import com.mengsea.khmercodepathbackend.dto.advices.AuthResponse;
import com.mengsea.khmercodepathbackend.dto.response.UserResponse;
import com.mengsea.khmercodepathbackend.entities.CustomUserDetail;
import com.mengsea.khmercodepathbackend.entities.User;
import com.mengsea.khmercodepathbackend.exceptions.BusinessException;
import com.mengsea.khmercodepathbackend.repositories.UserRepository;
import com.mengsea.khmercodepathbackend.services.UserService;
import com.mengsea.khmercodepathbackend.services.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    String expiredIn;

    @Value("${jwt.refresh-expiration}")
    String refreshExpiredIn;

    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    private final Map<String, PasswordResetPayload> passwordResetStore = new ConcurrentHashMap<>();

    private record PasswordResetPayload(String email, Instant expiresAt) {}

    @Override
    public void register(String username, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()){
            throw new BusinessException(ExceptionCode.USER_ALREADY_EXISTS);
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider(Provider.LOCAL);
        user.setRole(Role.STUDENT);
        userRepository.save(user);
    }

    @Override
    public AuthResponse login(String email, String password) {
        if (!userRepository.findByEmail(email).isPresent()){
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

            AuthResponse response = AuthResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(Long.valueOf(expiredIn))
                    .user(UserResponse.builder()
                            .userId(userDetails.getUser().getUuid())
                            .userName(userDetails.getUsername())
                            .role(userDetails.getUser().getRole())
                            .build())
                    .build();

            return response;
        } catch (BadCredentialsException exception) {
            throw  new BusinessException(ExceptionCode.INVALID_CREDENTIAL);
        }
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ExceptionCode.REFRESH_TOKEN_NOT_FOUND);
        }
        if (!refreshTokenStore.containsKey(refreshToken)) {
            throw new BusinessException(ExceptionCode.REFRESH_TOKEN_REVOKED);
        }
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

        CustomUserDetail userDetails = new CustomUserDetail(user);
        String newAccessToken = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(Long.valueOf(expiredIn))
                .user(UserResponse.builder()
                        .userId(user.getUuid())
                        .userName(user.getUsername())
                        .role(user.getRole())
                        .build())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenStore.remove(refreshToken);
        }
    }

    @Override
    public UserResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        return UserResponse.builder()
                .userId(user.getUuid())
                .userName(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        String token = UUID.randomUUID().toString();
        passwordResetStore.put(token, new PasswordResetPayload(user.getEmail(), Instant.now().plusSeconds(3600)));
    }

    @Override
    public void confirmPasswordReset(String token, String newPassword) {
        PasswordResetPayload payload = passwordResetStore.get(token);
        if (payload == null) {
            throw new BusinessException(ExceptionCode.PASSWORD_RESET_TOKEN_INVALID);
        }
        if (payload.expiresAt().isBefore(Instant.now())) {
            passwordResetStore.remove(token);
            throw new BusinessException(ExceptionCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }
        User user = userRepository.findByEmail(payload.email())
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetStore.remove(token);
    }

}
