package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.UserRequest;
import com.huy.quizme_backend.dto.response.PageResponse;
import com.huy.quizme_backend.dto.response.UserProfileResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.enity.enums.Role;
import com.huy.quizme_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import com.huy.quizme_backend.util.FileValidationUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final LocalStorageService localStorageService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lấy thông tin người dùng theo ID
     *
     * @param id ID của người dùng
     * @return Thông tin người dùng
     */
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> UserResponse.fromUser(user, localStorageService))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    /**
     * Lấy Top người dùng có tổng số quiz được chơi nhiều nhất
     *
     * @return Danh sách người dùng
     */
    public List<UserResponse> getTopUsersByTotalQuizPlays() {
        return userRepository.findTopUsersByTotalQuizPlays()
                .stream()
                .map(user -> UserResponse.fromUser(user, localStorageService))
                .collect(Collectors.toList());
    }

    /**
     * Đếm tổng số người dùng trong hệ thống
     *
     * @return Tổng số người dùng
     */
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * Lấy danh sách người dùng theo trang với các tùy chọn lọc và sắp xếp
     *
     * @param page     Số trang (bắt đầu từ 0)
     * @param pageSize Số lượng kết quả mỗi trang
     * @param search   Từ khóa tìm kiếm theo tên hoặc username
     * @param sort     Cách sắp xếp kết quả (newest, oldest, name, username)
     * @return Danh sách người dùng theo trang
     */
    public PageResponse<UserResponse> getPagedUsers(
            int page,
            int pageSize,
            String search,
            String sort) {

        Pageable pageable;

        // Xử lý sắp xếp
        if (sort != null) {
            pageable = switch (sort) {
                case "newest" -> PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
                case "oldest" -> PageRequest.of(page, pageSize, Sort.by("createdAt").ascending());
                case "name" -> PageRequest.of(page, pageSize, Sort.by("fullName").ascending());
                case "username" -> PageRequest.of(page, pageSize, Sort.by("username").ascending());
                default -> PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
            };
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        }

        Page<User> userPage;

        // Xử lý tìm kiếm
        if (search != null && !search.trim().isEmpty()) {
            userPage = userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserResponse> userResponses = userPage.getContent()
                .stream()
                .map(user -> UserResponse.fromUser(user, localStorageService))
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .content(userResponses)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    /**
     * Lấy thông tin profile của người dùng theo ID
     *
     * @param userId ID của người dùng
     * @return Thông tin profile của người dùng
     */
    public UserProfileResponse getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (user.getUserProfile() == null) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User profile not found for user with id: " + userId);
                    }
                    return UserProfileResponse.fromUserProfile(user.getUserProfile(), localStorageService);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + userId));
    }

    /**
     * Lấy thông tin profile của người dùng hiện tại
     *
     * @param user Thông tin người dùng hiện tại đã đăng nhập
     * @return Thông tin profile của người dùng
     */
    public UserProfileResponse getCurrentUserProfile(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        if (user.getUserProfile() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User profile not found for current user");
        }

        return UserProfileResponse.fromUserProfile(user.getUserProfile(), localStorageService);
    }

    /**
     * Upload avatar cho người dùng hiện tại
     *
     * @param user       Thông tin người dùng hiện tại đã đăng nhập
     * @param avatarFile File ảnh avatar cần upload
     * @return Thông tin người dùng đã cập nhật
     */
    @Transactional
    public UserResponse uploadAvatar(User user, MultipartFile avatarFile) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file is required");
        }

        // Validate file using utility
        String validationError = FileValidationUtil.getImageValidationError(avatarFile);
        if (validationError != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationError);
        }

        try {
            // Lưu avatar cũ để xóa sau khi upload thành công
            String oldAvatarUrl = user.getProfileImage();

            // Upload ảnh mới
            String newAvatarFilename = localStorageService.uploadProfileImage(avatarFile, user.getId());

            if (newAvatarFilename == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload avatar");
            }

            // Cập nhật thông tin người dùng
            user.setProfileImage(newAvatarFilename);
            User savedUser = userRepository.save(user);

            // Xóa avatar cũ nếu có
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                localStorageService.deleteProfileImage(oldAvatarUrl);
            }

            return UserResponse.fromUser(savedUser, localStorageService);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload avatar: " + e.getMessage());
        }
    }

    /**
     * Xóa avatar của người dùng hiện tại
     *
     * @param user Thông tin người dùng hiện tại đã đăng nhập
     * @return Thông tin người dùng đã cập nhật
     */
    @Transactional
    public UserResponse removeAvatar(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no avatar to remove");
        }

        try {
            // Lưu URL avatar cũ để xóa
            String oldAvatarUrl = user.getProfileImage();

            // Cập nhật thông tin người dùng
            user.setProfileImage(null);
            User savedUser = userRepository.save(user);

            // Xóa file avatar cũ
            localStorageService.deleteProfileImage(oldAvatarUrl);

            return UserResponse.fromUser(savedUser, localStorageService);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to remove avatar: " + e.getMessage());
        }
    }

    /**
     * Thêm một người dùng mới vào hệ thống
     *
     * @param userRequest Thông tin người dùng mới
     * @return Thông tin người dùng đã được thêm
     */
    @Transactional
    public UserResponse addUser(
            UserRequest userRequest
    ) {
        if (userRequest == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User information is required");
        }

        // Kiểm tra xem người dùng đã tồn tại chưa
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        // Kiểm tra xem email đã tồn tại chưa
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // Tạo người dùng mới
        User user = User.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .fullName(userRequest.getFullName())
                .role(userRequest.getRole())
                .isActive(userRequest.isActive()) // Gán trạng thái hoạt động
                .build();

        // Lưu người dùng mới
        User savedUser = userRepository.save(user);

        // Thêm ảnh đại diện nếu có
        MultipartFile avatarFile = userRequest.getProfileImage();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Validate file using utility
            String validationError = FileValidationUtil.getImageValidationError(avatarFile);
            if (validationError != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationError);
            }

            // Upload ảnh đại diện
            String avatarFilename = localStorageService.uploadProfileImage(avatarFile, user.getId());
            if (avatarFilename == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload avatar");
            }
            user.setProfileImage(avatarFilename);
        }

        // Trả về thông tin người dùng đã lưu
        return UserResponse.fromUser(savedUser, localStorageService);
    }

    /**
     * Cập nhật thông tin người dùng
     *
     * @param userId      ID của người dùng cần cập nhật
     * @param userRequest Thông tin mới của người dùng
     * @return Thông tin người dùng đã cập nhật
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserRequest userRequest) {
        if (userRequest == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User information is required");
        }

        // Tìm người dùng theo ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        // Cập nhật thông tin người dùng
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setFullName(userRequest.getFullName());
        user.setRole(userRequest.getRole());
        user.setActive(userRequest.isActive());

        // Mã hóa mật khẩu nếu có thay đổi
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        // Lưu người dùng đã cập nhật
        User savedUser = userRepository.save(user);

        // Thêm ảnh đại diện nếu có
        MultipartFile avatarFile = userRequest.getProfileImage();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Validate file using utility
            String validationError = FileValidationUtil.getImageValidationError(avatarFile);
            if (validationError != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationError);
            }

            // Upload ảnh đại diện
            String avatarFilename = localStorageService.uploadProfileImage(avatarFile, savedUser.getId());
            if (avatarFilename == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload avatar");
            }
            savedUser.setProfileImage(avatarFilename);
        }

        // Trả về thông tin người dùng đã cập nhật
        return UserResponse.fromUser(savedUser, localStorageService);
    }

    /**
     * Xóa người dùng theo ID
     *
     * @param userId ID của người dùng cần xóa
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        // Xóa ảnh đại diện nếu có
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            localStorageService.deleteProfileImage(user.getProfileImage());
        }

        // Xóa người dùng
        userRepository.delete(user);
    }

    /**
     * Khóa hoặc mở khóa người dùng theo ID
     *
     * @param userId ID của người dùng cần khóa hoặc mở khóa
     * @return Thông tin người dùng đã cập nhật
     */
    @Transactional
    public UserResponse toggleUserActiveStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        // Chuyển đổi trạng thái hoạt động
        user.setActive(isActive);

        // Lưu người dùng đã cập nhật
        User savedUser = userRepository.save(user);

        return UserResponse.fromUser(savedUser, localStorageService);
    }
}
