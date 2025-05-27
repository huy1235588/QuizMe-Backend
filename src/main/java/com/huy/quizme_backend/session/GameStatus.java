package com.huy.quizme_backend.session;

/**
 * Trạng thái của phiên chơi game.
 */
public enum GameStatus {
    WAITING,
    IN_PROGRESS,
    QUESTION_END,
    SHOWING_RESULTS,    // Hiển thị kết quả câu hỏi
    SHOWING_LEADERBOARD, // Hiển thị bảng xếp hạng
    NEXT_QUESTION,
    COMPLETED
}
