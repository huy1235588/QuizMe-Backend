package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.QuestionRequest;
import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.QuestionResponse;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    
    /**
     * API tạo mới câu hỏi
     * @param questionRequest Thông tin câu hỏi cần tạo
     * @return QuestionResponse của câu hỏi đã tạo
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuestionResponse> createQuestion(
            @ModelAttribute @Valid QuestionRequest questionRequest) {
        QuestionResponse createdQuestion = questionService.createQuestion(questionRequest);
        return ApiResponse.created(createdQuestion, "Question created successfully");
    }
    
    /**
     * API cập nhật thông tin câu hỏi
     * @param id ID của câu hỏi cần cập nhật
     * @param questionRequest Thông tin mới của câu hỏi
     * @return QuestionResponse của câu hỏi đã cập nhật
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<QuestionResponse> updateQuestion(
            @PathVariable Long id,
            @ModelAttribute @Valid QuestionRequest questionRequest) {
        QuestionResponse updatedQuestion = questionService.updateQuestion(id, questionRequest);
        return ApiResponse.success(updatedQuestion, "Question updated successfully");
    }
    
    /**
     * API xóa câu hỏi
     * @param id ID của câu hỏi cần xóa
     * @return Thông báo xóa thành công
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ApiResponse.success(null, "Question deleted successfully");
    }
}