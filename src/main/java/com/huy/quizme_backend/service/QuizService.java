package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.QuizRequest;
import com.huy.quizme_backend.dto.response.PageResponse;
import com.huy.quizme_backend.dto.response.QuizResponse;
import com.huy.quizme_backend.enity.Category;
import com.huy.quizme_backend.enity.enums.Difficulty;
import com.huy.quizme_backend.enity.Quiz;
import com.huy.quizme_backend.enity.Question;
import com.huy.quizme_backend.enity.QuestionOption;
import com.huy.quizme_backend.enity.enums.Role;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.repository.CategoryRepository;
import com.huy.quizme_backend.repository.QuizRepository;
import com.huy.quizme_backend.repository.UserRepository;
import com.huy.quizme_backend.repository.QuestionRepository;
import com.huy.quizme_backend.repository.QuestionOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocalStorageService localStorageService;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    /**
     * Lấy danh sách tất cả các quiz
     *
     * @return Danh sách các QuizResponse
     */
    public List<QuizResponse> getAllQuizzes() {
        return quizRepository.findAll().
                stream()
                .map(quiz -> QuizResponse.fromQuiz(quiz, localStorageService))
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
        return QuizResponse.fromQuiz(quiz, localStorageService);
    }

    /**
     * Lấy danh sách các quiz công khai
     *
     * @return Danh sách các QuizResponse
     */
    public List<QuizResponse> getPublicQuizzes() {
        return quizRepository.findByIsPublicTrue()
                .stream()
                .map(quiz -> QuizResponse.fromQuiz(quiz, localStorageService))
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
                .map(quiz -> QuizResponse.fromQuiz(quiz, localStorageService))
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách các quiz theo phân trang và lọc
     *
     * @param page       Số trang (bắt đầu từ 0)
     * @param pageSize   Số lượng kết quả mỗi trang
     * @param categoryId ID của category cần lọc
     * @param search     Từ khóa tìm kiếm
     * @param difficulty Độ khó của quiz
     * @param sort       Cách sắp xếp kết quả
     * @param isPublic   Trạng thái công khai
     * @param tab        Tab hiện tại (newest, popular)
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
                case "newest" -> quizRepository.findNewestQuizzes(
                        tab, categoryId, difficulty, isPublic, search, pageable);
                case "popular" -> quizRepository.findPopularQuizzes(
                        tab, categoryId, difficulty, isPublic, search, pageable);
                default -> quizRepository.findQuizzesWithFilters(
                        categoryId, difficulty, isPublic, search, pageable);
            };
        } else {
            quizPage = quizRepository.findQuizzesWithFilters(
                    categoryId, difficulty, isPublic, search, pageable);
        }

        List<QuizResponse> quizResponses = quizPage.getContent()
                .stream()
                .map(quiz -> QuizResponse.fromQuiz(quiz, localStorageService))
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

    /**
     * Tạo mới quiz
     *
     * @param quizRequest Thông tin quiz cần tạo
     * @param creatorId   ID của người tạo
     * @return QuizResponse của quiz đã tạo
     */
    @Transactional
    public QuizResponse createQuiz(QuizRequest quizRequest, Long creatorId) {
        // Tìm người tạo
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Creator not found with id: " + creatorId));

        // Tìm các category nếu có
        List<Category> categories = new ArrayList<>();
        if (quizRequest.getCategoryIds() != null) {
            categories = categoryRepository.findAllById(quizRequest.getCategoryIds());

            // Kiểm tra xem tất cả các category có tồn tại không
            if (categories.size() != quizRequest.getCategoryIds().size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Some categories not found");
            }
        }

        // Tạo mới đối tượng Quiz
        Quiz quiz = Quiz.builder()
                .title(quizRequest.getTitle())
                .description(quizRequest.getDescription())
                .categories(categories)
                .creator(creator)
                .difficulty(quizRequest.getDifficulty())
                .isPublic(quizRequest.getIsPublic())
                .playCount(0)
                .questionCount(0)
                .favoriteCount(0)
                .build();

        // Lưu vào database và lấy ID
        Quiz savedQuiz = quizRepository.save(quiz);

        // Lưu thumbnail vào local storage (nếu có)
        if (quizRequest.getThumbnailFile() != null && !quizRequest.getThumbnailFile().isEmpty()) {
            // Tải lên local storage và lấy tên file
            String thumbnailUrl = localStorageService.uploadQuizThumbnail(
                    quizRequest.getThumbnailFile(),
                    savedQuiz.getId()
            );

            // Cập nhật URL thumbnail trong quiz
            savedQuiz.setQuizThumbnails(thumbnailUrl);

            // Lưu lại quiz với URL thumbnail mới
            quizRepository.save(savedQuiz);
        }

        // Tạo câu hỏi nếu có
        if (quizRequest.getQuestions() != null && !quizRequest.getQuestions().isEmpty()) {
            createQuestionsForQuiz(savedQuiz, quizRequest.getQuestions());
        }

        // Cập nhật số lượng quiz trong category
        for (Category category : categories) {
            category.setQuizCount(category.getQuizCount() + 1);
            categoryRepository.save(category);
        }

        // Trả về response
        return QuizResponse.fromQuiz(savedQuiz, localStorageService);
    }

    /**
     * Tạo câu hỏi cho quiz
     *
     * @param quiz      Quiz đã được tạo
     * @param questions Danh sách câu hỏi cần tạo
     */
    private void createQuestionsForQuiz(Quiz quiz, List<QuizRequest.QuizQuestionRequest> questions) {
        List<Question> createdQuestions = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            QuizRequest.QuizQuestionRequest questionRequest = questions.get(i);

            // Tạo câu hỏi
            Question question = Question.builder()
                    .quiz(quiz)
                    .content(questionRequest.getContent())
                    .timeLimit(questionRequest.getTimeLimit())
                    .points(questionRequest.getPoints())
                    .orderNumber(questionRequest.getOrderNumber() != null ? questionRequest.getOrderNumber() : i + 1)
                    .type(questionRequest.getType())
                    .build();

            // Lưu câu hỏi
            Question savedQuestion = questionRepository.save(question);

            // Xử lý upload file ảnh/âm thanh nếu có
            handleQuestionMediaUploads(savedQuestion, questionRequest);

            // Tạo các lựa chọn cho câu hỏi
            createOptionsForQuestion(savedQuestion, questionRequest.getOptions());

            createdQuestions.add(savedQuestion);
        }

        // Cập nhật số lượng câu hỏi trong quiz
        quiz.setQuestionCount(createdQuestions.size());
        quizRepository.save(quiz);
    }

    /**
     * Xử lý upload file media cho câu hỏi
     *
     * @param question        Câu hỏi đã được tạo
     * @param questionRequest Request chứa thông tin file
     */
    private void handleQuestionMediaUploads(Question question, QuizRequest.QuizQuestionRequest questionRequest) {
        boolean needUpdate = false;

        // Upload file ảnh nếu có
        if (questionRequest.getImageFile() != null && !questionRequest.getImageFile().isEmpty()) {
            String imageUrl = localStorageService.uploadQuestionImage(
                    questionRequest.getImageFile(),
                    question.getQuiz().getId(),
                    question.getId()
            );
            question.setImageUrl(imageUrl);
            needUpdate = true;
        }

        // Upload file âm thanh nếu có
        if (questionRequest.getAudioFile() != null && !questionRequest.getAudioFile().isEmpty()) {
            String audioUrl = localStorageService.uploadQuestionAudio(
                    questionRequest.getAudioFile(),
                    question.getQuiz().getId(),
                    question.getId()
            );
            question.setAudioUrl(audioUrl);
            needUpdate = true;
        }

        // Lưu lại câu hỏi nếu có cập nhật media
        if (needUpdate) {
            questionRepository.save(question);
        }
    }

    /**
     * Tạo các lựa chọn cho câu hỏi
     *
     * @param question Câu hỏi đã được tạo
     * @param options  Danh sách lựa chọn
     */
    private void createOptionsForQuestion(Question question, List<QuizRequest.QuizQuestionRequest.QuestionOptionRequest> options) {
        List<QuestionOption> questionOptions = new ArrayList<>();

        for (QuizRequest.QuizQuestionRequest.QuestionOptionRequest optionRequest : options) {
            QuestionOption option = QuestionOption.builder()
                    .question(question)
                    .content(optionRequest.getContent())
                    .isCorrect(optionRequest.getIsCorrect())
                    .build();

            questionOptions.add(option);
        }

        // Lưu tất cả lựa chọn
        questionOptionRepository.saveAll(questionOptions);
    }

    /**
     * Cập nhật thông tin quiz
     *
     * @param id            ID của quiz cần cập nhật
     * @param quizRequest   Thông tin mới của quiz
     * @param currentUserId ID của người dùng hiện tại
     * @return QuizResponse của quiz đã cập nhật
     */
    @Transactional
    public QuizResponse updateQuiz(Long id, QuizRequest quizRequest, Long currentUserId) {
        // Tìm quiz theo ID
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz not found with id: " + id));

        // Kiểm tra quyền truy cập (chỉ người tạo quiz hoặc admin mới có quyền cập nhật)
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + currentUserId));

        if (!quiz.getCreator().getId().equals(currentUserId) && currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to update this quiz");
        }

        // Xử lý cập nhật categories
        List<Category> newCategories = new ArrayList<>();
        List<Category> oldCategories = new ArrayList<>(quiz.getCategories());

        if (quizRequest.getCategoryIds() != null) {
            newCategories = categoryRepository.findAllById(quizRequest.getCategoryIds());

            // Kiểm tra xem có category nào không tìm thấy không
            if (!quizRequest.getCategoryIds().isEmpty() &&
                    newCategories.size() != quizRequest.getCategoryIds().size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "One or more categories not found");
            }

            // Giảm quiz count của các category cũ không còn trong danh sách mới
            for (Category oldCategory : oldCategories) {
                if (!newCategories.contains(oldCategory)) {
                    oldCategory.setQuizCount(oldCategory.getQuizCount() - 1);
                    categoryRepository.save(oldCategory);
                }
            }

            // Tăng quiz count của các category mới chưa có trong danh sách cũ
            for (Category newCategory : newCategories) {
                if (!oldCategories.contains(newCategory)) {
                    newCategory.setQuizCount(newCategory.getQuizCount() + 1);
                    categoryRepository.save(newCategory);
                }
            }

            // Cập nhật danh sách categories trong quiz
            quiz.setCategories(newCategories);
        }

        // Nếu có file thumbnail mới, tải lên Cloudinary và xóa file cũ
        if (quizRequest.getThumbnailFile() != null && !quizRequest.getThumbnailFile().isEmpty()) {
            // Lưu URL thumbnail cũ để xóa sau khi upload thành công
            String oldThumbnailUrl = quiz.getQuizThumbnails();

            // Tải lên Cloudinary và lấy tên file mới
            String thumbnailUrl = localStorageService.uploadQuizThumbnail(
                    quizRequest.getThumbnailFile(),
                    quiz.getId()
            );

            // Cập nhật URL thumbnail trong quiz
            quiz.setQuizThumbnails(thumbnailUrl);

            // Xóa thumbnail cũ từ Cloudinary (nếu có)
            if (oldThumbnailUrl != null && !oldThumbnailUrl.isEmpty()) {
                localStorageService.deleteQuizThumbnail(oldThumbnailUrl);
            }
        }

        // Cập nhật thông tin quiz
        quiz.setTitle(quizRequest.getTitle());
        quiz.setDescription(quizRequest.getDescription());
        quiz.setDifficulty(quizRequest.getDifficulty());
        quiz.setIsPublic(quizRequest.getIsPublic());

        // Lưu vào database
        Quiz updatedQuiz = quizRepository.save(quiz);

        // Xử lý cập nhật câu hỏi nếu có trong request
        if (quizRequest.getQuestions() != null) {
            updateQuestionsForQuiz(updatedQuiz, quizRequest.getQuestions());
        }

        // Trả về response
        return QuizResponse.fromQuiz(updatedQuiz, localStorageService);
    }

    /**
     * Cập nhật danh sách câu hỏi cho quiz
     * Xóa tất cả câu hỏi cũ và tạo lại từ đầu
     *
     * @param quiz      Quiz cần cập nhật câu hỏi
     * @param questions Danh sách câu hỏi mới
     */
    private void updateQuestionsForQuiz(Quiz quiz, List<QuizRequest.QuizQuestionRequest> questions) {
        // Xóa tất cả câu hỏi cũ và media files
        deleteAllQuestionsAndMedia(quiz);

        // Tạo lại câu hỏi từ danh sách mới
        if (!questions.isEmpty()) {
            createQuestionsForQuiz(quiz, questions);
        } else {
            // Nếu không có câu hỏi mới, cập nhật questionCount = 0
            quiz.setQuestionCount(0);
            quizRepository.save(quiz);
        }
    }

    /**
     * Xóa tất cả câu hỏi và media files liên quan của quiz
     *
     * @param quiz Quiz cần xóa câu hỏi
     */
    private void deleteAllQuestionsAndMedia(Quiz quiz) {
        // Lấy tất cả câu hỏi của quiz
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderNumber(quiz.getId());

        for (Question question : questions) {
            // Xóa các file media của câu hỏi
            deleteQuestionMediaFiles(question);

            // Xóa tất cả options của câu hỏi (cascade delete sẽ xử lý)
            questionOptionRepository.deleteByQuestionId(question.getId());
        }

        // Xóa tất cả câu hỏi của quiz (cascade delete sẽ xóa options)
        questionRepository.deleteByQuizId(quiz.getId());
    }

    /**
     * Xóa các file media của câu hỏi
     *
     * @param question Câu hỏi cần xóa media files
     */
    private void deleteQuestionMediaFiles(Question question) {
        // Xóa file ảnh nếu có
        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            localStorageService.deleteQuestionImage(question.getImageUrl());
        }

        // Xóa file âm thanh nếu có
        if (question.getAudioUrl() != null && !question.getAudioUrl().isEmpty()) {
            localStorageService.deleteQuestionAudio(question.getAudioUrl());
        }
    }

    /**
     * Xóa quiz
     *
     * @param id            ID của quiz cần xóa
     * @param currentUserId ID của người dùng hiện tại
     */
    @Transactional
    public void deleteQuiz(Long id, Long currentUserId) {
        // Tìm quiz theo ID
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quiz not found with id: " + id));

        // Kiểm tra quyền truy cập (chỉ người tạo quiz hoặc admin mới có quyền xóa)
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + currentUserId));

        if (!quiz.getCreator().getId().equals(currentUserId) && currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to delete this quiz");
        }

        // Cập nhật số lượng quiz trong các categories
        if (quiz.getCategories() != null) {
            for (Category category : quiz.getCategories()) {
                category.setQuizCount(category.getQuizCount() - 1);
                categoryRepository.save(category);
            }
        }

        // Xóa tất cả câu hỏi và media files liên quan
        deleteAllQuestionsAndMedia(quiz);

        // Xóa thumbnail từ Cloudinary nếu có
        if (quiz.getQuizThumbnails() != null && !quiz.getQuizThumbnails().isEmpty()) {
            localStorageService.deleteQuizThumbnail(quiz.getQuizThumbnails());
        }

        // Xóa quiz từ database
        quizRepository.deleteById(id);
    }
}