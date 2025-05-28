package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.QuestionRequest;
import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.QuestionOption;
import com.huy.quizme_backend.repository.QuestionOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionOptionService {
    private final QuestionOptionRepository questionOptionRepository;

    /**
     * Lấy tất cả các options cho một câu hỏi
     *
     * @param questionId ID của câu hỏi
     * @return Danh sách các QuestionOption
     */
    public List<QuestionOption> getOptionsByQuestionId(Long questionId) {
        return questionOptionRepository.findByQuestionId(questionId);
    }

    /**
     * Lấy tất cả các options cho nhiều câu hỏi
     *
     * @param questionIds Danh sách ID của các câu hỏi
     * @return Danh sách các QuestionOption
     */
    public List<QuestionOption> getOptionsByQuestionIds(List<Long> questionIds) {
        return questionOptionRepository.findByQuestionIdIn(questionIds);
    }

    /**
     * Nhóm options theo questionId
     *
     * @param options Danh sách các QuestionOption
     * @return Map với key là questionId và value là danh sách QuestionOption
     */
    public Map<Long, List<QuestionOption>> groupOptionsByQuestionId(List<QuestionOption> options) {
        return options.stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));
    }

    /**
     * Tạo các options cho một câu hỏi
     *
     * @param question       Question đã được lưu
     * @param optionRequests Danh sách các QuestionOptionRequest
     * @return Danh sách các QuestionOption đã tạo
     */
    @Transactional
    public List<QuestionOption> createOptionsForQuestion(Question question,
                                                         List<QuestionRequest.QuestionOptionRequest> optionRequests) {
        if (optionRequests == null || optionRequests.isEmpty()) {
            return new ArrayList<>();
        }

        // Kiểm tra xem có ít nhất một lựa chọn đúng
        boolean hasCorrectOption = optionRequests.stream()
                .anyMatch(QuestionRequest.QuestionOptionRequest::getIsCorrect);

        if (!hasCorrectOption) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Question must have at least one correct option");
        }

        // Tạo và lưu các lựa chọn
        List<QuestionOption> savedOptions = new ArrayList<>();
        for (QuestionRequest.QuestionOptionRequest optionRequest : optionRequests) {
            QuestionOption option = QuestionOption.builder()
                    .question(question)
                    .content(optionRequest.getContent())
                    .isCorrect(optionRequest.getIsCorrect())
                    .build();

            savedOptions.add(questionOptionRepository.save(option));
        }

        return savedOptions;
    }

    /**
     * Tạo các options cho nhiều câu hỏi
     *
     * @param questionOptions Map với key là Question và value là danh sách các QuestionOptionRequest
     * @return Danh sách các QuestionOption đã tạo
     */
    @Transactional
    public List<QuestionOption> createOptionsForQuestions(
            Map<Question, List<QuestionRequest.QuestionOptionRequest>> questionOptions
    ) {
        List<QuestionOption> allSavedOptions = new ArrayList<>();

        for (Map.Entry<Question, List<QuestionRequest.QuestionOptionRequest>> entry : questionOptions.entrySet()) {
            Question question = entry.getKey();
            List<QuestionRequest.QuestionOptionRequest> optionRequests = entry.getValue();

            // Tạo và lưu các lựa chọn cho từng câu hỏi
            List<QuestionOption> savedOptions = createOptionsForQuestion(question, optionRequests);
            allSavedOptions.addAll(savedOptions);
        }

        return allSavedOptions;
    }

    /**
     * Cập nhật các options cho một câu hỏi
     *
     * @param question       Question đã được lưu
     * @param optionRequests Danh sách các QuestionOptionRequest mới
     * @return Danh sách các QuestionOption đã cập nhật
     */
    @Transactional
    public List<QuestionOption> updateOptionsForQuestion(Question question,
                                                         List<QuestionRequest.QuestionOptionRequest> optionRequests) {
        // Xóa tất cả các lựa chọn cũ
        List<QuestionOption> oldOptions = questionOptionRepository.findByQuestionId(question.getId());
        questionOptionRepository.deleteAll(oldOptions);

        // Tạo mới các options
        return createOptionsForQuestion(question, optionRequests);
    }

    /**
     * Xóa tất cả options của một câu hỏi
     *
     * @param questionId ID của câu hỏi
     */
    @Transactional
    public void deleteAllOptionsForQuestion(Long questionId) {
        List<QuestionOption> options = questionOptionRepository.findByQuestionId(questionId);
        questionOptionRepository.deleteAll(options);
    }
}