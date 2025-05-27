package com.huy.quizme_backend.service;

import com.huy.quizme_backend.dto.game.GameResultDTO.FinalPlayerRankingDTO;
import com.huy.quizme_backend.dto.game.LeaderboardDTO;
import com.huy.quizme_backend.dto.game.LeaderboardDTO.PlayerRankingDTO;
import com.huy.quizme_backend.dto.game.QuestionGameDTO;
import com.huy.quizme_backend.dto.game.QuestionGameDTO.QuestionOptionDTO;
import com.huy.quizme_backend.dto.game.QuestionResultDTO;
import com.huy.quizme_backend.dto.game.QuestionResultDTO.UserAnswerDTO;
import com.huy.quizme_backend.enity.*;
import com.huy.quizme_backend.enity.enums.QuestionType;
import com.huy.quizme_backend.repository.*;
import com.huy.quizme_backend.session.GameSession;
import com.huy.quizme_backend.session.ParticipantSession;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Xử lý tiến trình trò chơi: câu hỏi, câu trả lời, tính điểm, bảng xếp hạng
 */
@Service
@RequiredArgsConstructor
public class GameProgressService {
    private final QuizRepository quizRepository;
    private final RoomRepository RoomRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final GameResultService gameResultService;
    private final LocalStorageService localStorageService;

    /**
     * Tải đầy đủ thông tin Quiz và câu hỏi
     */
    public List<QuestionGameDTO> loadQuizAndPrepareQuestions(Long quizId) {
        // Tải Quiz từ cơ sở dữ liệu
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        // Khởi tạo danh sách QuestionGameDTO
        List<QuestionGameDTO> questionGameDTOs = new ArrayList<>();

        // Lặp qua từng câu hỏi và chuyển đổi thành QuestionGameDTO
        for (Question question : quiz.getQuestions()) {
            // Khởi tạo QuestionGameDTO và thiết lập thông tin
            QuestionGameDTO questionGameDTO = new QuestionGameDTO();
            questionGameDTO.setQuestionId(question.getId());
            questionGameDTO.setContent(question.getContent());
            questionGameDTO.setImageUrl(question.getImageUrl());
            questionGameDTO.setVideoUrl(question.getVideoUrl());
            questionGameDTO.setAudioUrl(question.getAudioUrl());
            questionGameDTO.setType(question.getType());
            questionGameDTO.setTimeLimit(question.getTimeLimit());
            questionGameDTO.setPoints(question.getPoints());
            questionGameDTO.setQuestionNumber(question.getOrderNumber());
            questionGameDTO.setTotalQuestions(quiz.getQuestions().size());

            // Lấy danh sách tùy chọn câu hỏi
            List<QuestionOptionDTO> optionDTOs = new ArrayList<>();

            // Lặp qua từng tùy chọn và chuyển đổi thành QuestionOptionDTO
            for (QuestionOption option : question.getOptions()) {
                QuestionOptionDTO optionDTO = new QuestionOptionDTO();
                optionDTO.setId(option.getId());
                optionDTO.setContent(option.getContent());
                optionDTOs.add(optionDTO);
            }

            // Thiết lập danh sách tùy chọn cho QuestionGameDTO
            questionGameDTO.setOptions(optionDTOs);

            // Thêm QuestionGameDTO vào danh sách
            questionGameDTOs.add(questionGameDTO);
        }

        return questionGameDTOs;
    }

    /**
     * Kiểm tra câu trả lời
     */
    public boolean validateAnswer(Question question, GamePlayerAnswer playerAnswer) {
        // Lấy type của câu hỏi
        QuestionType questionType = question.getType();

        // Lấy danh sách các tùy chọn đúng
        List<Long> correctOptions = question.getOptions().stream()
                .filter(QuestionOption::getIsCorrect)
                .map(QuestionOption::getId)
                .toList();

        // Kiểm tra nếu câu hỏi là trắc nghiệm
        switch (questionType) {
            case QUIZ:
                // Kiểm tra nếu câu trả lời đúng với câu hỏi trắc nghiệm
                return correctOptions.contains(playerAnswer.getSelectedOptionIds().getFirst());

            case TRUE_FALSE:
                // Kiểm tra nếu câu trả lời đúng với câu hỏi đúng/sai
                return correctOptions.contains(playerAnswer.getSelectedOptionIds().getFirst());

            case QUIZ_AUDIO:
                // Kiểm tra nếu câu trả lời đúng với câu hỏi trắc nghiệm âm thanh
                return correctOptions.contains(playerAnswer.getSelectedOptionIds().getFirst());

            case QUIZ_VIDEO:
                // Kiểm tra nếu câu trả lời đúng với câu hỏi trắc nghiệm video
                return correctOptions.contains(playerAnswer.getSelectedOptionIds().getFirst());

            case CHECKBOX:
                Set<Long> selected = new HashSet<>(playerAnswer.getSelectedOptionIds());
                Set<Long> correct = new HashSet<>(correctOptions);
                // Kiểm tra nếu câu trả lời đúng với câu hỏi checkbox
                return selected.equals(correct);

            case TYPE_ANSWER:
                throw new UnsupportedOperationException("Chưa triển khai kiểm tra câu trả lời cho câu hỏi tự luận");

            default:
                return false;
        }
    }

    /**
     * Tính điểm dựa trên độ chính xác và thời gian
     */
    public int calculateScore(Question question, GamePlayerAnswer playerAnswer, boolean isCorrect) {
        // Nếu câu trả lời sai thì trả về 0 điểm
        if (!isCorrect) {
            return 0;
        }

        // Lấy điểm cơ bản từ câu hỏi
        int basePoints = question.getPoints();

        // Lấy total time từ câu hỏi
        int totalTime = question.getTimeLimit() * 1000; // Chuyển đổi sang milliseconds

        // Lấy thời gian người chơi đã trả lời
        Double timeTaken = playerAnswer.getAnswerTime();

        // Tính thời gian còn lại
        double timeRemaining = Math.max(0, totalTime - timeTaken);

        // Tính hệ số thưởng thời gian
        double timeBonusFactor = (timeRemaining / totalTime) * 0.5;

        // Tính điểm dựa trên thời gian
        int score = (int) (basePoints + (basePoints * timeBonusFactor));

        // Xử lý điểm cho Checkbox
        if (question.getType() == QuestionType.CHECKBOX) {
            // Lấy số lượng tùy chọn đúng
            long correctSelectedOptions = playerAnswer.getSelectedOptionIds().stream()
                    .filter(id -> question.getOptions().stream()
                            .anyMatch(opt -> opt.getId().equals(id) && opt.getIsCorrect()))
                    .count();

            // Lấy số lượng tùy chọn sai
            long incorrectSelectedOptions = playerAnswer.getSelectedOptionIds().stream()
                    .filter(id -> question.getOptions().stream()
                            .anyMatch(opt -> opt.getId().equals(id) && !opt.getIsCorrect()))
                    .count();

            // Lấy tổng số tùy chọn đúng
            long totalCorrectOptions = question.getOptions().stream()
                    .filter(QuestionOption::getIsCorrect)
                    .count();

            // Tính điểm cho Checkbox
            if (correctSelectedOptions > 0) {
                double scoreRatio = (double) (correctSelectedOptions - incorrectSelectedOptions) / totalCorrectOptions;

                score = (int) (basePoints * Math.max(0, scoreRatio));
            } else {
                score = 0;
            }
        }

        // Trả về điểm đã tính
        return score;
    }

    /**
     * Tính kết quả cho một câu hỏi
     */
    public QuestionResultDTO calculateResults(GameSession session, Question currentQuestionEntity) {
        // Khởi tạo QuestionResultDTO
        QuestionResultDTO questionResultDTO = new QuestionResultDTO();
        questionResultDTO.setQuestionId(currentQuestionEntity.getId());
        questionResultDTO.setCorrectOptions(currentQuestionEntity.getOptions().stream()
                .filter(QuestionOption::getIsCorrect)
                .map(QuestionOption::getId)
                .collect(Collectors.toList()));
        questionResultDTO.setExplanation(currentQuestionEntity.getExplanation());
        questionResultDTO.setFunFact(currentQuestionEntity.getFunFact());

        // Tạo danh sách
        List<UserAnswerDTO> userAnswers = new ArrayList<>();

        // Lặp qua từng người chơi trong phiên
        for (ParticipantSession participant : session.getParticipants().values()) {
            // Lấy câu trả lời của người chơi
            GamePlayerAnswer playerAnswer = participant.getAnswers().get(currentQuestionEntity.getId());

            // Kiểm tra nếu người chơi đã trả lời câu hỏi này
            if (playerAnswer != null) {
                // Kiểm tra tính đúng sai của câu trả lời
                boolean isCorrect = validateAnswer(currentQuestionEntity, playerAnswer);

                // Tính điểm cho người chơi
                int score = calculateScore(currentQuestionEntity, playerAnswer, isCorrect);

                // Tạo UserAnswerDTO từ GamePlayerAnswer
                UserAnswerDTO userAnswerDTO = UserAnswerDTO.fromEntity(playerAnswer);
                userAnswerDTO.setUserId(participant.getUserId());
                userAnswerDTO.setIsCorrect(isCorrect);
                userAnswerDTO.setScore(score);
                userAnswerDTO.setTimeTaken(playerAnswer.getAnswerTime());

                // Thêm vào danh sách câu trả lời người dùng
                userAnswers.add(userAnswerDTO);

                // Cập nhật điểm số cho người chơi
                participant.setScore(participant.getScore() + score);
            }
        }

        // Thiết lập danh sách câu trả lời người dùng cho QuestionResultDTO
        questionResultDTO.setUserAnswer(userAnswers);

        // Tính toán tỷ lệ lựa chọn cho từng tùy chọn
        List<QuestionResultDTO.OptionStatDTO> optionStats = new ArrayList<>();

        // Lấy tổng số người chơi đã trả lời câu hỏi này
        int totalAnswers = session.getParticipants().size();
        for (QuestionOption option : currentQuestionEntity.getOptions()) {
            // Lấy số lượng người chơi đã chọn tùy chọn này
            long count = session.getParticipants().values().stream()
                    .filter(p -> p.getAnswers().containsKey(currentQuestionEntity.getId()))
                    .filter(p -> p.getAnswers().get(currentQuestionEntity.getId()).getSelectedOptionIds().contains(option.getId()))
                    .count();

            // Tính tỷ lệ phần trăm
            double percentage = totalAnswers > 0 ? (double) count / totalAnswers * 100 : 0.0;

            // Tạo OptionStatDTO và thêm vào danh sách
            QuestionResultDTO.OptionStatDTO optionStatDTO = QuestionResultDTO.OptionStatDTO.fromEntity(option, percentage);
            optionStats.add(optionStatDTO);
        }
        // Thiết lập danh sách thống kê tùy chọn cho QuestionResultDTO
        questionResultDTO.setOptionStats(optionStats);

        return questionResultDTO;
    }

    /**
     * Tạo bảng xếp hạng từ phiên chơi
     */
    public LeaderboardDTO generateLeaderboardDTO(GameSession session) {
        // Lấy danh sách người chơi từ phiên
        List<ParticipantSession> participants = new ArrayList<>(session.getParticipants().values());

        // Sắp xếp danh sách người chơi theo điểm số
        participants.sort(Comparator.comparingInt(ParticipantSession::getScore).reversed());

        // Tạo danh sách bảng xếp hạng
        List<PlayerRankingDTO> leaderboard = getPlayerRankingDTOS(participants);

        // Tạo LeaderboardDTO
        LeaderboardDTO leaderboardDTO = new LeaderboardDTO();
        leaderboardDTO.setRankings(leaderboard);

        // Trả về bảng xếp hạng
        return leaderboardDTO;
    }

    public List<FinalPlayerRankingDTO> generateFinalPlayerRankingDTO(GameSession session) {
        // Lấy danh sách người chơi từ phiên
        List<ParticipantSession> participants = new ArrayList<>(session.getParticipants().values());

        // Sắp xếp danh sách người chơi theo điểm số
        participants.sort(Comparator.comparingInt(ParticipantSession::getScore).reversed());

        // Tạo danh sách bảng xếp hạng
        List<PlayerRankingDTO> leaderboard = getPlayerRankingDTOS(participants);

        // Chuyển đổi sang FinalPlayerRankingDTO
        List<FinalPlayerRankingDTO> finalRankings = leaderboard.stream()
                .map(player -> new FinalPlayerRankingDTO(
                        player.getUserId(),
                        player.getUsername(),
                        player.getScore(),
                        player.getRank(),
                        player.getAvatar(),
                        null,
                        null // Correct answers not available in this context
                ))
                .collect(Collectors.toList());

        return finalRankings;
    }

    private List<PlayerRankingDTO> getPlayerRankingDTOS(List<ParticipantSession> participants) {
        List<PlayerRankingDTO> leaderboard = new ArrayList<>();

        // Lặp qua từng người chơi và tạo PlayerRankingDTO
        for (int i = 0; i < participants.size(); i++) {
            ParticipantSession participant = participants.get(i);

            // Lấy info người dùng
            Long userId = participant.getUserId();

            // Lấy avatar của người dùng từ repository
            String avatar = userRepository.findAvatarByUserId(userId).orElse(null);

            // Chuye
            String urlAvatar = localStorageService.getProfileImageUrl(avatar);

            // Tạo PlayerRankingDTO
            PlayerRankingDTO playerRankingDTO = new PlayerRankingDTO();
            playerRankingDTO.setUserId(participant.getUserId());
            playerRankingDTO.setUsername(participant.getUsername());
            playerRankingDTO.setScore(participant.getScore());
            playerRankingDTO.setRank(i + 1);
            playerRankingDTO.setAvatar(urlAvatar);

            leaderboard.add(playerRankingDTO);
        }
        return leaderboard;
    }

    /**
     * Hoàn thiện kết quả trò chơi
     */
    public GameResult finalizeResults(GameSession session) {
        // Lấy quit từ phiên
        Quiz quiz = quizRepository.findById(session.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        // Lấy phòng từ phiên
        Room room = RoomRepository.findById(session.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // Tạo GameResultDTO
        GameResult gameResult = new GameResult();
        gameResult.setQuiz(quiz);
        gameResult.setRoom(room);

        // Trả về kết quả trò chơi
        return gameResultService.saveGameResult(gameResult);
    }

    /**
     * Lưu kết quả trò chơi (phương thức cũ - deprecated)
     */
    @Deprecated
    public GameResult saveGameResults(GameSession session, GameResult gameResult) {
        // Thiết lập thời gian
        gameResult.setStartTime(session.getStartTime());
        gameResult.setEndTime(session.getEndTime());
        gameResult.setParticipantCount(session.getParticipants().size());
        gameResult.setQuestionCount(session.getQuestions().size());

        // Tính toán thống kê
        int totalScore = session.getParticipants().values().stream()
                .mapToInt(ParticipantSession::getScore)
                .sum();
        gameResult.setAvgScore((double) totalScore / session.getParticipants().size());

        gameResult.setHighestScore(session.getParticipants().values().stream()
                .mapToInt(ParticipantSession::getScore)
                .max().orElse(0));

        gameResult.setLowestScore(session.getParticipants().values().stream()
                .mapToInt(ParticipantSession::getScore)
                .min().orElse(0));

        // Tính completion rate (tỷ lệ hoàn thành)
        int totalAnswers = session.getParticipants().values().stream()
                .mapToInt(p -> p.getAnswers().size())
                .sum();
        int maxPossibleAnswers = session.getParticipants().size() * session.getQuestions().size();
        gameResult.setCompletionRate(maxPossibleAnswers > 0 ?
                (double) totalAnswers / maxPossibleAnswers : 0.0);

        // Lưu kết quả trò chơi
        return gameResultService.saveGameResult(gameResult);
    }
}
