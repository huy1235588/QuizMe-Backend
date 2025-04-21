package com.huy.quizme_backend.exception;

import com.huy.quizme_backend.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {
    // Bắt riêng ResponseStatusException để trả đúng status
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<String>> handleResponseStatusException(ResponseStatusException ex) {
        // Tạo phản hồi lỗi
        ApiResponse<String> response = ApiResponse.error(ex.getReason());

        // Trả về phản hồi với mã trạng thái BAD_REQUEST
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(response);
    }

    // Bắt tất cả các RuntimeException khác
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleYourCustomException(RuntimeException ex) {
        // Tạo phản hồi lỗi
        ApiResponse<String> response = ApiResponse.error(ex.getMessage());

        // Trả về phản hồi với mã trạng thái BAD_REQUEST
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
