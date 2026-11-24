# Đặc tả thiết kế: Tính năng Dự đoán điểm & Bộ mô phỏng mục tiêu GPA (DUT Grade Predictor & Simulator)

- **Ngày tạo:** 24/11/2026
- **Trạng thái:** Đã xác thực thiết kế (Validated Design)
- **Tác giả:** Hiếu & AI Assistant (Antigravity)
- **Nhánh:** `hieu-sub`

---

## 1. Tóm tắt yêu cầu (Understanding Summary)
* **Mục tiêu:** Xây dựng tính năng hỗ trợ sinh viên DUT dự đoán và lập chiến lược điểm số ở 2 cấp độ:
  1. *Cấp độ môn học (Course Target):* Tính ngược điểm thành phần còn thiếu (CK, GK...) dựa trên công thức môn học để đạt từng mốc điểm chữ ($A, B+, B, C+...$).
  2. *Cấp độ toàn khóa/kỳ (GPA Goal Simulator):* Bộ mô phỏng mục tiêu GPA thời gian thực cho các môn đang học hoặc môn giả định, kèm thuật toán gợi ý kịch bản phân bổ điểm tối ưu (Kịch bản an toàn, Kịch bản tối thiểu).
* **Vị trí giao diện:** 
  - Tab mới **`🎯 Dự đoán & Mục tiêu`** trên Dashboard, điều hướng mượt mà bằng Bootstrap 5 Nav-Tabs (không reload trang).
  - Tích hợp bảng tính nhanh điểm CK cần đạt ngay trong Modal chỉnh sửa môn học.
* **Cơ chế lưu trữ:** Chế độ **Thử nghiệm an toàn (Sandbox)** chạy trên RAM trình duyệt, không ảnh hưởng CSDL thật. Chỉ lưu khi người dùng bấm "Áp dụng vào CSDL".

---

## 2. Nhật ký quyết định (Decision Log)

| STT | Quyết định | Các phương án đã cân nhắc | Lý do lựa chọn |
| :--- | :--- | :--- | :--- |
| **D-01** | Phạm vi bài toán: Hỗ trợ cả 2 cấp độ (Môn học & GPA tổng) | Chỉ làm dự đoán môn học HOẶC Chỉ làm GPA tổng | Sinh viên cần cả 2: vừa cần biết thi cuối kỳ môn này cần mấy điểm, vừa cần nhìn bức tranh tổng thể để đạt GPA 3.2 / 3.6. |
| **D-02** | Cơ chế tính toán GPA: Kết hợp Sandbox tương tác + Gợi ý tối ưu | Chỉ tự động HOẶC Chỉ kéo thả tay | Cho phép sinh viên tự do "nghịch" các điểm số theo ý thích, vừa hỗ trợ sinh viên khi lười tính toán bằng 1 nút bấm gợi ý. |
| **D-03** | Vị trí giao diện: Tab chuyên biệt trên Dashboard | Modal pop-up HOẶC Trang riêng biệt (`/predict`) | Tab trên Dashboard giữ trọn ngữ cảnh, không phải reload trang, không làm rối bảng điểm hiện tại. |
| **D-04** | Cơ chế lưu trữ: Sandbox an toàn (Chỉ lưu khi bấm xác nhận) | Tự động lưu vào DB HOẶC Không cho phép lưu | Đảm bảo an toàn dữ liệu điểm thật, tránh việc nghịch điểm dự đoán làm sai lệch bảng điểm thật. |
| **D-05** | Kiến trúc: Client-side Engine (`simulator.js`) | Server-side Engine qua AJAX Servlet (`PredictBO`) | Tốc độ phản hồi tức thì (0ms latency), trải nghiệm kéo thả mượt mà, không gây tải cho Tomcat và TiDB Cloud. |

---

## 3. Kiến trúc & Thiết kế chi tiết

### 3.1. Cấu trúc Component
* **`dashboard.jsp`:**
  * Bổ sung thanh Tab điều hướng: `📋 Bảng điểm chính thức` và `🎯 Dự đoán & Mục tiêu`.
  * Nội dung Tab 2: Bảng mục tiêu CPA (Tiến độ, Target input, nút gợi ý kịch bản) và Danh sách môn đang học/mô phỏng với dropdown chọn điểm dự kiến.
  * Mở rộng Modal `editGradeModal`: Bổ sung bảng gợi ý *"Điểm CK cần đạt"* tương ứng với các mức điểm chữ.
* **`assets/js/simulator.js`:** Module JavaScript độc lập quản lý:
  * Trạng thái mô phỏng (`simState`).
  * Thuật toán giải phương trình điểm thành phần (`calculateRequiredScore`).
  * Bộ tính CPA thời gian thực (`calculateLiveCPA`).
  * Bộ giải thuật gợi ý kịch bản (`optimizeScenarios`).
* **`GradeController.java`:**
  * Bổ sung action `apply_sim` nhận batch danh sách `gradeId` và điểm dự kiến để lưu vào CSDL khi người dùng yêu cầu.

### 3.2. Thuật toán chi tiết
1. **Course Target Solver:**
   $$Điểm\_CK_{cần} = \frac{Ngưỡng\_Điểm_{mục\_tiêu} - \sum (Điểm_{đã\_có} \times Trọng\_số)}{Trọng\_số\_CK}$$
   - $CK > 10.0$: `Không khả thi`.
   - $CK \le 0.0$: `Chắc chắn đạt`.
   - $0.0 < CK \le 10.0$: Làm tròn lên 1 chữ số thập phân.
2. **CPA Live Calculator:**
   $$CPA = \frac{\sum (\text{Tín chỉ môn đã hoàn thành} \times \text{Điểm 4}) + \sum (\text{Tín chỉ môn mô phỏng} \times \text{Điểm 4})}{\text{Tổng tín chỉ tích lũy (không tính môn 0 TC)}}$$
3. **Smart Scenario Optimizer:**
   - Kịch bản an toàn (Even Distribution): Tìm mức điểm đồng đều thấp nhất để tổng CPA $\ge$ Target.
   - Kịch bản tập trung (Credit-weighted): Ưu tiên gán điểm cao (A, B+) cho môn nhiều tín chỉ, giảm tải cho môn ít tín chỉ.

---

## 4. Xử lý trường hợp biên (Edge Cases)
* Mục tiêu GPA vượt quá mức tối đa có thể đạt trong kỳ: Hiển thị cảnh báo số tín chỉ cần học thêm ở các kỳ sau.
* Môn học 0 tín chỉ (GDTC/GDQP): Loại trừ khỏi công thức tính CPA.
* Môn chưa có điểm thành phần: Mặc định tính trên điểm 10 dự kiến hoặc cho phép chọn công thức mẫu.
