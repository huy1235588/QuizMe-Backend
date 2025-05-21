package com.huy.quizme_backend.session;

/**
 * Trạng thái kết nối của người chơi trong phiên game.
 */
public enum ConnectionStatus {
    ACTIVE,
    DISCONNECTED,
    TIMED_OUT
}
