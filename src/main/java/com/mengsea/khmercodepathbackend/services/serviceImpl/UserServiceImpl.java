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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    String expiredIn;

    @Override
    public void register(String username, String email, String password) {
        // check use already exist
        if (userRepository.findByEmail(email).isPresent()){
            throw new RuntimeException("User is already exist");
        }
        // if not exist register user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider(Provider.LOCAL);
        user.setRole(Role.USER);
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

            AuthResponse response = AuthResponse.builder()
                    .accessToken(token)
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

}
