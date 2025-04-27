package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.request.QuizRequest;
import com.huy.quizme_backend.dto.response.PageResponse;
import com.huy.quizme_backend.dto.response.QuizResponse;
import com.huy.quizme_backend.enity.Category;
import com.huy.quizme_backend.enity.Difficulty;
import com.huy.quizme_backend.enity.Quiz;
import com.huy.quizme_backend.enity.Role;
import com.huy.quizme_backend.enity.User;
import com.huy.quizme_backend.repository.CategoryRepository;
import com.huy.quizme_backend.repository.QuizRepository;
import com.huy.quizme_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
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

    /**
     * Tạo mới quiz
     *
     * @param quizRequest Thông tin quiz cần tạo
     * @param creatorId ID của người tạo
     * @return QuizResponse của quiz đã tạo
     */
    @Transactional
    public QuizResponse createQuiz(QuizRequest quizRequest, Long creatorId) {
        // Tìm người tạo
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Creator not found with id: " + creatorId));
        
        // Tìm category nếu có
        Category category = null;
        if (quizRequest.getCategoryId() != null) {
            category = categoryRepository.findById(quizRequest.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Category not found with id: " + quizRequest.getCategoryId()));
        }
        
        // Tạo mới đối tượng Quiz
        Quiz quiz = Quiz.builder()
                .title(quizRequest.getTitle())
                .description(quizRequest.getDescription())
                .category(category)
                .creator(creator)
                .difficulty(quizRequest.getDifficulty())
                .isPublic(quizRequest.getIsPublic())
                .playCount(0)
                .questionCount(0)
                .build();
        
        // Lưu vào database và lấy ID
        Quiz savedQuiz = quizRepository.save(quiz);
        
        // Lưu thumbnail vào Cloudinary (nếu có)
        if (quizRequest.getThumbnailFile() != null && !quizRequest.getThumbnailFile().isEmpty()) {
            // Tải lên Cloudinary và lấy tên file
            String thumbnailUrl = cloudinaryService.uploadQuizThumbnail(
                    quizRequest.getThumbnailFile(),
                    savedQuiz.getId()
            );
            
            // Cập nhật URL thumbnail trong quiz
            savedQuiz.setQuizThumbnails(thumbnailUrl);
            
            // Lưu lại quiz với URL thumbnail mới
            quizRepository.save(savedQuiz);
        }
        
        // Cập nhật số lượng quiz trong category
        if (category != null) {
            category.setQuizCount(category.getQuizCount() + 1);
            categoryRepository.save(category);
        }
        
        // Trả về response
        return QuizResponse.fromQuiz(savedQuiz, cloudinaryService);
    }
    
    /**
     * Cập nhật thông tin quiz
     *
     * @param id ID của quiz cần cập nhật
     * @param quizRequest Thông tin mới của quiz
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
        
        // Tìm category mới nếu có thay đổi
        Category newCategory = null;
        if (quizRequest.getCategoryId() != null) {
            newCategory = categoryRepository.findById(quizRequest.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Category not found with id: " + quizRequest.getCategoryId()));
        }
        
        // Nếu có thay đổi category, cập nhật số lượng quiz trong cả category cũ và mới
        if (quizRequest.getCategoryId() != null && 
                (quiz.getCategory() == null || !quiz.getCategory().getId().equals(quizRequest.getCategoryId()))) {
            
            // Giảm số lượng quiz trong category cũ (nếu có)
            if (quiz.getCategory() != null) {
                Category oldCategory = quiz.getCategory();
                oldCategory.setQuizCount(oldCategory.getQuizCount() - 1);
                categoryRepository.save(oldCategory);
            }
            
            // Tăng số lượng quiz trong category mới
            newCategory.setQuizCount(newCategory.getQuizCount() + 1);
            categoryRepository.save(newCategory);
        }
        
        // Nếu có file thumbnail mới, tải lên Cloudinary và xóa file cũ
        if (quizRequest.getThumbnailFile() != null && !quizRequest.getThumbnailFile().isEmpty()) {
            // Lưu URL thumbnail cũ để xóa sau khi upload thành công
            String oldThumbnailUrl = quiz.getQuizThumbnails();
            
            // Tải lên Cloudinary và lấy tên file mới
            String thumbnailUrl = cloudinaryService.uploadQuizThumbnail(
                    quizRequest.getThumbnailFile(),
                    quiz.getId()
            );
            
            // Cập nhật URL thumbnail trong quiz
            quiz.setQuizThumbnails(thumbnailUrl);
            
            // Xóa thumbnail cũ từ Cloudinary (nếu có)
            if (oldThumbnailUrl != null && !oldThumbnailUrl.isEmpty()) {
                cloudinaryService.deleteQuizThumbnail(oldThumbnailUrl);
            }
        }
        
        // Cập nhật thông tin quiz
        quiz.setTitle(quizRequest.getTitle());
        quiz.setDescription(quizRequest.getDescription());
        quiz.setCategory(newCategory != null ? newCategory : quiz.getCategory());
        quiz.setDifficulty(quizRequest.getDifficulty());
        quiz.setIsPublic(quizRequest.getIsPublic());
        
        // Lưu vào database
        Quiz updatedQuiz = quizRepository.save(quiz);
        
        // Trả về response
        return QuizResponse.fromQuiz(updatedQuiz, cloudinaryService);
    }
    
    /**
     * Xóa quiz
     * 
     * @param id ID của quiz cần xóa
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
        
        // Nếu quiz có category, cập nhật số lượng quiz trong category
        if (quiz.getCategory() != null) {
            Category category = quiz.getCategory();
            category.setQuizCount(category.getQuizCount() - 1);
            categoryRepository.save(category);
        }
        
        // Xóa thumbnail từ Cloudinary nếu có
        if (quiz.getQuizThumbnails() != null && !quiz.getQuizThumbnails().isEmpty()) {
            cloudinaryService.deleteQuizThumbnail(quiz.getQuizThumbnails());
        }
        
        // Xóa quiz từ database
        quizRepository.deleteById(id);
    }
}