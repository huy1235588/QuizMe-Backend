-- *****************************************************************************
-- 1. Category
-- *****************************************************************************
INSERT INTO category (id, name, description, icon_url, quiz_count, total_play_count, is_active)
VALUES (1,
        'Geography',
        'Explore the world through geography questions, from countries, cities to natural wonders!',
        'category_1_1745400000.png',
        1,
        0,
        true)
ON DUPLICATE KEY UPDATE name             = VALUES(name),
                        description      = VALUES(description),
                        icon_url         = VALUES(icon_url),
                        quiz_count       =
                            VALUES(quiz_count),
                        total_play_count =
                            VALUES(total_play_count),
                        is_active        =
                            VALUES(is_active);
-- Nếu category đã tồn tại, cập nhật description và icon_url

-- *****************************************************************************
-- 2. Quiz
-- *****************************************************************************
INSERT INTO quiz (id, title, description, quiz_thumbnails, category_id, creator_id, difficulty, is_public, play_count,
                  question_count)
VALUES (1,
        'Flags of World Quiz',
        'Guess the country based on the displayed national flag.',
        'quiz_thumbnail_1_1745400000.jpg',
        1,
        3, -- lehuy
        'medium',
        true,
        0,
        20)
ON DUPLICATE KEY UPDATE title       = VALUES(title),
                        description = VALUES(description),
                        category_id = VALUES(category_id),
                        creator_id  = VALUES(creator_id),
                        difficulty  = VALUES(difficulty),
                        is_public   = VALUES(is_public);
-- Nếu quiz đã tồn tại, cập nhật title, description, category_id, creator_id, difficulty và is_public

-- *****************************************************************************
-- 3. Question
-- *****************************************************************************
INSERT INTO question (id, quiz_id, content, image_url, time_limit, points, order_number)
VALUES (1,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_1_1745400000.jpg',
        10,
        10,
        1),
       (2,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_2_1680000002.jpg',
        10,
        10,
        2),

       (3,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_3_1680000003.jpg',
        10,
        10,
        3),

       (4,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_4_1680000004.jpg',
        10,
        10,
        4),

       (5,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_5_1680000005.jpg',
        10,
        10,
        5),

       (6,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_6_1680000006.jpg',
        10,
        10,
        6),

       (7,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_7_1680000007.jpg',
        10,
        10,
        7),

       (8,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_8_1680000008.jpg',
        10,
        10,
        8),

       (9,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_9_1680000009.jpg',
        10,
        10,
        9),

       (10,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_10_1680000010.jpg',
        10,
        10,
        10),

       (11,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_11_1680000011.jpg',
        10,
        10,
        11),

       (12,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_12_1680000012.jpg',
        10,
        10,
        12),

       (13,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_13_1680000013.jpg',
        10,
        10,
        13),

       (14,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_14_1680000014.jpg',
        10,
        10,
        14),

       (15,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_15_1680000015.jpg',
        10,
        10,
        15),

       (16,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_16_1680000016.jpg',
        10,
        10,
        16),

       (17,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_17_1680000017.jpg',
        10,
        10,
        17),

       (18,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_18_1680000018.jpg',
        10,
        10,
        18),

       (19,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_19_1680000019.jpg',
        10,
        10,
        19),

       (20,
        1,
        'Which country does this flag belong to?',
        'quiz_1_question_20_1680000020.jpg',
        10,
        10,
        20)
ON DUPLICATE KEY UPDATE quiz_id      = VALUES(quiz_id),
                        content      = VALUES(content),
                        image_url    = VALUES(image_url),
                        time_limit   = VALUES(time_limit),
                        points       = VALUES(points),
                        order_number = VALUES(order_number);
-- Nếu question đã tồn tại, cập nhật quiz_id, content, image_url, time_limit, points và order_number

-- *****************************************************************************
-- 4. Question Option
-- *****************************************************************************
INSERT INTO question_option (question_id, content, is_correct)
VALUES
    -- Q1: France
    (1, 'France', TRUE),
    (1, 'Italy', FALSE),
    (1, 'Russia', FALSE),
    (1, 'Netherlands', FALSE),

    -- Q2: Japan
    (2, 'Japan', TRUE),
    (2, 'China', FALSE),
    (2, 'South Korea', FALSE),
    (2, 'Thailand', FALSE),

    -- Q3: Canada
    (3, 'Canada', TRUE),
    (3, 'United States', FALSE),
    (3, 'Australia', FALSE),
    (3, 'Ireland', FALSE),

    -- Q4: Germany
    (4, 'Germany', TRUE),
    (4, 'Belgium', FALSE),
    (4, 'Austria', FALSE),
    (4, 'Switzerland', FALSE),

    -- Q5: Brazil
    (5, 'Brazil', TRUE),
    (5, 'Argentina', FALSE),
    (5, 'Portugal', FALSE),
    (5, 'Colombia', FALSE),

    -- Q6: India
    (6, 'India', TRUE),
    (6, 'Pakistan', FALSE),
    (6, 'Bangladesh', FALSE),
    (6, 'Sri Lanka', FALSE),

    -- Q7: USA
    (7, 'United States', TRUE),
    (7, 'United Kingdom', FALSE),
    (7, 'Australia', FALSE),
    (7, 'Canada', FALSE),

    -- Q8: China
    (8, 'China', TRUE),
    (8, 'Japan', FALSE),
    (8, 'South Korea', FALSE),
    (8, 'Vietnam', FALSE),

    -- Q9: Russia
    (9, 'Russia', TRUE),
    (9, 'Czech Republic', FALSE),
    (9, 'Serbia', FALSE),
    (9, 'Slovenia', FALSE),

    -- Q10: Italy
    (10, 'Italy', TRUE),
    (10, 'Ireland', FALSE),
    (10, 'Mexico', FALSE),
    (10, 'Mexico', FALSE),

    -- Q11: Australia
    (11, 'Australia', TRUE),
    (11, 'New Zealand', FALSE),
    (11, 'United Kingdom', FALSE),
    (11, 'Fiji', FALSE),

    -- Q12: South Africa
    (12, 'South Africa', TRUE),
    (12, 'Kenya', FALSE),
    (12, 'Zimbabwe', FALSE),
    (12, 'Namibia', FALSE),

    -- Q13: Mexico
    (13, 'Mexico', TRUE),
    (13, 'Spain', FALSE),
    (13, 'Chile', FALSE),
    (13, 'Peru', FALSE),

    -- Q14: South Korea
    (14, 'South Korea', TRUE),
    (14, 'North Korea', FALSE),
    (14, 'China', FALSE),
    (14, 'Japan', FALSE),

    -- Q15: Spain
    (15, 'Spain', TRUE),
    (15, 'Portugal', FALSE),
    (15, 'Italy', FALSE),
    (15, 'Mexico', FALSE),

    -- Q16: Argentina
    (16, 'Argentina', TRUE),
    (16, 'Uruguay', FALSE),
    (16, 'Chile', FALSE),
    (16, 'Colombia', FALSE),

    -- Q17: Kenya
    (17, 'Kenya', TRUE),
    (17, 'Uganda', FALSE),
    (17, 'Tanzania', FALSE),
    (17, 'Ethiopia', FALSE),

    -- Q18: Sweden
    (18, 'Sweden', TRUE),
    (18, 'Finland', FALSE),
    (18, 'Norway', FALSE),
    (18, 'Denmark', FALSE),

    -- Q19: Egypt
    (19, 'Egypt', TRUE),
    (19, 'Iraq', FALSE),
    (19, 'Sudan', FALSE),
    (19, 'Libya', FALSE),

    -- Q20: Turkey
    (20, 'Turkey', TRUE),
    (20, 'Cyprus', FALSE),
    (20, 'Azerbaijan', FALSE),
    (20, 'Greece', FALSE)
ON DUPLICATE KEY UPDATE question_id = VALUES(question_id),
                        content     = VALUES(content),
                        is_correct  = VALUES(is_correct);
-- Nếu question_option đã tồn tại, cập nhật question_id, content và is_correct
