package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.QuestionResponse;
import com.huy.quizme_backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    
    /**
     * API lấy danh sách tất cả các câu hỏi
     * @return Danh sách các QuestionResponse
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<QuestionResponse>> getAllQuestions() {
        List<QuestionResponse> questions = questionService.getAllQuestions();
        return ApiResponse.success(questions, "Questions retrieved successfully");
    }
    
    /**
     * API lấy câu hỏi theo ID
     * @param id ID của câu hỏi
     * @return QuestionResponse
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<QuestionResponse> getQuestionById(@PathVariable Long id) {
        QuestionResponse question = questionService.getQuestionById(id);
        return ApiResponse.success(question, "Question retrieved successfully");
    }
    
    /**
     * API lấy danh sách các câu hỏi theo quiz ID
     * @param quizId ID của quiz
     * @return Danh sách các QuestionResponse
     */
    @GetMapping("/quiz/{quizId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<QuestionResponse>> getQuestionsByQuizId(@PathVariable Long quizId) {
        List<QuestionResponse> questions = questionService.getQuestionsByQuizId(quizId);
        return ApiResponse.success(questions, "Questions for quiz retrieved successfully");
    }
}