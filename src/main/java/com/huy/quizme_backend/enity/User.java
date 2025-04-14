package com.huy.quizme_backend.enity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER; // Mặc định là USER

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "avatar_type", length = 20)
    private String avatarType = "DEFAULT";

    @Column(name = "avatar_updated_at")
    private LocalDateTime avatarUpdatedAt;

    @CreationTimestamp // Tự động tạo thời gian khi tạo bản ghi
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // --- UserDetails methods ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Trả về quyền hạn của người dùng
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        // Trả về mật khẩu của người dùng
        return password;
    }

    @Override
    public String getUsername() {
        // Trả về tên người dùng hoặc email
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Tài khoản không hết hạn
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Tài khoản không bị khóa
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Thông tin xác thực không hết hạn
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Tài khoản đã được kích hoạt
        return true;
    }
}
