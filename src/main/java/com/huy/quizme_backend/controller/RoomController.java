package com.huy.quizme_backend.controller;

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

    // Thêm các API khác như startRoom, endRoom, leaveRoom, etc.
}