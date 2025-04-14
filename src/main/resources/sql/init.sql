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
-- II. TẠO CÁC BẢNG (Dựa theo thiet_ke_co_so_du_lieu.pdf)
-- *****************************************************************************

-- Bảng users
CREATE TABLE IF NOT EXISTS users
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    email             VARCHAR(100) NOT NULL UNIQUE,
    password          VARCHAR(255) NOT NULL,
    full_name         VARCHAR(100) NOT NULL,
    role              VARCHAR(20)  NOT NULL DEFAULT 'USER',
    avatar_url        VARCHAR(255),
    avatar_type       VARCHAR(20)           DEFAULT 'DEFAULT',
    avatar_updated_at TIMESTAMP    NULL,                                                           -- Cho phép NULL
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Cho phép NULL và tự cập nhật
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- Bảng default_avatars
CREATE TABLE IF NOT EXISTS default_avatars
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    url        VARCHAR(255) NOT NULL,
    category   VARCHAR(50)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_avatar_category (category)
);

-- Bảng categories
CREATE TABLE IF NOT EXISTS categories
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    difficulty  TINYINT               DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_name (name)
);

-- Bảng questions
CREATE TABLE IF NOT EXISTS questions
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id    BIGINT    NOT NULL,
    content        TEXT      NOT NULL,
    option_a       TEXT      NOT NULL,
    option_b       TEXT      NOT NULL,
    option_c       TEXT      NOT NULL,
    option_d       TEXT      NOT NULL,
    correct_option CHAR(1)   NOT NULL,
    points         INT       NOT NULL DEFAULT 1,
    difficulty     TINYINT   NOT NULL DEFAULT 1,
    image_url      VARCHAR(255),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE, -- Thêm ON DELETE CASCADE
    INDEX idx_question_category (category_id),
    INDEX idx_question_difficulty (difficulty)
);

-- Bảng rooms
CREATE TABLE IF NOT EXISTS rooms
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    code             VARCHAR(10)  NOT NULL UNIQUE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'WAITING',
    start_time       DATETIME     NULL,                                     -- Cho phép NULL
    end_time         DATETIME     NULL,                                     -- Cho phép NULL
    duration         INT          NOT NULL,
    max_participants INT          NOT NULL DEFAULT 10,
    creator_id       BIGINT       NOT NULL,
    category_id      BIGINT       NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users (id) ON DELETE CASCADE,       -- Thêm ON DELETE CASCADE
    FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE, -- Thêm ON DELETE CASCADE
    INDEX idx_room_code (code),
    INDEX idx_room_status (status),
    INDEX idx_room_start_time (start_time)
);

-- Bảng room_questions (Sử dụng `order` thay vì order để tránh từ khóa SQL)
CREATE TABLE IF NOT EXISTS room_questions
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id     BIGINT    NOT NULL,
    question_id BIGINT    NOT NULL,
    `order`     INT       NOT NULL,                                        -- Sử dụng backticks cho từ khóa 'order'
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY idx_room_question (room_id, question_id),
    FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE,         -- Thêm ON DELETE CASCADE
    FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE, -- Thêm ON DELETE CASCADE
    INDEX idx_room_question_order (room_id, `order`)                       -- Sử dụng backticks
);

-- Bảng room_participants
CREATE TABLE IF NOT EXISTS room_participants
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    join_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status     VARCHAR(20) NOT NULL DEFAULT 'JOINED',
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY idx_room_participant (room_id, user_id),
    FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE, -- Thêm ON DELETE CASCADE
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE, -- Thêm ON DELETE CASCADE
    INDEX idx_participant_status (status)
);

-- Bảng user_answers
CREATE TABLE IF NOT EXISTS user_answers
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT    NOT NULL,
    question_id     BIGINT    NOT NULL,
    room_id         BIGINT    NOT NULL,
    selected_option CHAR(1)   NULL,                                        -- Cho phép NULL nếu không trả lời
    is_correct      BOOLEAN   NOT NULL DEFAULT FALSE,
    answer_time     INT       NULL,                                        -- Thời gian trả lời (giây), cho phép NULL
    points_earned   INT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY idx_user_question_room (user_id, question_id, room_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,         -- Thêm ON DELETE CASCADE
    FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE, -- Thêm ON DELETE CASCADE
    FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE,         -- Thêm ON DELETE CASCADE
    INDEX idx_answer_correctness (is_correct)
);

-- Bảng scores
CREATE TABLE IF NOT EXISTS scores
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT    NOT NULL,
    room_id         BIGINT    NOT NULL,
    total_points    INT       NOT NULL DEFAULT 0,
    correct_answers INT       NOT NULL DEFAULT 0,
    wrong_answers   INT       NOT NULL DEFAULT 0,
    rank            INT       NULL,                                -- Cho phép NULL
    completion_time INT       NULL,                                -- Thời gian hoàn thành (giây), cho phép NULL
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY idx_user_room_score (user_id, room_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE, -- Thêm ON DELETE CASCADE
    FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE, -- Thêm ON DELETE CASCADE
    INDEX idx_score_points (total_points),
    INDEX idx_score_rank (room_id, rank)
);


-- *****************************************************************************
-- III. CHÈN DỮ LIỆU MẪU
-- *****************************************************************************

-- === Users ===
-- Mật khẩu cho tất cả user mẫu là: "password123"
-- Mã hóa BCrypt cho "password123": $2a$10$NVM0n8cUw5U7uUB31p./0.6W.L.GWH9e9/NMoWBJalfHlMAOvZWCm
INSERT INTO users (username, email, password, full_name, role, avatar_url, avatar_type)
VALUES ('user1', 'user1@example.com', '$2a$10$NVM0n8cUw5U7uUB31p./0.6W.L.GWH9e9/NMoWBJalfHlMAOvZWCm', 'Người Dùng Một',
        'USER', '/storage/avatars/default/human1.png', 'DEFAULT'),
       ('user2', 'user2@example.com', '$2a$10$NVM0n8cUw5U7uUB31p./0.6W.L.GWH9e9/NMoWBJalfHlMAOvZWCm', 'Người Dùng Hai',
        'USER', '/storage/avatars/default/animal1.png', 'DEFAULT'),
       ('admin1', 'admin1@example.com', '$2a$10$NVM0n8cUw5U7uUB31p./0.6W.L.GWH9e9/NMoWBJalfHlMAOvZWCm', 'Quản Trị Viên',
        'ADMIN', NULL, 'DEFAULT')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);
-- Nếu user đã tồn tại thì chỉ cập nhật tên

-- === Default Avatars ===
INSERT INTO default_avatars (name, url, category)
VALUES ('Human 1', '/storage/avatars/default/human1.png', 'Human'),
       ('Human 2', '/storage/avatars/default/human2.png', 'Human'),
       ('Animal 1', '/storage/avatars/default/animal1.png', 'Animal'),
       ('Cartoon 1', '/storage/avatars/default/cartoon1.png', 'Cartoon')
ON DUPLICATE KEY UPDATE url = VALUES(url);
-- Nếu avatar đã tồn tại thì cập nhật url

-- === Categories ===
INSERT INTO categories (name, description, difficulty)
VALUES ('Lịch sử Việt Nam', 'Các câu hỏi về lịch sử dân tộc Việt Nam qua các thời kỳ.', 2),
       ('Địa lý Thế giới', 'Kiến thức về địa lý các quốc gia, châu lục, sông ngòi, biển cả.', 3),
       ('Khoa học Tự nhiên', 'Câu hỏi về Vật lý, Hóa học, Sinh học cơ bản.', 3),
       ('Văn học Việt Nam', 'Tác giả, tác phẩm nổi tiếng trong nền văn học Việt Nam.', 2)
ON DUPLICATE KEY UPDATE description = VALUES(description);
-- Nếu category đã tồn tại thì cập nhật description

-- === Questions ===
-- Lấy ID của các category vừa tạo (Lưu ý: ID có thể thay đổi nếu bảng đã có dữ liệu)
SET @lichsu_id = (SELECT id
                  FROM categories
                  WHERE name = 'Lịch sử Việt Nam');
SET @dialy_id = (SELECT id
                 FROM categories
                 WHERE name = 'Địa lý Thế giới');
SET @khoahoc_id = (SELECT id
                   FROM categories
                   WHERE name = 'Khoa học Tự nhiên');

-- Questions cho Lịch sử Việt Nam (@lichsu_id)
INSERT INTO questions (category_id, content, option_a, option_b, option_c, option_d, correct_option, points, difficulty)
VALUES (@lichsu_id, 'Chiến thắng Điện Biên Phủ diễn ra vào năm nào?', '1945', '1954', '1975', '1968', 'B', 1, 2),
       (@lichsu_id, 'Vị vua nào đã dời đô từ Hoa Lư về Thăng Long?', 'Lý Thái Tổ', 'Trần Nhân Tông', 'Lê Lợi',
        'Quang Trung', 'A', 1, 1),
       (@lichsu_id, 'Hai Bà Trưng khởi nghĩa chống lại ách đô hộ của triều đại nào?', 'Nhà Hán', 'Nhà Đường',
        'Nhà Tống', 'Nhà Nguyên', 'A', 2, 3);

-- Questions cho Địa lý Thế giới (@dialy_id)
INSERT INTO questions (category_id, content, option_a, option_b, option_c, option_d, correct_option, points, difficulty)
VALUES (@dialy_id, 'Dãy núi cao nhất thế giới là gì?', 'Andes', 'Alps', 'Himalaya', 'Rocky', 'C', 1, 2),
       (@dialy_id, 'Quốc gia nào có diện tích lớn nhất thế giới?', 'Trung Quốc', 'Canada', 'Hoa Kỳ', 'Nga', 'D', 1, 2),
       (@dialy_id, 'Sông dài nhất thế giới là sông nào?', 'Amazon', 'Mississippi', 'Trường Giang', 'Nile', 'D', 2, 3);

-- Questions cho Khoa học Tự nhiên (@khoahoc_id)
INSERT INTO questions (category_id, content, option_a, option_b, option_c, option_d, correct_option, points, difficulty)
VALUES (@khoahoc_id, 'Công thức hóa học của nước là gì?', 'CO2', 'O2', 'H2O', 'NaCl', 'C', 1, 1),
       (@khoahoc_id, 'Hành tinh nào gần Mặt Trời nhất?', 'Sao Kim', 'Sao Hỏa', 'Sao Thủy', 'Trái Đất', 'C', 1, 1),
       (@khoahoc_id, 'Lực hút của Trái Đất tác dụng lên mọi vật gọi là gì?', 'Lực đẩy Archimedes', 'Lực ma sát',
        'Lực hấp dẫn (Trọng lực)', 'Lực đàn hồi', 'C', 1, 2);


-- === Rooms (Ví dụ tạo 1 phòng) ===
-- Lấy ID của người tạo (admin1) và category Lịch sử
SET @admin_id = (SELECT id
                 FROM users
                 WHERE username = 'admin1');
-- SET @lichsu_id đã có ở trên

INSERT INTO rooms (name, code, duration, max_participants, creator_id, category_id, status)
VALUES ('Lịch Sử Hào Hùng', UPPER(SUBSTRING(MD5(RAND()), 1, 6)), 600, 10, @admin_id, @lichsu_id,
        'WAITING') -- Mã phòng ngẫu nhiên 6 ký tự, thời gian 10 phút (600s)
ON DUPLICATE KEY UPDATE name = VALUES(name);
-- Nếu mã phòng trùng (khó xảy ra) thì cập nhật tên

-- === Room Questions (Ví dụ thêm 2 câu hỏi vào phòng) ===
-- Lấy ID phòng vừa tạo
SET @room_id = (SELECT id
                FROM rooms
                WHERE code = (SELECT code FROM rooms ORDER BY created_at DESC LIMIT 1));
-- Lấy phòng mới nhất
-- Lấy ID 2 câu hỏi lịch sử
SET @q1_id = (SELECT id
              FROM questions
              WHERE content LIKE 'Chiến thắng Điện Biên Phủ%');
SET @q2_id = (SELECT id
              FROM questions
              WHERE content LIKE 'Vị vua nào đã dời đô%');

INSERT INTO room_questions (room_id, question_id, `order`)
VALUES (@room_id, @q1_id, 1),
       (@room_id, @q2_id, 2)
ON DUPLICATE KEY UPDATE `order` = VALUES(`order`);

-- === Room Participants (Ví dụ thêm user1 vào phòng) ===
-- Lấy ID của user1
SET @user1_id = (SELECT id
                 FROM users
                 WHERE username = 'user1');

INSERT INTO room_participants (room_id, user_id, status)
VALUES (@room_id, @user1_id, 'JOINED')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- *****************************************************************************
-- KẾT THÚC SCRIPT
-- *****************************************************************************

SELECT 'Database initialization and sample data insertion completed.' AS Status;