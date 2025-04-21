package com.huy.quizme_backend.config;

import com.huy.quizme_backend.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Value("${allowed-origins}")
    private String[] allowedOrigins;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Bean để mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean để cung cấp AuthenticationProvider
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Tạo một DaoAuthenticationProvider
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

        // Thiết lập UserDetailsService cho DaoAuthenticationProvider
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);

        // Thiết lập PasswordEncoder cho DaoAuthenticationProvider
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

        // Trả về DaoAuthenticationProvider
        return daoAuthenticationProvider;
    }

    // Bean để cung cấp AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Bean để cấu hình CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Tạo một đối tượng CorsConfiguration
        CorsConfiguration configuration = new CorsConfiguration();

        // Thiết lập các nguồn gốc, phương thức và header được phép
        configuration.setAllowedOrigins(List.of(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        // Cho phép tất cả các nguồn gốc
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Cấu hình CORS cho tất cả các endpoint
        source.registerCorsConfiguration("/**", configuration);

        // Trả về cấu hình CORS
        return source;
    }

    // Bean cấu hình chuỗi filter bảo mật
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Cấu hình chuỗi filter bảo mật
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Cấu hình CORS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Không sử dụng session
                )
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả truy cập vào /api/auth/** (đăng ký, đăng nhập)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Thêm các endpoint public khác nếu có (vd: /api/questions/public, /api/default-avatars)
                        // .requestMatchers("/api/some-public-endpoint").permitAll()

                        .anyRequest().authenticated() // Tất cả các request khác đều cần xác thực
                )
                .authenticationProvider(authenticationProvider()) // Cung cấp AuthenticationProvider
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Thêm JWT filter trước filter mặc định

        // Trả về chuỗi filter bảo mật
        return http.build();
    }
}
