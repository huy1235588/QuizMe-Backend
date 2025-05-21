package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.RoomResponse;
import com.huy.quizme_backend.dto.response.GameStatusResponse;
import com.huy.quizme_backend.service.GameSessionService;
import com.huy.quizme_backend.service.GameProgressService;
import com.huy.quizme_backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * APIs để quản lý trò chơi.
 */
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameRestController {
    private final GameSessionService gameSessionService;
    private final GameProgressService gameProgressService;
    private final RoomService roomService;

    /**
     * Bắt đầu trò chơi trong phòng.
     */
    @PostMapping("/rooms/{roomId}/start")
    public ApiResponse<RoomResponse> startGame(@PathVariable Long roomId, Principal principal) {
        throw new UnsupportedOperationException("Chưa triển khai startGame API");
    }

    /**
     * Lấy trạng thái trò chơi.
     */
    @GetMapping("/rooms/{roomId}/status")
    public ApiResponse<GameStatusResponse> getGameStatus(@PathVariable Long roomId) {
        throw new UnsupportedOperationException("Chưa triển khai getGameStatus API");
    }

    /**
     * Lấy kết quả trò chơi.
     */
    @GetMapping("/rooms/{roomId}/results")
    public ApiResponse<?> getGameResults(@PathVariable Long roomId) {
        throw new UnsupportedOperationException("Chưa triển khai getGameResults API");
    }
}
