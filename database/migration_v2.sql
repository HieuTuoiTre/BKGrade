USE BKGRADE;

-- Bổ sung số tín chỉ riêng cho từng sinh viên và cờ đánh dấu điểm dự kiến
ALTER TABLE grades ADD COLUMN IF NOT EXISTS credits DOUBLE NOT NULL DEFAULT 0;
ALTER TABLE grades ADD COLUMN IF NOT EXISTS is_predicted BOOLEAN NOT NULL DEFAULT FALSE;

-- Đồng bộ số tín chỉ hiện có từ bảng courses sang grades (nếu chưa có)
UPDATE grades g
JOIN courses c ON g.course_id = c.id
SET g.credits = c.credits
WHERE g.credits = 0 AND c.credits > 0;
