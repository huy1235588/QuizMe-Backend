package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.response.QuizResponse;
import com.huy.quizme_backend.enity.Difficulty;
import com.huy.quizme_backend.enity.Quiz;
import com.huy.quizme_backend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
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
     * Tạo tên file thumbnail cho quiz theo định dạng quy định
     * @param quizId ID của quiz
     * @return Tên file theo quy tắc
     */
    public String generateQuizThumbnailFilename(Long quizId) {
        return cloudinaryService.generateQuizThumbnailFilename(quizId);
    }
}