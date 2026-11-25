# Thiết kế ngắn: Tính năng Xóa hàng loạt môn học (Bulk Delete)

- **Ngày:** 25/11/2026
- **Quy mô công việc:** Thay đổi nhỏ (chạm 4 file Track B: `dashboard.jsp`, `dashboard.js`, `GradeController.java`, `GradeDAO.java`; không thay đổi schema CSDL, không đụng Track A).

---

## 1. Mục đích (Intent & Why)
Cho phép sinh viên chọn và xóa nhiều môn học cùng lúc trên bảng điểm chính thức thay vì phải xóa và xác nhận thủ công từng dòng một.

## 2. Phạm vi & Ràng buộc
- **Mục tiêu (Goals):**
  - Trạng thái bình thường giữ nguyên bố cục bảng 16 cột (ẩn hoàn toàn cột checkbox).
  - Bấm nút **Chọn nhiều để xóa** ở tiêu đề bảng để bật chế độ chọn: hiện cột checkbox ở đầu mỗi hàng và checkbox **Chọn tất cả** ở dòng tiêu đề.
  - Hỗ trợ tick trực tiếp vào checkbox hoặc bấm vào hàng môn học (trừ nút sửa/xóa lẻ) để chọn nhanh; hàng đang chọn được tô màu nhận diện.
  - Thanh thao tác hiện nút **Xóa đã chọn (N)** (bị khóa khi N = 0) và nút **Hủy** (thoát chế độ chọn, bỏ tick toàn bộ).
  - Bấm xóa mở **Bootstrap Modal** xác nhận tự nhiên kèm số lượng môn sắp xóa (ví dụ: *"Bạn có chắc chắn muốn xóa 5 môn học đã chọn khỏi bảng điểm không? Thao tác này không thể hoàn tác."*).
  - Backend xóa toàn bộ các môn đã chọn của đúng `userId` trong 1 lượt truy vấn an toàn.
- **Ngoài phạm vi (Non-goals):** Không có tính năng xóa tự động theo học kỳ, không có thùng rác / khôi phục sau khi xóa, không chỉnh sửa file Track A.
- **Tránh tuyệt đối (Anti-goals):** Không làm chật hoặc vỡ bảng điểm khi chưa bật chế độ chọn; không để lọt lỗ hổng IDOR, CSRF hay SQL Injection.
- **Ràng buộc kỹ thuật:** Giữ nguyên schema CSDL; kế thừa toàn bộ `csrfToken`, kiểm tra `user_id`, `try-with-resources`, PRG flash message và `fn:escapeXml`.

## 3. Các phương án đã cân nhắc (Approaches)
1. **Phương án 1 (Đã chọn) — Bootstrap Modal xác nhận + 1 câu SQL `DELETE ... WHERE user_id = ? AND id IN (?, ?, ...)`:**
   - *Ưu:* Đồng bộ thẩm mỹ với 2 modal Bootstrap 5 hiện có, cảnh báo xóa rõ ràng, chỉ tốn 1 round-trip sang TiDB Cloud (~60ms dù xóa nhiều môn).
   - *Đánh đổi:* Thêm ~25 dòng HTML cho modal xác nhận.
2. **Phương án 2 — `window.confirm()` mặc định + `PreparedStatement.addBatch()`:**
   - *Ưu:* Ít HTML hơn.
   - *Đánh đổi:* Hộp thoại trình duyệt khó nổi bật cảnh báo nguy hiểm, và `DELETE` theo `addBatch()` không được MySQL JDBC gộp như `INSERT` nên chậm hơn trên TiDB.

## 4. Chi tiết triển khai đã chốt
- **Giao diện (`dashboard.jsp`) & Tương tác (`dashboard.js`):**
  - Ẩn nút *Chọn nhiều để xóa* khi bảng chưa có môn học nào.
  - Thêm cột `<th class="bulk-select-col d-none">` chứa `#selectAllGrades` và `<td class="bulk-select-col d-none">` chứa `.grade-select-checkbox` (value = `${g.id}`).
  - Khi bật chế độ chọn: bỏ `d-none` trên `.bulk-select-col`, đổi nhóm nút ở header sang **Xóa đã chọn (`#bulkSelectedCount`)** và **Hủy**.
  - Bấm *Xóa đã chọn* mở `#bulkDeleteConfirmModal`, điền số môn vào câu xác nhận; bấm xác nhận trong modal sẽ tạo các `<input type="hidden" name="gradeIds">` rồi submit form POST (`action=delete_batch`, `csrfToken`).
- **Backend (`GradeController.java`, `GradeDAO.java`):**
  - `GradeController.doPost`: thêm nhánh `action=delete_batch`, đọc `request.getParameterValues("gradeIds")`, lọc các ID số nguyên dương và loại trùng, gọi `gradeDAO.deleteGradesBatch(userId, ids)`, đặt flash message *"Đã xóa thành công X môn học khỏi bảng điểm!"* rồi redirect về `/grades` (PRG).
  - `GradeDAO.deleteGradesBatch(int userId, List<Integer> gradeIds)`: dựng chuỗi placeholder `?` đúng bằng số phần tử, chạy `DELETE FROM grades WHERE user_id = ? AND id IN (?, ?, ...)` với `PreparedStatement` trong `try-with-resources` và trả về số bản ghi đã xóa.
