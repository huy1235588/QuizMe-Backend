package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.RefreshToken;
import com.huy.quizme_backend.enity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    boolean existsByToken(String token);

    void deleteByToken(String token);
    void deleteByUser(User user);
}
