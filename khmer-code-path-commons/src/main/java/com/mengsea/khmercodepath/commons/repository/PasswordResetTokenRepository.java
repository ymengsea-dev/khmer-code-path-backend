package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser_UuidAndUsedAtIsNull(String userUuid);
}
