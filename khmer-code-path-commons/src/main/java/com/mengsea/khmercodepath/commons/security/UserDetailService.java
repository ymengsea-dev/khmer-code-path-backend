package com.mengsea.khmercodepath.commons.security;

import com.mengsea.khmercodepath.commons.domain.CustomUserDetail;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with this email: " + email));

        return new CustomUserDetail(user);
    }
}
