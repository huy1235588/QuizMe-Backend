-- *****************************************************************************
-- I. KHỞI TẠO DATABASE
-- *****************************************************************************

-- Tạo database nếu chưa tồn tại, sử dụng utf8mb4 để hỗ trợ tiếng Việt và emoji
CREATE DATABASE IF NOT EXISTS quizme_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Sử dụng database vừa tạo
USE quizme_db;

-- *****************************************************************************
-- II. TẠO CÁC BẢNG
-- *****************************************************************************

-- Bảng user
CREATE TABLE IF NOT EXISTS user
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    profile_image VARCHAR(255),
    created_at    TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login    TIMESTAMP    NULL      DEFAULT NULL,
    role          ENUM ('ADMIN', 'USER') DEFAULT 'USER',
    is_active     BOOLEAN                DEFAULT TRUE,

    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- Bảng user_profile
CREATE TABLE IF NOT EXISTS user_profile
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    date_of_birth    DATE,
    city             VARCHAR(100),
    phone_number     VARCHAR(20),
    total_score      INT DEFAULT 0,
    quizzes_played   INT DEFAULT 0,
    quizzes_created  INT DEFAULT 0,
    total_quiz_plays INT DEFAULT 0,

    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_id (user_id),
    INDEX idx_total_quiz_plays (total_quiz_plays)
);

-- Bảng achievement
CREATE TABLE IF NOT EXISTS achievement
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url    VARCHAR(255),

    achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- Bảng user_achievement
CREATE TABLE IF NOT EXISTS user_achievement
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,

    achieved_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES achievement (id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_achievement (user_id, achievement_id),
    INDEX idx_user_id (user_id),
    INDEX idx_achievement_id (achievement_id)
);

-- Bảng refresh_token
CREATE TABLE IF NOT EXISTS refresh_token
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(500) NOT NULL UNIQUE,
    jti        VARCHAR(100) NOT NULL UNIQUE, -- JWT ID để định danh token duy nhất
    issued_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,

    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_jti (jti),
    INDEX idx_expires_at (expires_at)
);

-- Bảng category
CREATE TABLE IF NOT EXISTS category
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL UNIQUE,
    description      TEXT,
    icon_url         VARCHAR(255),
    quiz_count       INT                   DEFAULT 0,
    total_play_count INT                   DEFAULT 0,
    is_active        BOOLEAN               DEFAULT TRUE,

    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_name (name),
    INDEX idx_quiz_count (quiz_count),
    INDEX idx_total_play_count (total_play_count)
);

-- Bảng quiz
CREATE TABLE IF NOT EXISTS quiz
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(100) NOT NULL,
    description     VARCHAR(1000),
    quiz_thumbnails VARCHAR(255),
    creator_id      BIGINT       NOT NULL,
    difficulty      ENUM ('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM',
    is_public       BOOLEAN                         DEFAULT TRUE,
    play_count      INT                             DEFAULT 0,
    question_count  INT                             DEFAULT 0,
    favorite_count  INT                             DEFAULT 0,

    created_at      TIMESTAMP                       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP                       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (creator_id) REFERENCES user (id) ON DELETE CASCADE,
    INDEX idx_creator_id (creator_id),
    INDEX idx_difficulty (difficulty),
    INDEX idx_is_public (is_public),
    INDEX idx_play_count (play_count)
);

-- Bảng liên kết giữa quiz và category
CREATE TABLE IF NOT EXISTS quiz_category
(
    quiz_id     BIGINT NOT NULL,
    category_id BIGINT NOT NULL,

    primary key (quiz_id, category_id),

    foreign key (quiz_id) REFERENCES quiz (id) ON DELETE CASCADE,
    foreign key (category_id) REFERENCES category (id) ON DELETE CASCADE,
    INDEX idx_quiz_id (quiz_id)
);

-- Bảng question
CREATE TABLE IF NOT EXISTS question
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id      BIGINT NOT NULL,
    content      TEXT   NOT NULL,
    image_url    VARCHAR(255),
    video_url    VARCHAR(255),
    audio_url    VARCHAR(255),
    fun_fact     TEXT, -- Nội dung thú vị về câu hỏi
    explanation  TEXT, -- Giải thích câu trả lời đúng
    time_limit   INT             DEFAULT 30,
    points       INT             DEFAULT 10,
    order_number INT    NOT NULL,
    type         ENUM (
        'QUIZ',
        'TRUE_FALSE',
        'TYPE_ANSWER',
        'QUIZ_AUDIO',
        'QUIZ_VIDEO',
        'CHECKBOX',
        'POLL'
        )               NOT NULL DEFAULT 'QUIZ',

    created_at   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (quiz_id) REFERENCES quiz (id) ON DELETE CASCADE,
    INDEX idx_quiz_id (quiz_id),
    INDEX idx_order_number (order_number)
);

-- Bảng question_option
CREATE TABLE IF NOT EXISTS question_option
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    content     TEXT   NOT NULL,
    is_correct  BOOLEAN   DEFAULT FALSE,

    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE,
    INDEX idx_question_id (question_id),
    INDEX idx_question_is_correct (question_id, is_correct)
);

-- Bảng autocomplete_hint
CREATE TABLE IF NOT EXISTS autocomplete_hint
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    content    TEXT NOT NULL,
    priority   INT       DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng liên kết giữa question và autocomplete_hint
CREATE TABLE IF NOT EXISTS question_autocomplete_hint
(
    question_id     BIGINT NOT NULL,
    autocomplete_id BIGINT NOT NULL,

    primary key (question_id, autocomplete_id),

    FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE,
    FOREIGN KEY (autocomplete_id) REFERENCES autocomplete_hint (id) ON DELETE CASCADE,
    INDEX idx_question_id (question_id),
    INDEX idx_autocomplete_id (autocomplete_id)
);

-- Bảng saved_quiz
CREATE TABLE IF NOT EXISTS saved_quiz
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT    NOT NULL,
    quiz_id     BIGINT    NOT NULL,
    saved_at    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    last_played TIMESTAMP NULL DEFAULT NULL,

    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quiz (id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_quiz (user_id, quiz_id),
    INDEX idx_user_id (user_id),
    INDEX idx_quiz_id (quiz_id)
);

-- Bảng quiz_attempt
CREATE TABLE IF NOT EXISTS quiz_attempt
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    quiz_id         BIGINT    NOT NULL,
    score           INT            DEFAULT 0,
    completion_time INT,
    started_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP NULL DEFAULT NULL,
    is_completed    BOOLEAN        DEFAULT FALSE,

    created_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE SET NULL,
    FOREIGN KEY (quiz_id) REFERENCES quiz (id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_quiz_id (quiz_id),
    INDEX idx_started_at (started_at),
    INDEX idx_is_completed (is_completed)
);

-- Tạo bảng user_answer
CREATE TABLE IF NOT EXISTS user_answer
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id  BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    option_id   BIGINT,
    is_correct  BOOLEAN   DEFAULT FALSE,
    time_taken  INT,

    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (attempt_id) REFERENCES quiz_attempt (id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES question_option (id) ON DELETE SET NULL,
    INDEX idx_attempt_id (attempt_id),
    INDEX idx_question_id (question_id),
    INDEX idx_is_correct (is_correct)
);

-- Bảng room
CREATE TABLE IF NOT EXISTS room
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(10)  NOT NULL UNIQUE,
    quiz_id     BIGINT       NOT NULL,
    host_id     BIGINT       NOT NULL,
    password    VARCHAR(255),
    is_public   BOOLEAN           DEFAULT TRUE,
    max_players INT               DEFAULT 10,
    status      ENUM (
        'waiting',
        'in_progress',
        'completed',
        'cancelled')              DEFAULT 'waiting',
    start_time  TIMESTAMP    NULL DEFAULT NULL,
    end_time    TIMESTAMP    NULL DEFAULT NULL,

    created_at  TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (quiz_id) REFERENCES quiz (id) ON DELETE CASCADE,
    FOREIGN KEY (host_id) REFERENCES user (id) ON DELETE CASCADE,
    index idx_quiz_id (quiz_id),
    INDEX idx_code (code),
    INDEX idx_host_id (host_id),
    INDEX idx_is_public_status (is_public, status),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Bảng room_participant
CREATE TABLE IF NOT EXISTS room_participant
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id    BIGINT    NOT NULL,
    user_id    BIGINT,
    score      INT            DEFAULT 0,
    is_host    BOOLEAN        DEFAULT FALSE,
    joined_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    left_at    TIMESTAMP NULL DEFAULT NULL,
    is_guest   BOOLEAN        DEFAULT FALSE,
    guest_name VARCHAR(50),

    FOREIGN KEY (room_id) REFERENCES room (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE SET NULL,
    INDEX idx_room_id (room_id),
    INDEX idx_user_id (user_id)
);


-- *****************************************************************************
-- III. CHÈN DỮ LIỆU MẪU
-- *****************************************************************************

-- === Users ===
INSERT INTO user (id, username, email, password, full_name, profile_image, role)
VALUES (1,
        'ha',
        'ha@gmail.com',
        '',
        'ha',
        'profile_1_1745400000.jpg',
        'ADMIN'),
       (2,
        'he',
        'he@gmail.com',
        '',
        'he',
        'profile_2_1745400000.jpg',
        'USER'),
       (3,
        'lehuy',
        'lehuy@gmail.com',
        '',
        'Lê Thiên Huy',
        'profile_3_1745400000.jpg',
        'USER')
ON DUPLICATE KEY
    UPDATE full_name = VALUES(full_name);
-- Nếu user đã tồn tại thì chỉ cập nhật tên

-- === User Profile ===
INSERT INTO user_profile (id,
                          user_id,
                          date_of_birth,
                          city,
                          phone_number,
                          total_score,
                          quizzes_played,
                          quizzes_created,
                          total_quiz_plays)
VALUES (1,
        1,
        '1990-01-01',
        'Việt Nam',
        '0123456789',
        100,
        5,
        2,
        0),

       (2,
        2,
        '1995-05-05',
        'Japan',
        '0987654321',
        50,
        3,
        0,
        0),

       (3,
        3,
        '1998-08-08',
        'USA',
        '1234567890',
        75,
        4,
        1,
        0)
ON DUPLICATE KEY UPDATE total_score      = VALUES(total_score),
                        quizzes_played   = VALUES(quizzes_played),
                        quizzes_created  = VALUES(quizzes_created),
                        total_quiz_plays = VALUES(total_quiz_plays);
-- Nếu user_profile đã tồn tại thì cập nhật các trường tương ứng

-- === Achievements ===
INSERT INTO achievement (id, user_id, title, description, icon_url)
VALUES (1,
        1,
        'Người chơi xuất sắc',
        'Đạt điểm cao nhất trong một quiz.',
        'achievement_1_1745400000.jpg'),
       (2,
        2,
        'Người tạo quiz xuất sắc',
        'Tạo quiz được nhiều người chơi nhất.',
        'achievement_2_1745400000.jpg'),
       (3,
        3,
        'Người chơi chăm chỉ',
        'Chơi nhiều quiz nhất trong tháng.',
        'achievement_3_1745400000.jpg')
ON DUPLICATE KEY UPDATE description = VALUES(description);
-- Nếu achievement đã tồn tại thì cập nhật description

-- === User Achievement ===
INSERT INTO user_achievement (id, user_id, achievement_id, achieved_at)
VALUES (1, 1, 1, NOW()),
       (2, 2, 2, NOW()),
       (3, 3, 3, NOW())
ON DUPLICATE KEY UPDATE achieved_at = VALUES(achieved_at);
-- Nếu user_achievement đã tồn tại thì cập nhật achieved_at

-- *****************************************************************************
-- KẾT THÚC SCRIPT
-- *****************************************************************************

SELECT 'Database initialization and sample data insertion completed.' AS Status;