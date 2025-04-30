package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.QuestionRequest;
import com.huy.quizme_backend.dto.response.QuestionResponse;
import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.QuestionOption;
import com.huy.quizme_backend.enity.Quiz;
import com.huy.quizme_backend.repository.QuestionRepository;
import com.huy.quizme_backend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service xử lý các thao tác liên quan đến câu hỏi
 */
@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final CloudinaryService cloudinaryService;
    private final QuestionOptionService questionOptionService;

    /**
     * Lấy danh sách tất cả các câu hỏi
     *
     * @return Danh sách các QuestionResponse
     */
    public List<QuestionResponse> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        return convertQuestionsToResponses(questions);
    }

    /**
     * Lấy câu hỏi theo ID
     *
     * @param id ID của câu hỏi
     * @return QuestionResponse
     */
    public QuestionResponse getQuestionById(Long id) {
        Question question = findQuestionById(id);
        List<QuestionOption> options = questionOptionService.getOptionsByQuestionId(id);
        return QuestionResponse.fromQuestionWithOptions(question, options, cloudinaryService);
    }

    /**
     * Lấy danh sách các câu hỏi theo quiz ID
     *
     * @param quizId ID của quiz
     * @return Danh sách các QuestionResponse
     */
    public List<QuestionResponse> getQuestionsByQuizId(Long quizId) {
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderNumber(quizId);

        if (questions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No questions found for quiz id: " + quizId);
        }

        return convertQuestionsToResponses(questions);
    }

    /**
     * Tạo mới câu hỏi
     *
     * @param questionRequest thông tin câu hỏi cần tạo
     * @return QuestionResponse của câu hỏi đã tạo
     */
    @Transactional
    public QuestionResponse createQuestion(QuestionRequest questionRequest) {
        Quiz quiz = findQuizById(questionRequest.getQuizId());

        // Nếu không có số thứ tự, tự động gán số thứ tự tiếp theo
        if (questionRequest.getOrderNumber() == null) {
            questionRequest.setOrderNumber(getNextOrderNumber(quiz.getId()));
        }

        Question question = buildQuestionFromRequest(questionRequest, quiz);
        Question savedQuestion = questionRepository.save(question);

        // Xử lý upload file hình ảnh và âm thanh
        handleMediaUploads(savedQuestion, questionRequest.getImageFile(), questionRequest.getAudioFile());

        // Tạo các lựa chọn cho câu hỏi
        List<QuestionOption> savedOptions = questionOptionService.createOptionsForQuestion(
                savedQuestion, questionRequest.getOptions());

        // Cập nhật số lượng câu hỏi trong quiz
        updateQuizQuestionCount(quiz, 1);

        return QuestionResponse.fromQuestionWithOptions(savedQuestion, savedOptions, cloudinaryService);
    }

    /**
     * Cập nhật thông tin câu hỏi
     *
     * @param id              ID của câu hỏi cần cập nhật
     * @param questionRequest Thông tin mới của câu hỏi
     * @return QuestionResponse của câu hỏi đã cập nhật
     */
    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest questionRequest) {
        Question question = findQuestionById(id);
        Quiz oldQuiz = question.getQuiz();

        // Kiểm tra nếu câu hỏi được chuyển sang quiz khác
        if (!oldQuiz.getId().equals(questionRequest.getQuizId())) {
            Quiz newQuiz = findQuizById(questionRequest.getQuizId());
            updateQuizQuestionCount(oldQuiz, -1); // Giảm số câu hỏi ở quiz cũ
            updateQuizQuestionCount(newQuiz, 1);  // Tăng số câu hỏi ở quiz mới
            question.setQuiz(newQuiz);
        }

        // Cập nhật các trường thông tin cơ bản
        updateQuestionFields(question, questionRequest);

        // Lưu URL cũ để xóa nếu cần thiết
        String oldImageUrl = question.getImageUrl();
        String oldAudioUrl = question.getAudioUrl();

        // Xử lý cập nhật file media
        handleMediaUpdates(question, questionRequest.getImageFile(), questionRequest.getAudioFile(),
                oldImageUrl, oldAudioUrl);

        Question updatedQuestion = questionRepository.save(question);

        // Cập nhật các lựa chọn cho câu hỏi
        List<QuestionOption> newOptions = questionOptionService.updateOptionsForQuestion(
                updatedQuestion, questionRequest.getOptions());

        return QuestionResponse.fromQuestionWithOptions(updatedQuestion, newOptions, cloudinaryService);
    }

    /**
     * Xóa câu hỏi
     *
     * @param id ID của câu hỏi cần xóa
     */
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = findQuestionById(id);

        // Giảm số lượng câu hỏi trong quiz
        updateQuizQuestionCount(question.getQuiz(), -1);
        
        // Xóa các file media
        deleteQuestionMedia(question);
        
        // Xóa các lựa chọn
        questionOptionService.deleteAllOptionsForQuestion(id);
        
        // Xóa câu hỏi
        questionRepository.deleteById(id);
    }

    // Các phương thức hỗ trợ

    /**
     * Chuyển đổi danh sách câu hỏi thành danh sách QuestionResponse
     * 
     * @param questions Danh sách câu hỏi cần chuyển đổi
     * @return Danh sách QuestionResponse
     */
    private List<QuestionResponse> convertQuestionsToResponses(List<Question> questions) {
        if (questions.isEmpty()) {
            return List.of();
        }

        // Lấy danh sách ID của các câu hỏi
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());

        // Lấy tất cả các lựa chọn cho các câu hỏi
        List<QuestionOption> allOptions = questionOptionService.getOptionsByQuestionIds(questionIds);
        
        // Nhóm các lựa chọn theo ID câu hỏi
        Map<Long, List<QuestionOption>> optionsByQuestionId = questionOptionService.groupOptionsByQuestionId(allOptions);

        // Chuyển đổi từng câu hỏi thành QuestionResponse
        return questions.stream()
                .map(question -> {
                    List<QuestionOption> options = optionsByQuestionId.getOrDefault(question.getId(), List.of());
                    return QuestionResponse.fromQuestionWithOptions(question, options, cloudinaryService);
                })
                .collect(Collectors.toList());
    }

    /**
     * Tìm câu hỏi theo ID
     * 
     * @param id ID của câu hỏi
     * @return Đối tượng Question
     * @throws ResponseStatusException nếu không tìm thấy câu hỏi
     */
    private Question findQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Question not found with id: " + id));
    }

    /**
     * Tìm quiz theo ID
     * 
     * @param id ID của quiz
     * @return Đối tượng Quiz
     * @throws ResponseStatusException nếu không tìm thấy quiz
     */
    private Quiz findQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz not found with id: " + id));
    }

    /**
     * Lấy số thứ tự tiếp theo cho câu hỏi mới trong quiz
     * 
     * @param quizId ID của quiz
     * @return Số thứ tự tiếp theo
     */
    private int getNextOrderNumber(Long quizId) {
        List<Question> existingQuestions = questionRepository.findByQuizIdOrderByOrderNumber(quizId);
        return existingQuestions.isEmpty() ? 1 :
                existingQuestions.stream()
                        .mapToInt(Question::getOrderNumber)
                        .max()
                        .orElse(0) + 1;
    }

    /**
     * Xây dựng đối tượng Question từ QuestionRequest
     * 
     * @param request Thông tin yêu cầu
     * @param quiz Quiz chứa câu hỏi
     * @return Đối tượng Question đã xây dựng
     */
    private Question buildQuestionFromRequest(QuestionRequest request, Quiz quiz) {
        return Question.builder()
                .quiz(quiz)
                .content(request.getContent())
                .timeLimit(request.getTimeLimit())
                .points(request.getPoints())
                .orderNumber(request.getOrderNumber())
                .build();
    }

    /**
     * Cập nhật các trường thông tin của câu hỏi
     * 
     * @param question Câu hỏi cần cập nhật
     * @param request Thông tin yêu cầu cập nhật
     */
    private void updateQuestionFields(Question question, QuestionRequest request) {
        question.setContent(request.getContent());
        question.setTimeLimit(request.getTimeLimit());
        question.setPoints(request.getPoints());

        if (request.getOrderNumber() != null) {
            question.setOrderNumber(request.getOrderNumber());
        }
    }

    /**
     * Cập nhật số lượng câu hỏi trong quiz
     * 
     * @param quiz Quiz cần cập nhật
     * @param delta Giá trị thay đổi (tăng hoặc giảm)
     */
    private void updateQuizQuestionCount(Quiz quiz, int delta) {
        quiz.setQuestionCount(quiz.getQuestionCount() + delta);
        quizRepository.save(quiz);
    }

    /**
     * Xử lý tải lên file media cho câu hỏi mới
     * 
     * @param question Câu hỏi
     * @param imageFile File hình ảnh
     * @param audioFile File âm thanh
     */
    private void handleMediaUploads(Question question, MultipartFile imageFile, MultipartFile audioFile) {
        // Xử lý upload hình ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadQuestionImage(
                    imageFile, question.getQuiz().getId(), question.getId());
            question.setImageUrl(imageUrl);
            questionRepository.save(question);
        }

        // Xử lý upload âm thanh
        if (audioFile != null && !audioFile.isEmpty()) {
            String audioUrl = cloudinaryService.uploadQuestionAudio(
                    audioFile, question.getQuiz().getId(), question.getId());
            question.setAudioUrl(audioUrl);
            questionRepository.save(question);
        }
    }

    /**
     * Xử lý cập nhật file media cho câu hỏi đã tồn tại
     * 
     * @param question Câu hỏi
     * @param imageFile File hình ảnh mới
     * @param audioFile File âm thanh mới
     * @param oldImageUrl URL hình ảnh cũ
     * @param oldAudioUrl URL âm thanh cũ
     */
    private void handleMediaUpdates(Question question, MultipartFile imageFile, MultipartFile audioFile,
                                    String oldImageUrl, String oldAudioUrl) {
        // Xử lý cập nhật hình ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadQuestionImage(
                    imageFile, question.getQuiz().getId(), question.getId());
            question.setImageUrl(imageUrl);

            // Xóa hình ảnh cũ nếu có
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                cloudinaryService.deleteQuestionImage(oldImageUrl);
            }
        }

        // Xử lý cập nhật âm thanh
        if (audioFile != null && !audioFile.isEmpty()) {
            String audioUrl = cloudinaryService.uploadQuestionAudio(
                    audioFile, question.getQuiz().getId(), question.getId());
            question.setAudioUrl(audioUrl);

            // Xóa âm thanh cũ nếu có
            if (oldAudioUrl != null && !oldAudioUrl.isEmpty()) {
                cloudinaryService.deleteQuestionAudio(oldAudioUrl);
            }
        }
    }

    /**
     * Xóa các file media của câu hỏi
     * 
     * @param question Câu hỏi cần xóa media
     */
    private void deleteQuestionMedia(Question question) {
        // Xóa hình ảnh nếu có
        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            cloudinaryService.deleteQuestionImage(question.getImageUrl());
        }

        // Xóa âm thanh nếu có
        if (question.getAudioUrl() != null && !question.getAudioUrl().isEmpty()) {
            cloudinaryService.deleteQuestionAudio(question.getAudioUrl());
        }
    }
}