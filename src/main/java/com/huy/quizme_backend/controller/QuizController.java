package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.QuizResponse;
import com.huy.quizme_backend.enity.Difficulty;
import com.huy.quizme_backend.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;
    
    /**
     * API lấy danh sách tất cả các quiz
     * @return Danh sách các QuizResponse
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<QuizResponse>> getAllQuizzes() {
        List<QuizResponse> quizzes = quizService.getAllQuizzes();
        return ApiResponse.success(quizzes, "Quizzes retrieved successfully");
    }
    
    /**
     * API lấy quiz theo ID
     * @param id ID của quiz
     * @return QuizResponse
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<QuizResponse> getQuizById(@PathVariable Long id) {
        QuizResponse quiz = quizService.getQuizById(id);
        return ApiResponse.success(quiz, "Quiz retrieved successfully");
    }
    
    /**
     * API lấy danh sách các quiz công khai
     * @return Danh sách các QuizResponse
     */
    @GetMapping("/public")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<QuizResponse>> getPublicQuizzes() {
        List<QuizResponse> quizzes = quizService.getPublicQuizzes();
        return ApiResponse.success(quizzes, "Public quizzes retrieved successfully");
    }

    /**
     * API lấy danh sách các quiz theo độ khó
     * @param difficulty Độ khó của quiz
     * @return Danh sách các QuizResponse
     */
    @GetMapping("/difficulty/{difficulty}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<QuizResponse>> getQuizzesByDifficulty(@PathVariable Difficulty difficulty) {
        List<QuizResponse> quizzes = quizService.getQuizzesByDifficulty(difficulty);
        return ApiResponse.success(quizzes, "Quizzes by difficulty retrieved successfully");
    }
}