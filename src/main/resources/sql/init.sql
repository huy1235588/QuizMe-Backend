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
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    total_score     INT DEFAULT 0,
    quizzes_played  INT DEFAULT 0,
    quizzes_created INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_id (user_id)
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

-- Bảng category
CREATE TABLE IF NOT EXISTS category
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url    VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_name (name)
);

-- Bảng quiz
CREATE TABLE IF NOT EXISTS quiz
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    category_id BIGINT,
    creator_id  BIGINT       NOT NULL,
    difficulty  ENUM ('easy', 'medium', 'hard') DEFAULT 'medium',
    is_public   BOOLEAN                         DEFAULT TRUE,
    created_at  TIMESTAMP                       DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP                       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE SET NULL,
    FOREIGN KEY (creator_id) REFERENCES user (id) ON DELETE CASCADE,
    INDEX idx_category_id (category_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_difficulty (difficulty),
    INDEX idx_is_public (is_public)
);

-- Bảng question
CREATE TABLE IF NOT EXISTS question
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id      BIGINT NOT NULL,
    content      TEXT   NOT NULL,
    image_url    VARCHAR(255),
    time_limit   INT       DEFAULT 30,
    points       INT       DEFAULT 10,
    order_number INT    NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
    INDEX idx_question_id (question_id)
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
    INDEX idx_question_id (question_id)
);

-- Bảng room
CREATE TABLE IF NOT EXISTS room
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100),
    code        VARCHAR(10) NOT NULL UNIQUE,
    quiz_id     BIGINT      NOT NULL,
    host_id     BIGINT      NOT NULL,
    max_players INT                                                       DEFAULT 10,
    status      ENUM ('waiting', 'in_progress', 'completed', 'cancelled') DEFAULT 'waiting',
    start_time  TIMESTAMP   NULL                                          DEFAULT NULL,
    end_time    TIMESTAMP   NULL                                          DEFAULT NULL,
    created_at  TIMESTAMP                                                 DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP                                                 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quiz (id) ON DELETE CASCADE,
    FOREIGN KEY (host_id) REFERENCES user (id) ON DELETE CASCADE,
    INDEX idx_code (code),
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
INSERT INTO user (username, email, password, full_name, profile_image, role)
VALUES ('admin1',
        'admin1@gmail.com',
        '',
        'Admin One',
        '',
        'admin'),
       ('user1',
        'user1@gmail.com',
        '',
        'User One',
        '',
        'user'),
       ('user2',
        'user2@gmail.com',
        '',
        'User Two',
        '',
        'user')
ON DUPLICATE KEY
    UPDATE full_name = VALUES(full_name);
-- Nếu user đã tồn tại thì chỉ cập nhật tên

-- === User Profile ===
INSERT INTO user_profile (user_id, total_score, quizzes_played, quizzes_created)
VALUES (1, 100, 5, 2),
       (2, 50, 3, 0),
       (3, 75, 4, 1)
ON DUPLICATE KEY UPDATE total_score     = VALUES(total_score),
                        quizzes_played  = VALUES(quizzes_played),
                        quizzes_created = VALUES(quizzes_created);
-- Nếu user_profile đã tồn tại thì cập nhật các trường tương ứng

-- === Achievements ===
INSERT INTO achievement (user_id, title, description, icon_url)
VALUES (1,
        'Người chơi xuất sắc',
        'Đạt điểm cao nhất trong một quiz.',
        ''),
       (2,
        'Người tạo quiz xuất sắc',
        'Tạo quiz được nhiều người chơi nhất.',
        ''),
       (3,
        'Người chơi chăm chỉ',
        'Chơi nhiều quiz nhất trong tháng.',
        '')
ON DUPLICATE KEY UPDATE description = VALUES(description);
-- Nếu achievement đã tồn tại thì cập nhật description

-- === User Achievement ===
INSERT INTO user_achievement (user_id, achievement_id, achieved_at)
VALUES (1, 1, NOW()),
       (2, 2, NOW()),
       (3, 3, NOW())
ON DUPLICATE KEY UPDATE achieved_at = VALUES(achieved_at);
-- Nếu user_achievement đã tồn tại thì cập nhật achieved_at

-- === Category ===
INSERT INTO category (name, description, icon_url)
VALUES ('Lịch sử Việt Nam',
        'Các câu hỏi về lịch sử dân tộc Việt Nam qua các thời kỳ.',
        ''),
       ('Địa lý Thế giới',
        'Kiến thức về địa lý các quốc gia, châu lục, sông ngòi, biển cả.',
        ''),
       ('Khoa học Tự nhiên',
        'Câu hỏi về Vật lý, Hóa học, Sinh học cơ bản.',
        ''),
       ('Văn học Việt Nam',
        'Tác giả, tác phẩm nổi tiếng trong nền văn học Việt Nam.',
        '')
ON DUPLICATE KEY UPDATE description = VALUES(description);
-- Nếu category đã tồn tại thì cập nhật description

-- === Quiz ===
INSERT INTO quiz (title, description, category_id, creator_id, difficulty, is_public)
VALUES ('Quiz Lịch sử Việt Nam',
        'Một quiz thú vị về lịch sử Việt Nam.',
        (SELECT id FROM category WHERE name = 'Lịch sử Việt Nam'),
        1,
        'medium',
        TRUE),
       ('Quiz Địa lý Thế giới',
        'Khám phá thế giới qua quiz địa lý.',
        (SELECT id FROM category WHERE name = 'Địa lý Thế giới'),
        2,
        'easy',
        TRUE),
       ('Quiz Khoa học Tự nhiên',
        'Kiến thức khoa học tự nhiên cơ bản.',
        (SELECT id FROM category WHERE name = 'Khoa học Tự nhiên'),
        3,
        'hard',
        FALSE)
ON DUPLICATE KEY UPDATE description = VALUES(description),
                        difficulty  = VALUES(difficulty),
                        is_public   = VALUES(is_public);
-- Nếu quiz đã tồn tại thì cập nhật description, difficulty và is_public

-- === Question ===
INSERT INTO question (quiz_id, content, image_url, time_limit, points, order_number)
VALUES (1,
        '2 + 2 = ?',
        NULL,
        15,
        5,
        1),
       (1,
        '5 * 3 = ?',
        NULL,
        20,
        10,
        2),
       (2,
        'Nước có công thức hóa học là gì?',
        NULL,
        30,
        15,
        1),
       (2,
        'Trái đất quay quanh mặt trời trong bao lâu?',
        NULL,
        30, 15,
        2),
       (3,
        'Ai là tổng thống đầu tiên của Hoa Kỳ?',
        NULL,
        45,
        20,
        1),
       (3,
        'Chiến tranh thế giới thứ hai kết thúc vào năm nào?',
        NULL,
        45,
        20,
        2)
ON DUPLICATE KEY UPDATE content      = VALUES(content),
                        time_limit   = VALUES(time_limit),
                        points       = VALUES(points),
                        order_number = VALUES(order_number);
-- Nếu question đã tồn tại thì cập nhật content, time_limit, points và order_number

-- === Question Option ===
INSERT INTO question_option (question_id, content, is_correct)
VALUES (1, '4', TRUE),
       (1, '3', FALSE),
       (1, '5', FALSE),
       (1, '2', FALSE),
       (2, '15', TRUE),
       (2, '10', FALSE),
       (2, '20', FALSE),
       (2, '25', FALSE),
       (3, 'H2O', TRUE),
       (3, 'CO2', FALSE),
       (3, 'O2', FALSE),
       (3, 'N2', FALSE),
       (4, '365 ngày', TRUE),
       (4, '24 giờ', FALSE),
       (4, '30 ngày', FALSE),
       (4, '12 tháng', FALSE),
       (5, 'George Washington', TRUE),
       (5, 'Abraham Lincoln', FALSE),
       (5, 'Thomas Jefferson', FALSE),
       (5, 'John Adams', FALSE),
       (6, '1945', TRUE),
       (6, '1939', FALSE),
       (6, '1918', FALSE),
       (6, '1941', FALSE)
ON DUPLICATE KEY UPDATE content    = VALUES(content),
                        is_correct = VALUES(is_correct);
-- Nếu option đã tồn tại thì cập nhật content và is_correct

-- === Saved Quiz ===
INSERT INTO saved_quiz (user_id, quiz_id, saved_at, last_played)
VALUES (1, 1, NOW(), NULL),
       (2, 2, NOW(), NULL),
       (3, 3, NOW(), NULL)
ON DUPLICATE KEY UPDATE last_played = VALUES(last_played),
                        saved_at    = VALUES(saved_at);
-- Nếu saved_quiz đã tồn tại thì cập nhật last_played và saved_at

-- === Quiz Attempt ===
INSERT INTO quiz_attempt (user_id, quiz_id, score, completion_time, started_at, completed_at, is_completed)
VALUES (1, 1, 15, 35, NOW(), NOW(), TRUE),
       (2, 2, 30, 60, NOW(), NOW(), TRUE),
       (3, 3, 40, 90, NOW(), NOW(), TRUE)
ON DUPLICATE KEY UPDATE score           = VALUES(score),
                        completion_time = VALUES(completion_time),
                        started_at      = VALUES(started_at),
                        completed_at    = VALUES(completed_at),
                        is_completed    = VALUES(is_completed);
-- Nếu quiz_attempt đã tồn tại thì cập nhật score, completion_time, started_at, completed_at và is_completed

-- === User Answer ===
INSERT INTO user_answer (attempt_id, question_id, option_id, is_correct, time_taken)
VALUES (1, 1, 1, TRUE, 10),
       (1, 2, 5, TRUE, 15),
       (2, 3, 9, TRUE, 20),
       (2, 4, 13, TRUE, 25),
       (3, 5, 17, TRUE, 30),
       (3, 6, 21, TRUE, 35)
ON DUPLICATE KEY UPDATE is_correct = VALUES(is_correct),
                        time_taken = VALUES(time_taken);
-- Nếu user_answer đã tồn tại thì cập nhật is_correct và time_taken

-- === Room ===
INSERT INTO room (name, code, quiz_id, host_id, max_players, status, start_time, end_time)
VALUES ('Phòng 1', 'ROOM1', 1, 1, 5, 'waiting', NULL, NULL),
       ('Phòng 2', 'ROOM2', 2, 2, 10, 'in_progress', NOW(), NULL),
       ('Phòng 3', 'ROOM3', 3, 3, 15, 'completed', NOW(), NOW())
ON DUPLICATE KEY UPDATE name        = VALUES(name),
                        quiz_id     = VALUES(quiz_id),
                        host_id     = VALUES(host_id),
                        max_players = VALUES(max_players),
                        status      = VALUES(status),
                        start_time  = VALUES(start_time),
                        end_time    = VALUES(end_time);
-- Nếu room đã tồn tại thì cập nhật name, quiz_id, host_id, max_players, status, start_time và end_time

-- === Room Participant ===
INSERT INTO room_participant (room_id, user_id, score, is_host, joined_at, left_at, is_guest, guest_name)
VALUES (1, 1, 0, TRUE, NOW(), NULL, FALSE, NULL),
       (1, 2, 0, FALSE, NOW(), NULL, FALSE, NULL),
       (2, 2, 0, TRUE, NOW(), NULL, FALSE, NULL),
       (2, 3, 0, FALSE, NOW(), NULL, FALSE, NULL),
       (3, 3, 0, TRUE, NOW(), NULL, FALSE, NULL),
       (3, 1, 0, FALSE, NOW(), NULL, FALSE, NULL)
ON DUPLICATE KEY UPDATE score      = VALUES(score),
                        is_host    = VALUES(is_host),
                        joined_at  = VALUES(joined_at),
                        left_at    = VALUES(left_at),
                        is_guest   = VALUES(is_guest),
                        guest_name = VALUES(guest_name);
-- Nếu room_participant đã tồn tại thì cập nhật score, is_host, joined_at, left_at, is_guest và guest_name

-- *****************************************************************************
-- KẾT THÚC SCRIPT
-- *****************************************************************************

SELECT 'Database initialization and sample data insertion completed.' AS Status;