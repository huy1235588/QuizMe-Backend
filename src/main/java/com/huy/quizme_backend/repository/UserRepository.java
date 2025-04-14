package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user bằng username (cần cho login và kiểm tra tồn tại khi đăng ký)
    Optional<User> findByUsername(String username);

    // Tìm user bằng email (cần cho login và kiểm tra tồn tại khi đăng ký)
    Optional<User> findByEmail(String email);

    // Tìm user bằng username hoặc email (tiện cho login)
    Optional<User> findByUsernameOrEmail(String username, String email);

    // Kiểm tra username đã tồn tại chưa
    Boolean existsByUsername(String username);

    // Kiểm tra email đã tồn tại chưa
    Boolean existsByEmail(String email);
}
