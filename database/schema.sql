CREATE DATABASE IF NOT EXISTS BKGRADE;
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE BKGRADE;
-- 1. Bảng users

-- 2. Bảng courses
CREATE TABLE courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(50) UNIQUE NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    credits INT NOT NULL DEFAULT 0
);
-- 3. Bảng grades
CREATE TABLE grades(
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    formula VARCHAR(100),
    score_10 DOUBLE,
    score_4 DOUBLE,
    score_letter VARCHAR(5),
    CONSTRAINT fk_grade_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_course_sem (user_id, course_id, semester)
);