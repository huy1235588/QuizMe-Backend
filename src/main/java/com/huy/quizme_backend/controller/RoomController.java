package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.JoinRoomByIdRequest;
import com.huy.quizme_backend.dto.request.JoinRoomRequest;
import com.huy.quizme_backend.dto.request.RoomRequest;
import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.RoomResponse;
import com.huy.quizme_backend.enity.Room;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * API tạo phòng mới
     *
     * @param roomRequest Thông tin phòng
     * @param principal   Thông tin người dùng hiện tại
     * @return Thông tin phòng đã tạo
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoomResponse> createRoom(
            @Valid @RequestBody RoomRequest roomRequest,
            Principal principal
    ) {
        // Lấy user hiện tại
        User currentUser = (User) ((Authentication) principal).getPrincipal();
        Long hostId = currentUser.getId();

        // Tạo phòng mới
        RoomResponse createdRoom = roomService.createRoom(roomRequest, hostId);

        return ApiResponse.created(createdRoom, "Room created successfully");
    }

    /**
     * API tham gia phòng
     *
     * @param joinRequest Yêu cầu tham gia phòng
     * @param principal   Thông tin người dùng hiện tại (có thể null)
     * @return Thông tin phòng
     */
    @PostMapping("/join")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<RoomResponse> joinRoom(
            @Valid @RequestBody JoinRoomRequest joinRequest,
            Principal principal
    ) {
        // Lấy user ID nếu người dùng đã đăng nhập
        Long userId = null;
        if (principal != null) {
            User currentUser = (User) ((Authentication) principal).getPrincipal();
            userId = currentUser.getId();
        }

        // Tham gia phòng
        RoomResponse room = roomService.joinRoom(joinRequest, userId);

        return ApiResponse.success(room, "Joined room successfully");
    }

    /**
     * API tham gia phòng trực tiếp bằng ID
     *
     * @param roomId      ID của phòng
     * @param joinRequest Yêu cầu tham gia phòng
     * @param principal   Thông tin người dùng hiện tại (có thể null)
     * @return Thông tin phòng
     */
    @PostMapping("/join/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<RoomResponse> joinRoomById(
            @PathVariable Long roomId,
            @RequestBody(required = false) JoinRoomByIdRequest joinRequest,
            Principal principal
    ) {
        // Lấy user ID nếu người dùng đã đăng nhập
        Long userId = null;
        if (principal != null) {
            User currentUser = (User) ((Authentication) principal).getPrincipal();
            userId = currentUser.getId();
        }

        String guestName = joinRequest != null ? joinRequest.getGuestName() : null;
        String password = joinRequest != null ? joinRequest.getPassword() : null;

        // Tham gia phòng theo ID
        RoomResponse room = roomService.joinRoomById(roomId, userId, guestName, password);

        return ApiResponse.success(room, "Joined room successfully");
    }

    /**
     * API rời phòng
     *
     * @param roomId    ID của phòng
     * @param guestName Tên khách (nếu là khách)
     * @param principal Thông tin người dùng hiện tại (có thể null)
     * @return Thông báo thành công
     */
    @DeleteMapping("/leave/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> leaveRoom(
            @PathVariable Long roomId,
            @RequestParam(required = false) String guestName,
            Principal principal
    ) {
        // Lấy user ID nếu người dùng đã đăng nhập
        Long userId = null;
        if (principal != null) {
            User currentUser = (User) ((Authentication) principal).getPrincipal();
            userId = currentUser.getId();
        }

        String result = roomService.leaveRoom(roomId, userId, guestName);
        return ApiResponse.success(result, "Left room successfully");
    }

    /**
     * API đóng phòng (chỉ host mới có quyền)
     *
     * @param roomId    ID của phòng
     * @param principal Thông tin người dùng hiện tại
     * @return Thông tin phòng đã đóng
     */
    @PatchMapping("/close/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<RoomResponse> closeRoom(
            @PathVariable Long roomId,
            Principal principal
    ) {
        // Chỉ user đã đăng nhập mới có thể đóng phòng
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User currentUser = (User) ((Authentication) principal).getPrincipal();
        Long userId = currentUser.getId();

        RoomResponse room = roomService.closeRoom(roomId, userId);
        return ApiResponse.success(room, "Room closed successfully");
    }

    /**
     * API lấy thông tin phòng theo mã
     *
     * @param code Mã phòng
     * @return Thông tin phòng
     */
    @GetMapping("/{code}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<RoomResponse> getRoomByCode(@PathVariable String code) {
        RoomResponse room = roomService.getRoomByCode(code);
        return ApiResponse.success(room, "Room retrieved successfully");
    }

    /**
     * API lấy danh sách phòng đang chờ
     *
     * @return Danh sách phòng
     */
    @GetMapping("/waiting")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<RoomResponse>> getWaitingRooms() {
        List<RoomResponse> rooms = roomService.getRoomsByStatus(Room.Status.waiting);
        return ApiResponse.success(rooms, "Waiting rooms retrieved successfully");
    }

    /**
     * API lấy danh sách phòng đang chờ
     *
     * @return Danh sách phòng
     */
    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<RoomResponse>> getAvailableRooms(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search) {
        List<RoomResponse> rooms = roomService.getAvailableRooms(categoryId, search);
        return ApiResponse.success(rooms, "Available rooms retrieved successfully");
    }

    /**
     * API cập nhật thông tin phòng (chỉ host mới có quyền)
     *
     * @param roomId      ID của phòng
     * @param roomRequest Thông tin cập nhật
     * @param principal   Thông tin người dùng hiện tại
     * @return Thông tin phòng đã cập nhật
     */
    @PatchMapping("/{roomId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<RoomResponse> updateRoom(
            @PathVariable Long roomId,
            @RequestBody RoomRequest roomRequest,
            Principal principal
    ) {
        // Chỉ user đã đăng nhập mới có thể cập nhật phòng
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User currentUser = (User) ((Authentication) principal).getPrincipal();
        Long userId = currentUser.getId();

        RoomResponse room = roomService.updateRoom(roomId, userId, roomRequest);
        return ApiResponse.success(room, "Room updated successfully");
    }

    // Thêm các API khác như startRoom, endRoom, leaveRoom, etc.
}