# Kế hoạch triển khai: Tính năng Dự đoán điểm & Bộ mô phỏng mục tiêu GPA (DUT Grade Predictor & Simulator)

> **Nhánh thực hiện:** `hieu-sub`  
> **Tài liệu đặc tả chi tiết:** `docs/superpowers/specs/2026-11-24-grade-prediction-design.md`  
> **Trạng thái:** Chờ phê duyệt từ người dùng trước khi triển khai mã nguồn  

---

## 1. Mục tiêu tính năng (Goal Description)
Xây dựng tính năng hỗ trợ sinh viên DUT lập chiến lược điểm số và mục tiêu học tập thông qua 2 cấp độ:
1. **Dự đoán điểm thành phần môn học:** Tính toán điểm thi cuối kỳ (CK) hoặc điểm thành phần còn thiếu để đạt các mốc điểm chữ mong muốn ($A, B+, B, C+...$) dựa trên công thức trọng số của từng môn.
2. **Bộ mô phỏng mục tiêu GPA toàn khóa/học kỳ (Sandbox):** Cho phép sinh viên đặt mục tiêu CPA (ví dụ 3.20, 3.60), thử nghiệm các kịch bản điểm số cho các môn đang học với phản hồi tức thì (<10ms), và cung cấp nút bấm *"🪄 Gợi ý kịch bản tối ưu"* (An toàn / Tiết kiệm sức).
3. **Cơ chế Sandbox an toàn:** Mọi tính toán diễn ra trong bộ nhớ trình duyệt, không làm sai lệch bảng điểm thật cho đến khi người dùng chủ động bấm *"💾 Lưu điểm dự kiến vào CSDL"*.

---

## 2. Cam kết & Ràng buộc kỹ thuật (Constraints)
- **Không thay đổi CSDL:** Giữ nguyên schema hiện có (`courses`, `grades`), không cần chạy migration.
- **Phân định rõ ranh giới:** Không tác động vào các tệp thuộc Track A (`User*`, `Auth*`, `login.jsp`, `register.jsp`).
- **Quy chuẩn tính điểm DUT:** Môn 0 tín chỉ (GDTC, GDQP) không tham gia tính CPA; thang điểm quy đổi tuân thủ nghiêm ngặt chuẩn Đại học Bách Khoa - ĐHĐN:
  - $A \ge 8.5 \to 4.0$
  - $B+ \ge 8.0 \to 3.5$
  - $B \ge 7.0 \to 3.0$
  - $C+ \ge 6.5 \to 2.5$
  - $C \ge 5.5 \to 2.0$
  - $D+ \ge 5.0 \to 1.5$
  - $D \ge 4.0 \to 1.0$
  - $F < 4.0 \to 0.0$

---

## 3. Các đầu việc cụ thể (Task Breakdown)

### Bước 1: Tạo Module JavaScript Mô Phỏng (`assets/js/simulator.js`)
- [ ] Xây dựng hàm `calculateRequiredScore(formula, currentScores, targetScore10)` giải phương trình tìm điểm CK cần đạt cho từng mốc điểm chữ.
- [ ] Xây dựng hàm `renderModalPredictionTable()` hiển thị bảng gợi ý điểm CK có thể click chọn ngay trong Modal sửa môn.
- [ ] Xây dựng engine `initGpaSimulator()` nạp danh sách các môn đang học và khởi tạo trạng thái Sandbox trong RAM.
- [ ] Xây dựng hàm `recalculateSimulatorCpa()` tính toán lại CPA tức thì khi thay đổi điểm dự kiến.
- [ ] Xây dựng thuật toán `suggestOptimalScenarios(targetGpa)` tìm 2 kịch bản phân bổ điểm (Đều tay vs Trọng số tín chỉ).

### Bước 2: Nâng cấp Giao diện Dashboard (`dashboard.jsp`)
- [ ] Chuyển đổi giao diện sang dạng 2 Tab bằng Bootstrap 5 Nav-Tabs:
  - `📋 Bảng điểm chính thức` (giữ nguyên toàn bộ bảng điểm, thống kê và bộ lọc).
  - `🎯 Dự đoán & Mục tiêu GPA` (khu vực Sandbox mới).
- [ ] Dựng giao diện Tab Sandbox:
  - Thẻ KPI hiển thị CPA hiện tại vs CPA mục tiêu kèm thanh tiến độ trực quan.
  - Hộp nhập GPA mong muốn + Nút *"🪄 Gợi ý kịch bản tối ưu"*.
  - Bảng danh sách môn đang học với Dropdown chọn điểm chữ ($A \to F$).
  - Nút bấm *"🔄 Đặt lại thử nghiệm"* và *"💾 Áp dụng vào CSDL"*.
- [ ] Mở rộng Modal `editGradeModal`: Bổ sung khu vực bảng tính nhanh điểm CK cần đạt dưới các ô nhập điểm.

### Bước 3: Hỗ trợ Lưu Điểm Dự Kiến ở Backend (`GradeController.java`)
- [ ] Bổ sung `action=apply_simulation` trong `GradeController.java`:
  - Đọc danh sách các môn được áp dụng điểm dự kiến.
  - Cập nhật điểm vào CSDL thông qua `GradeDAO`.
  - Redirect lại `grades` kèm thông báo thành công.

### Bước 4: Kiểm thử, Xác minh & Đóng gói
- [ ] Build dự án bằng `.\mvnw compile war:exploded`.
- [ ] Kiểm thử trường hợp tính ngược điểm CK: Môn có GK, BT $\to$ Kiểm tra điểm CK cần đạt cho A, B+, B.
- [ ] Kiểm thử Sandbox live update: Đổi điểm môn đang học $\to$ CPA nhảy số lập tức.
- [ ] Kiểm thử Gợi ý kịch bản: Nhập target 3.20 $\to$ Kiểm tra kết quả gợi ý.
- [ ] Kiểm thử lưu CSDL: Bấm lưu $\to$ Bảng điểm thật cập nhật chính xác.
