package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.UserRequest;
import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.PageResponse;
import com.huy.quizme_backend.dto.response.UserProfileResponse;
import com.huy.quizme_backend.dto.response.UserResponse;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.enity.enums.Role;
import com.huy.quizme_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * API lấy thông tin người dùng theo ID
     *
     * @param id ID của người dùng
     * @return Thông tin người dùng
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ApiResponse.success(user, "User retrieved successfully");
    }

    /**
     * API lấy danh sách người dùng có tổng số quiz được chơi nhiều nhất
     *
     * @return Danh sách người dùng
     */
    @GetMapping("/top")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<UserResponse>> getTopUsersByTotalQuizPlays() {
        List<UserResponse> topUsers = userService.getTopUsersByTotalQuizPlays();
        return ApiResponse.success(topUsers, "Top users retrieved successfully");
    }

    /**
     * API lấy tổng số người dùng trong hệ thống
     *
     * @return Tổng số người dùng
     */
    @GetMapping("/count")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Map<String, Long>> getUserCount() {
        long count = userService.countUsers();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ApiResponse.success(response, "User count retrieved successfully");
    }

    /**
     * API lấy danh sách người dùng theo phân trang và lọc
     *
     * @param page     Số trang (bắt đầu từ 0)
     * @param pageSize Số lượng kết quả mỗi trang
     * @param search   Từ khóa tìm kiếm theo tên hoặc username
     * @param sort     Cách sắp xếp kết quả (newest, oldest, name, username)
     * @return Danh sách người dùng theo trang
     */
    @GetMapping("/paged")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponse<UserResponse>> getPagedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {

        PageResponse<UserResponse> pagedUsers = userService.getPagedUsers(
                page, pageSize, search, sort);

        return ApiResponse.success(pagedUsers, "Paged users retrieved successfully");
    }

    /**
     * API lấy thông tin profile của người dùng hiện tại đã đăng nhập
     *
     * @param principal Thông tin người dùng đăng nhập hiện tại
     * @return Thông tin profile người dùng
     */
    @GetMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileResponse> getCurrentUserProfile(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User currentUser = (User) ((Authentication) principal).getPrincipal();
        UserProfileResponse profile = userService.getCurrentUserProfile(currentUser);

        return ApiResponse.success(profile, "User profile retrieved successfully");
    }

    /**
     * API lấy thông tin profile của người dùng theo ID
     *
     * @param id ID của người dùng
     * @return Thông tin profile người dùng
     */
    @GetMapping("/profile/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileResponse> getUserProfileById(@PathVariable Long id) {
        UserProfileResponse profile = userService.getUserProfile(id);
        return ApiResponse.success(profile, "User profile retrieved successfully");
    }

    /**
     * API upload avatar cho người dùng hiện tại
     *
     * @param principal Thông tin người dùng đăng nhập hiện tại
     * @return Thông tin người dùng đã cập nhật
     */
    @PostMapping("/avatar/upload")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> uploadAvatar(
            @RequestParam("avatarFile") MultipartFile avatarFile,
            Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User currentUser = (User) ((Authentication) principal).getPrincipal();
        UserResponse updatedUser = userService.uploadAvatar(currentUser, avatarFile);

        return ApiResponse.success(updatedUser, "Avatar uploaded successfully");
    }

    /**
     * API xóa avatar của người dùng hiện tại
     *
     * @param principal Thông tin người dùng đăng nhập hiện tại
     * @return Thông tin người dùng đã cập nhật
     */
    @DeleteMapping("/avatar")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserResponse> removeAvatar(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User currentUser = (User) ((Authentication) principal).getPrincipal();
        UserResponse updatedUser = userService.removeAvatar(currentUser);

        return ApiResponse.success(updatedUser, "Avatar removed successfully");
    }

    /**
     * API thêm người dùng mới (chỉ dành cho quản trị viên)
     *
     * @param userRequest Thông tin người dùng mới
     * @return Thông tin người dùng đã thêm
     */
    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> addUser(
            @ModelAttribute @Valid UserRequest userRequest
    ) {
        // Thêm người dùng mới
        UserResponse newUser = userService.addUser(userRequest);
        return ApiResponse.success(newUser, "User added successfully");
    }

    /**
     * API cập nhật thông tin người dùng (chỉ dành cho quản trị viên)
     *
     * @param id          ID của người dùng cần cập nhật
     * @param userRequest Thông tin người dùng mới
     * @return Thông tin người dùng đã cập nhật
     */
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @ModelAttribute @Valid UserRequest userRequest
    ) {
        // Cập nhật thông tin người dùng
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        return ApiResponse.success(updatedUser, "User updated successfully");
    }

    /**
     * API xóa người dùng (chỉ dành cho quản trị viên)
     *
     * @param id ID của người dùng cần xóa
     * @return Thông báo thành công
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // Xóa người dùng
        userService.deleteUser(id);
        return ApiResponse.success(null, "User deleted successfully");
    }

    /**
     * Khoá hoặc mở khoá người dùng (chỉ dành cho quản trị viên)
     *
     * @param id ID của người dùng cần khoá hoặc mở khoá
     * @return Thông tin người dùng đã cập nhật
     */
    @PutMapping("/{id}/lock")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> toggleUserActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean isActive
    ) {
        // Khoá hoặc mở khoá người dùng
        UserResponse updatedUser = userService.toggleUserActiveStatus(id, isActive);
        return ApiResponse.success(updatedUser, isActive ? "User locked successfully" : "User unlocked successfully");
    }
}