package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.response.PageResponse;
import com.huy.quizme_backend.dto.response.QuizResponse;
import com.huy.quizme_backend.enity.Difficulty;
import com.huy.quizme_backend.enity.Quiz;
import com.huy.quizme_backend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Lấy danh sách tất cả các quiz
     *
     * @return Danh sách các QuizResponse
     */
    public List<QuizResponse> getAllQuizzes() {
        return quizRepository.findAll().
                stream()
                .map(quiz -> QuizResponse.fromQuiz(quiz, cloudinaryService))
                .collect(Collectors.toList());
    }

    /**
     * Lấy quiz theo ID
     *
     * @param id ID của quiz
     * @return QuizResponse
     */
    public QuizResponse getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz not found with id: " + id));
        return QuizResponse.fromQuiz(quiz, cloudinaryService);
    }

    /**
     * Lấy danh sách các quiz công khai
     *
     * @return Danh sách các QuizResponse
     */
    public List<QuizResponse> getPublicQuizzes() {
        return quizRepository.findByIsPublicTrue()
                .stream()
                .map(quiz -> QuizResponse.fromQuiz(quiz, cloudinaryService))
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách các quiz theo độ khó
     *
     * @param difficulty Độ khó của quiz
     * @return Danh sách các QuizResponse
     */
    public List<QuizResponse> getQuizzesByDifficulty(Difficulty difficulty) {
        return quizRepository.findByDifficulty(difficulty)
                .stream()
                .map(quiz -> QuizResponse.fromQuiz(quiz, cloudinaryService))
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách các quiz theo phân trang và lọc
     *
     * @param page Số trang (bắt đầu từ 0)
     * @param pageSize Số lượng kết quả mỗi trang
     * @param categoryId ID của category cần lọc
     * @param search Từ khóa tìm kiếm
     * @param difficulty Độ khó của quiz
     * @param sort Cách sắp xếp kết quả
     * @param isPublic Trạng thái công khai
     * @param tab Tab hiện tại (newest, popular)
     * @return Danh sách các QuizResponse theo trang
     */
    public PageResponse<QuizResponse> getPagedQuizzes(
            int page, 
            int pageSize, 
            Long categoryId, 
            String search, 
            Difficulty difficulty, 
            String sort, 
            Boolean isPublic, 
            String tab) {
        
        Pageable pageable;
        Page<Quiz> quizPage;
        
        // Xử lý sắp xếp
        if (sort != null) {
            pageable = switch (sort) {
                case "newest" -> PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
                case "popular" -> PageRequest.of(page, pageSize, Sort.by("playCount").descending());
                default -> PageRequest.of(page, pageSize);
            };
        } else {
            pageable = PageRequest.of(page, pageSize);
        }
        
        // Xử lý tab
        if (tab != null) {
            quizPage = switch (tab) {
                case "newest" -> quizRepository.findNewestQuizzes(tab, pageable);
                case "popular" -> quizRepository.findPopularQuizzes(tab, pageable);
                default -> quizRepository.findQuizzesWithFilters(
                        categoryId, difficulty, isPublic, search, pageable);
            };
        } else {
            quizPage = quizRepository.findQuizzesWithFilters(
                    categoryId, difficulty, isPublic, search, pageable);
        }
        
        List<QuizResponse> quizResponses = quizPage.getContent()
                .stream()
                .map(quiz -> QuizResponse.fromQuiz(quiz, cloudinaryService))
                .collect(Collectors.toList());
        
        return PageResponse.<QuizResponse>builder()
                .content(quizResponses)
                .pageNumber(quizPage.getNumber())
                .pageSize(quizPage.getSize())
                .totalElements(quizPage.getTotalElements())
                .totalPages(quizPage.getTotalPages())
                .last(quizPage.isLast())
                .build();
    }
}