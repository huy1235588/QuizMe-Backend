package com.huy.quizme_backend.repository;

import com.huy.quizme_backend.enity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    // Lấy Top người dùng có tổng số quiz được chơi nhiều nhất
    @Query("SELECT u " +
            "FROM User u " +
            "LEFT JOIN Quiz q ON u.id = q.creator.id " +
            "GROUP BY u.id " +
            "ORDER BY SUM(q.playCount) DESC")
    List<User> findTopUsersByTotalQuizPlays();

    // Lấy avatar của người dùng
    @Query("SELECT u.profileImage " +
            "FROM User u " +
            "WHERE u.id = ?1")
    Optional<String> findAvatarByUserId(Long userId);

    // Tìm kiếm người dùng theo username hoặc fullName với pagination
    Page<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String username, String fullName, Pageable pageable);
}
