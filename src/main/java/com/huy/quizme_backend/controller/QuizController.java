package com.huy.quizme_backend.controller;

import com.huy.quizme_backend.dto.request.QuizRequest;
import com.huy.quizme_backend.dto.response.ApiResponse;
import com.huy.quizme_backend.dto.response.PageResponse;
import com.huy.quizme_backend.dto.response.QuizResponse;
import com.huy.quizme_backend.enity.Difficulty;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    
    /**
     * API lấy danh sách các quiz theo phân trang và lọc
     * @param page Số trang (bắt đầu từ 0)
     * @param pageSize Số lượng kết quả mỗi trang
     * @param category ID của category cần lọc
     * @param search Từ khóa tìm kiếm
     * @param difficulty Độ khó của quiz
     * @param sort Cách sắp xếp kết quả (newest, popular)
     * @param isPublic Trạng thái công khai
     * @param tab Tab hiện tại (newest, popular)
     * @return Danh sách các QuizResponse theo trang
     */
    @GetMapping("/paged")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResponse<QuizResponse>> getPagedQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) String tab) {
            
        PageResponse<QuizResponse> pagedQuizzes = quizService.getPagedQuizzes(
                page, pageSize, category, search, difficulty, sort, isPublic, tab);
                
        return ApiResponse.success(pagedQuizzes, "Paged quizzes retrieved successfully");
    }

    /**
     * API tạo mới quiz
     * @param quizRequest Thông tin quiz cần tạo
     * @param principal Thông tin người dùng hiện tại
     * @return QuizResponse của quiz đã tạo
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuizResponse> createQuiz(
            @ModelAttribute @Valid QuizRequest quizRequest,
            Principal principal) {
        // Lấy user hiện tại
        User currentUser = (User) ((Authentication) principal).getPrincipal();
        Long creatorId = currentUser.getId();
        
        // Tạo quiz mới
        QuizResponse createdQuiz = quizService.createQuiz(quizRequest, creatorId);
        
        return ApiResponse.created(createdQuiz, "Quiz created successfully");
    }

    /**
     * API cập nhật thông tin quiz
     * @param id ID của quiz cần cập nhật
     * @param quizRequest Thông tin mới của quiz
     * @param principal Thông tin người dùng hiện tại
     * @return QuizResponse của quiz đã cập nhật
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<QuizResponse> updateQuiz(
            @PathVariable Long id,
            @ModelAttribute @Valid QuizRequest quizRequest,
            Principal principal) {
        // Lấy user hiện tại
        User currentUser = (User) ((Authentication) principal).getPrincipal();
        Long currentUserId = currentUser.getId();
        
        // Cập nhật quiz
        QuizResponse updatedQuiz = quizService.updateQuiz(id, quizRequest, currentUserId);
        
        return ApiResponse.success(updatedQuiz, "Quiz updated successfully");
    }

    /**
     * API xóa quiz
     * @param id ID của quiz cần xóa
     * @param principal Thông tin người dùng hiện tại
     * @return Thông báo xóa thành công
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteQuiz(
            @PathVariable Long id,
            Principal principal) {
        // Lấy user hiện tại
        User currentUser = (User) ((Authentication) principal).getPrincipal();
        Long currentUserId = currentUser.getId();
        
        // Xóa quiz
        quizService.deleteQuiz(id, currentUserId);
        
        return ApiResponse.success(null, "Quiz deleted successfully");
    }
}