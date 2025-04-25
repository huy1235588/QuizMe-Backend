package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.response.QuestionResponse;
import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.QuestionOption;
import com.huy.quizme_backend.repository.QuestionOptionRepository;
import com.huy.quizme_backend.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final CloudinaryService cloudinaryService;
    
    /**
     * Lấy danh sách tất cả các câu hỏi
     * @return Danh sách các QuestionResponse
     */
    public List<QuestionResponse> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        
        // Lấy tất cả ID của các câu hỏi
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        
        // Lấy tất cả options cho các câu hỏi
        List<QuestionOption> allOptions = questionOptionRepository.findByQuestionIdIn(questionIds);
        
        // Nhóm options theo questionId
        Map<Long, List<QuestionOption>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));
        
        // Chuyển đổi các câu hỏi thành QuestionResponse kèm theo options
        return questions.stream()
                .map(question -> {
                    List<QuestionOption> options = optionsByQuestionId.get(question.getId());
                    return QuestionResponse.fromQuestionWithOptions(question, options, cloudinaryService);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy câu hỏi theo ID
     * @param id ID của câu hỏi
     * @return QuestionResponse
     */
    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Question not found with id: " + id));
        
        List<QuestionOption> options = questionOptionRepository.findByQuestionId(id);
        
        return QuestionResponse.fromQuestionWithOptions(question, options, cloudinaryService);
    }
    
    /**
     * Lấy danh sách các câu hỏi theo quiz ID
     * @param quizId ID của quiz
     * @return Danh sách các QuestionResponse
     */
    public List<QuestionResponse> getQuestionsByQuizId(Long quizId) {
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderNumber(quizId);
        
        if (questions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No questions found for quiz id: " + quizId);
        }
        
        // Lấy tất cả ID của các câu hỏi
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        
        // Lấy tất cả options cho các câu hỏi
        List<QuestionOption> allOptions = questionOptionRepository.findByQuestionIdIn(questionIds);
        
        // Nhóm options theo questionId
        Map<Long, List<QuestionOption>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));
        
        // Chuyển đổi các câu hỏi thành QuestionResponse kèm theo options
        return questions.stream()
                .map(question -> {
                    List<QuestionOption> options = optionsByQuestionId.get(question.getId());
                    return QuestionResponse.fromQuestionWithOptions(question, options, cloudinaryService);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Tạo tên file hình ảnh cho câu hỏi theo định dạng quy định
     * @param quizId ID của quiz
     * @param questionId ID của câu hỏi
     * @return Tên file theo quy tắc
     */
    public String generateQuestionImageFilename(Long quizId, Long questionId) {
        return cloudinaryService.generateQuestionImageFilename(quizId, questionId);
    }
}