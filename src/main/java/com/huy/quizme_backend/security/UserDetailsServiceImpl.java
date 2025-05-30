package com.huy.quizme_backend.security;

import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    // Lấy thông tin người dùng từ username hoặc email
    @Override
    @Transactional // Đánh dấu phương thức này là giao dịch
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Trả về đối tượng UserDetails
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username or email: " + usernameOrEmail));
    }

    // JwtAuthenticationFilter sử dụng để load UserDetails từ ID (nếu cần)
    @Transactional
    public UserDetails loadUserById(Long id) {
        // Trả về đối tượng UserDetails
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with id: " + id));
    }
}
