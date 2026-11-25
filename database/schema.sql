CREATE DATABASE IF NOT EXISTS BKGRADE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE BKGRADE;

-- 1. Bảng users (Tài khoản người dùng & sinh viên)
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    student_code VARCHAR(20),
    role VARCHAR(20) DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo sẵn 1 tài khoản mẫu (id = 1) để test bảng điểm khi chưa ghép phần đăng nhập của Nghĩa
INSERT INTO users (id, username, password, full_name, student_code, role) 
VALUES (1, 'sinhvien', '$2a$12$e8p.9pT8i5VqH5K2l1y9OeBw6qZ7P8rY5vK6f1m3n5b8c9d0e1f2g', 'Nguyễn Văn Hiếu', '102220000', 'STUDENT')
ON DUPLICATE KEY UPDATE id = id;

-- 2. Bảng courses (Danh mục môn học)
CREATE TABLE IF NOT EXISTS courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(150) UNIQUE NOT NULL,
    credits DOUBLE NOT NULL DEFAULT 0
);

-- 3. Bảng grades (Điểm chi tiết của từng sinh viên)
CREATE TABLE IF NOT EXISTS grades (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    credits DOUBLE NOT NULL DEFAULT 0,
    formula VARCHAR(100),
    score_bt DOUBLE NULL,
    score_bv DOUBLE NULL,
    score_ck DOUBLE NULL,
    score_da DOUBLE NULL,
    score_gk DOUBLE NULL,
    score_qt DOUBLE NULL,
    score_th DOUBLE NULL,
    score_10 DOUBLE NULL,
    score_4 DOUBLE NULL,
    score_letter VARCHAR(5) NULL,
    is_predicted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_grade_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_course_sem (user_id, course_id, semester)
);
