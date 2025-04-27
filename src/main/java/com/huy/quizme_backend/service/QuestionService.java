package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.QuestionRequest;
import com.huy.quizme_backend.dto.response.QuestionResponse;
import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.QuestionOption;
import com.huy.quizme_backend.enity.Quiz;
import com.huy.quizme_backend.repository.QuestionOptionRepository;
import com.huy.quizme_backend.repository.QuestionRepository;
import com.huy.quizme_backend.repository.QuizRepository;
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
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuizRepository quizRepository;
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
     * Tạo mới câu hỏi
     * @param questionRequest thông tin câu hỏi cần tạo
     * @return QuestionResponse của câu hỏi đã tạo
     */
    @Transactional
    public QuestionResponse createQuestion(QuestionRequest questionRequest) {
        // Tìm quiz theo ID
        Quiz quiz = quizRepository.findById(questionRequest.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz not found with id: " + questionRequest.getQuizId()));
        
        // Nếu không có orderNumber, tự động đặt orderNumber là max + 1
        if (questionRequest.getOrderNumber() == null) {
            List<Question> existingQuestions = questionRepository.findByQuizIdOrderByOrderNumber(quiz.getId());
            int maxOrderNumber = existingQuestions.isEmpty() ? 0 : 
                    existingQuestions.stream()
                    .mapToInt(Question::getOrderNumber)
                    .max()
                    .orElse(0);
            questionRequest.setOrderNumber(maxOrderNumber + 1);
        }
        
        // Tạo mới đối tượng Question
        Question question = Question.builder()
                .quiz(quiz)
                .content(questionRequest.getContent())
                .timeLimit(questionRequest.getTimeLimit())
                .points(questionRequest.getPoints())
                .orderNumber(questionRequest.getOrderNumber())
                .build();
        
        // Lưu vào database và lấy ID
        Question savedQuestion = questionRepository.save(question);
        
        // Lưu hình ảnh vào Cloudinary (nếu có)
        if (questionRequest.getImageFile() != null && !questionRequest.getImageFile().isEmpty()) {
            // Tải lên Cloudinary và lấy tên file
            String imageUrl = cloudinaryService.uploadQuestionImage(
                    questionRequest.getImageFile(),
                    questionRequest.getQuizId(),
                    savedQuestion.getId()
            );
            
            // Cập nhật URL hình ảnh trong câu hỏi
            savedQuestion.setImageUrl(imageUrl);
            
            // Lưu lại câu hỏi với URL hình ảnh mới
            questionRepository.save(savedQuestion);
        }
        
        // Tạo và lưu các lựa chọn cho câu hỏi
        List<QuestionOption> savedOptions = new ArrayList<>();
        if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
            // Kiểm tra xem có ít nhất một lựa chọn đúng
            boolean hasCorrectOption = questionRequest.getOptions().stream()
                    .anyMatch(QuestionRequest.QuestionOptionRequest::getIsCorrect);
            
            if (!hasCorrectOption) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Question must have at least one correct option");
            }
            
            // Tạo và lưu các lựa chọn
            for (QuestionRequest.QuestionOptionRequest optionRequest : questionRequest.getOptions()) {
                QuestionOption option = QuestionOption.builder()
                        .question(savedQuestion)
                        .content(optionRequest.getContent())
                        .isCorrect(optionRequest.getIsCorrect())
                        .build();
                
                savedOptions.add(questionOptionRepository.save(option));
            }
        }
        
        // Cập nhật số lượng câu hỏi trong quiz
        quiz.setQuestionCount(quiz.getQuestionCount() + 1);
        quizRepository.save(quiz);
        
        // Trả về response
        return QuestionResponse.fromQuestionWithOptions(savedQuestion, savedOptions, cloudinaryService);
    }
    
    /**
     * Cập nhật thông tin câu hỏi
     * @param id ID của câu hỏi cần cập nhật
     * @param questionRequest Thông tin mới của câu hỏi
     * @return QuestionResponse của câu hỏi đã cập nhật
     */
    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest questionRequest) {
        // Tìm câu hỏi theo ID
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Question not found with id: " + id));
        
        // Kiểm tra xem quizId có bị thay đổi không
        if (!question.getQuiz().getId().equals(questionRequest.getQuizId())) {
            // Nếu có, tìm quiz mới
            Quiz newQuiz = quizRepository.findById(questionRequest.getQuizId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Quiz not found with id: " + questionRequest.getQuizId()));
            
            // Giảm số lượng câu hỏi trong quiz cũ
            Quiz oldQuiz = question.getQuiz();
            oldQuiz.setQuestionCount(oldQuiz.getQuestionCount() - 1);
            quizRepository.save(oldQuiz);
            
            // Tăng số lượng câu hỏi trong quiz mới
            newQuiz.setQuestionCount(newQuiz.getQuestionCount() + 1);
            quizRepository.save(newQuiz);
            
            // Cập nhật quiz cho câu hỏi
            question.setQuiz(newQuiz);
        }
        
        // Cập nhật thông tin câu hỏi
        question.setContent(questionRequest.getContent());
        question.setTimeLimit(questionRequest.getTimeLimit());
        question.setPoints(questionRequest.getPoints());
        
        // Cập nhật orderNumber nếu có thay đổi
        if (questionRequest.getOrderNumber() != null) {
            question.setOrderNumber(questionRequest.getOrderNumber());
        }
        
        // Nếu có file hình ảnh mới, tải lên Cloudinary và xóa file cũ
        if (questionRequest.getImageFile() != null && !questionRequest.getImageFile().isEmpty()) {
            // Lưu URL hình ảnh cũ để xóa sau khi upload thành công
            String oldImageUrl = question.getImageUrl();
            
            // Tải lên Cloudinary và lấy tên file mới
            String imageUrl = cloudinaryService.uploadQuestionImage(
                    questionRequest.getImageFile(),
                    question.getQuiz().getId(),
                    question.getId()
            );
            
            // Cập nhật URL hình ảnh trong câu hỏi
            question.setImageUrl(imageUrl);
            
            // Xóa hình ảnh cũ từ Cloudinary (nếu có)
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                cloudinaryService.deleteQuestionImage(oldImageUrl);
            }
        }
        
        // Lưu câu hỏi vào database
        Question updatedQuestion = questionRepository.save(question);
        
        // Xóa tất cả các lựa chọn cũ
        List<QuestionOption> oldOptions = questionOptionRepository.findByQuestionId(id);
        questionOptionRepository.deleteAll(oldOptions);
        
        // Tạo và lưu các lựa chọn mới
        List<QuestionOption> newOptions = new ArrayList<>();
        if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
            // Kiểm tra xem có ít nhất một lựa chọn đúng
            boolean hasCorrectOption = questionRequest.getOptions().stream()
                    .anyMatch(QuestionRequest.QuestionOptionRequest::getIsCorrect);
            
            if (!hasCorrectOption) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Question must have at least one correct option");
            }
            
            // Tạo và lưu các lựa chọn mới
            for (QuestionRequest.QuestionOptionRequest optionRequest : questionRequest.getOptions()) {
                QuestionOption option = QuestionOption.builder()
                        .question(updatedQuestion)
                        .content(optionRequest.getContent())
                        .isCorrect(optionRequest.getIsCorrect())
                        .build();
                
                newOptions.add(questionOptionRepository.save(option));
            }
        }
        
        // Trả về response
        return QuestionResponse.fromQuestionWithOptions(updatedQuestion, newOptions, cloudinaryService);
    }
    
    /**
     * Xóa câu hỏi
     * @param id ID của câu hỏi cần xóa
     */
    @Transactional
    public void deleteQuestion(Long id) {
        // Tìm câu hỏi theo ID
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Question not found with id: " + id));
        
        // Giảm số lượng câu hỏi trong quiz
        Quiz quiz = question.getQuiz();
        quiz.setQuestionCount(quiz.getQuestionCount() - 1);
        quizRepository.save(quiz);
        
        // Xóa hình ảnh từ Cloudinary nếu có
        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            cloudinaryService.deleteQuestionImage(question.getImageUrl());
        }
        
        // Xóa tất cả các lựa chọn của câu hỏi
        List<QuestionOption> options = questionOptionRepository.findByQuestionId(id);
        questionOptionRepository.deleteAll(options);
        
        // Xóa câu hỏi từ database
        questionRepository.deleteById(id);
    }
}