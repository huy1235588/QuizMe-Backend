package com.huy.quizme_backend.session;

/**
 * Trạng thái của phiên chơi game.
 */
public enum GameStatus {
    WAITING,
    IN_PROGRESS,
    QUESTION_END,
    NEXT_QUESTION,
    COMPLETED
}
