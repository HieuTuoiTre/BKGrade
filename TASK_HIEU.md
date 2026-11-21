# KẾ HOẠCH PHÂN CÔNG CÔNG VIỆC: BẠN HIẾU (TRACK B)
## Phân hệ: Bóc Tách Bảng Điểm DUT, Quản Lý Điểm Thành Phần, Công Thức Điểm, Connection Pool & Cân Bằng Tải

> **Mục tiêu chính:** 
> Làm chủ toàn bộ chu trình MVC cho phân hệ Quản lý Điểm (từ CSDL đến Giao diện JSP), xây dựng tính năng "sát thủ":
> 1. Bóc tách tự động bảng điểm copy từ `sv.dut.udn.vn` (hỗ trợ đầy đủ 7 cột điểm thành phần: `BT`, `BV`, `CK`, `DA`, `GK`, `QT`, `TH`, các môn lẻ `2.5 TC`, và các môn đang học chưa có điểm tổng kết).
> 2. **Bộ máy tính điểm theo công thức (`formula`):** Tự động tính điểm hệ 10 từ các điểm thành phần theo đúng công thức của từng môn (ví dụ: `[GK]*0.20+[BT]*0.20+[CK]*0.60`, `[QT]*0.40+[BV]*0.60`, `[CK]*1.00`), từ đó tự quy đổi ra điểm hệ 4 và điểm chữ theo quy chế tín chỉ DUT.
> 3. **GPA Simulator & Quản lý điểm linh hoạt:** Cho phép chỉnh sửa từng ô điểm thành phần, nhập điểm dự kiến cho các môn đang học, thêm môn học thủ công bằng tay.
> 4. Tự viết **Custom Connection Pool** chịu tải cao, khắc phục triệt để lỗi timeout TiDB Cloud.
> 5. Cấu hình **Cân bằng tải (Load Balancing)** trên Apache XAMPP và **Deploy lên mạng qua Cloudflare Tunnel**.

---

### I. Danh sách các tệp tin bạn Hiếu phụ trách (Không đụng chạm với bạn Nghĩa)

```text
src/
├── main/
│   ├── java/com/gradecalc/
│   │   ├── config/
│   │   │   └── CustomConnectionPool.java   <- [TRỌNG TÂM] Tự viết Connection Pool chịu tải, chống timeout TiDB
│   │   ├── model/
│   │   │   ├── Bean/
│   │   │   │   ├── Course.java             <- Entity môn học (JavaBean, credits double)
│   │   │   │   ├── Grade.java              <- Entity bảng điểm đầy đủ 7 điểm thành phần (JavaBean)
│   │   │   │   └── SemesterSummary.java    <- Đối tượng tổng kết GPA/CPA (DTO/Bean)
│   │   │   ├── Bo/
│   │   │   │   ├── DutParserBO.java        <- [TÍNH NĂNG ĐỈNH] Bóc tách 19 cột từ DUT (kể cả môn đang học)
│   │   │   │   └── GradeCalculatorBO.java  <- Engine tính điểm theo công thức & thuật toán CPA chuẩn DUT
│   │   │   └── Dao/
│   │   │       ├── CourseDAO.java          <- Tương tác DB bảng courses (ON DUPLICATE KEY UPDATE)
│   │   │       └── GradeDAO.java           <- Tương tác DB bảng grades (Lưu & cập nhật 7 điểm thành phần)
│   │   └── controller/
│   │       └── GradeController.java        <- Servlet xử lý /grades (import, edit, update, add thủ công)
│   └── webapp/
│       ├── assets/
│       │   ├── css/style.css               <- Tùy biến style giao diện
│       │   ├── images/                     <- Ảnh minh họa, logo DUT
│       │   └── videos/                     <- Video hướng dẫn copy bảng điểm
│       ├── index.jsp                       <- Trang chủ / Landing Page công khai
│       └── WEB-INF/
│           └── views/
│               ├── layout/
│               │   ├── navbar.jsp          <- Thanh điều hướng dùng chung
│               │   └── footer.jsp          <- Chân trang dùng chung
│               └── grades/
│                   └── dashboard.jsp       <- Dashboard hiển thị điểm thành phần, công thức & GPA Simulator
```

---

### II. Chi tiết từng bước thực hiện

#### Bước 1: Nhiệm vụ Kỹ thuật 1: Tự viết `CustomConnectionPool.java` (Thầy chấm điểm)
* **Vị trí:** `src/main/java/com/gradecalc/config/CustomConnectionPool.java`
* **Mục tiêu:** Cấp phát và quản lý kết nối CSDL MySQL/TiDB cho toàn bộ ứng dụng, giải quyết bài toán chịu tải cao.
* **Cơ chế triển khai:**
  * Áp dụng mẫu thiết kế **Singleton Pattern** (chỉ có duy nhất 1 bể kết nối trong suốt vòng đời ứng dụng).
  * Sử dụng hàng đợi an toàn đa luồng `BlockingQueue<Connection>` (hoặc `ArrayBlockingQueue<Connection>`).
  * Khởi tạo sẵn các kết nối:
    * `INITIAL_POOL_SIZE = 5` (mở sẵn 5 kết nối khi khởi động web).
    * `MAX_POOL_SIZE = 15` (tối đa 15 kết nối cùng lúc).
  * Viết 2 phương thức cốt lõi:
    * `public Connection getConnection()`: Lấy kết nối rảnh từ hàng đợi. Bắt buộc kiểm tra `conn.isClosed() || !conn.isValid(2)` trong khối `try-catch` an toàn. Nếu kết nối bị ngắt do TiDB idle timeout thì tự động tái tạo kết nối mới bù vào, triệt tiêu lỗi `No operations allowed after connection closed`.
    * `public void releaseConnection(Connection conn)`: Đưa kết nối trở lại hàng đợi sau khi DAO truy vấn xong (tuyệt đối không gọi `conn.close()` để tránh làm chết kết nối).
  * Thêm tham số `autoReconnect=true` vào JDBC URL để đảm bảo tính ổn định trên môi trường Cloud.

#### Bước 2: Cơ sở dữ liệu (Database)
Cập nhật 2 bảng trong MySQL/TiDB (`database/schema.sql`):
1. Bảng `courses` (Danh mục môn học):
   * `id`: Khóa chính tự tăng (`INT AUTO_INCREMENT PRIMARY KEY`).
   * `course_code`: Mã học phần (`VARCHAR(50) UNIQUE NOT NULL`, ví dụ: `1022940.2410.24.12`).
   * `course_name`: Tên môn học (`VARCHAR(150) NOT NULL`).
   * `credits`: Số tín chỉ (`DOUBLE NOT NULL DEFAULT 0` — hỗ trợ các môn lẻ `2.5 TC` như Lập trình Java, Lập trình .NET, HDT, HĐH).
2. Bảng `grades` (Điểm chi tiết của từng sinh viên):
   * `id`: Khóa chính tự tăng.
   * `user_id`: Khóa ngoại liên kết `users.id` của bạn Nghĩa.
   * `course_id`: Khóa ngoại liên kết `courses.id`.
   * `semester`: Học kỳ (ví dụ: `1/2026-2027`, `2/2025-2026`).
   * `formula`: Công thức tính điểm (ví dụ: `[GK]*0.20+[BT]*0.20+[CK]*0.60`, `[QT]*0.40+[BV]*0.60`, `[CK]*1.00`).
   * **7 Cột điểm thành phần chuẩn DUT:**
     * `score_bt`: Điểm Bài tập (`DOUBLE NULL`).
     * `score_bv`: Điểm Bảo vệ (`DOUBLE NULL`).
     * `score_ck`: Điểm Cuối kỳ (`DOUBLE NULL`).
     * `score_da`: Điểm Đồ án (`DOUBLE NULL`).
     * `score_gk`: Điểm Giữa kỳ (`DOUBLE NULL`).
     * `score_qt`: Điểm Quá trình (`DOUBLE NULL`).
     * `score_th`: Điểm Thực hành (`DOUBLE NULL`).
   * **Các cột điểm tổng kết:**
     * `score_10`: Điểm tổng kết hệ 10 (`DOUBLE NULL` — cho phép NULL khi môn đang học chưa có điểm).
     * `score_4`: Điểm hệ 4 (`DOUBLE NULL` — cho phép NULL).
     * `score_letter`: Điểm chữ (`VARCHAR(5) NULL` — A, B+, C, D, F...; để trống nếu môn đang học).
   * Khóa duy nhất chống trùng lặp: `UNIQUE KEY uk_user_course_sem (user_id, course_id, semester)`.

#### Bước 3: Tầng Model - Bean (`Course.java`, `Grade.java`, `SemesterSummary.java`)
* **Vị trí:** `src/main/java/com/gradecalc/model/Bean/`
* `Course.java`:
  * Thuộc tính: `id`, `courseCode`, `courseName`, `credits` (kiểu `double`).
* `Grade.java`:
  * Thuộc tính cốt lõi: `id`, `userId`, `courseId`, `semester`, `formula`.
  * **Thuộc tính 7 điểm thành phần:** `Double scoreBt, scoreBv, scoreCk, scoreDa, scoreGk, scoreQt, scoreTh;` (dùng đối tượng `Double` để nhận giá trị `null`).
  * **Thuộc tính điểm tổng kết:** `Double score10, score4; String scoreLetter;` (dùng `Double` nullable).
  * Thuộc tính phụ hiển thị giao diện: `courseCode`, `courseName`, `credits` (kiểu `double`).
  * Đầy đủ Constructor, Getter và Setter chuẩn JavaBean.
* `SemesterSummary.java`:
  * `totalCredits` dùng `double`.

#### Bước 4: Tầng DAO (`CourseDAO.java`, `GradeDAO.java`)
* **Vị trí:** `src/main/java/com/gradecalc/model/Dao/`
* **`CourseDAO.java`:**
  * `findByCode(String courseCode)`: Tìm kiếm môn học theo mã.
  * `save(Course course)`: Sử dụng câu lệnh:
    ```sql
    INSERT INTO courses (course_code, course_name, credits) VALUES (?, ?, ?)
    ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), course_name=VALUES(course_name), credits=VALUES(credits)
    ```
    Tránh hoàn toàn lỗi `Duplicate entry '...' for key 'courses.course_code'`.
* **`GradeDAO.java`:**
  * `saveGradeList(int userId, List<Grade> gradeList)`: Lưu hoặc cập nhật hàng loạt điểm (Batch update), hỗ trợ ghi nhận đầy đủ 7 điểm thành phần và điểm `null` thông qua `ps.setNull(..., Types.DOUBLE)`.
  * `getGradeByUserId(int userId)`: Truy vấn danh sách toàn bộ điểm (gồm cả 7 điểm thành phần và thông tin môn học).
  * `updateGrade(int gradeId, Grade grade)`: Cập nhật điểm thành phần hoặc điểm tổng kết của một môn học sau khi sinh viên chỉnh sửa trên giao diện.
  * `addManualGrade(int userId, Grade grade)`: Thêm một môn học và điểm nhập tay thủ công.
  * `deleteGrade(int gradeId)`: Cho phép xóa một môn học khỏi bảng điểm cá nhân.

#### Bước 5: Tầng BO 1: `DutParserBO.java` (Bộ não bóc tách dữ liệu DUT 19 cột)
* **Vị trí:** `src/main/java/com/gradecalc/model/Bo/DutParserBO.java`
* Nhận chuỗi thô copy từ `sv.dut.udn.vn/PageKQRL.aspx`.
* **Quy trình thuật toán theo cấu trúc 19 cột chuẩn:**
  1. Tách chuỗi thành từng dòng: `rawText.split("\\r?\\n")`.
  2. Mỗi dòng tách thành các cột bằng ký tự Tab: `line.split("\\t", -1)`. Bỏ qua các dòng tiêu đề hoặc không đủ 14 cột.
  3. Trích xuất thông tin chung:
     * `semester = cols[1].trim()`
     * `courseCode = cols[3].trim()`
     * `courseName = cols[4].trim()`
     * `credits = Double.parseDouble(cols[5].trim())` (hỗ trợ cả môn `2.5 TC`).
     * `formula = cols[6].trim()`
  4. **Trích xuất 7 cột điểm thành phần (từ cột 7 đến 13):**
     * `cols[7]`: `BT` (Bài tập)
     * `cols[8]`: `BV` (Bảo vệ)
     * `cols[9]`: `CK` (Cuối kỳ)
     * `cols[10]`: `DA` (Đồ án)
     * `cols[11]`: `GK` (Giữa kỳ)
     * `cols[12]`: `QT` (Quá trình)
     * `cols[13]`: `TH` (Thực hành)
     *(Sử dụng hàm helper `parseDoubleOrNull(cols[i])` để gán `Double` hoặc `null` nếu ô trống).*
  5. **Bóc tách điểm tổng kết linh hoạt (hỗ trợ môn đang học):**
     * Kiểm tra xem các cột 14, 15, 16 có dữ liệu điểm chữ hợp lệ (`A+`, `A`, `B+`, `B`, `C+`, `C`, `D+`, `D`, `F`) hay không.
     * **Trường hợp môn đã có điểm:** Lấy `score_10` (cột 14), `score_4` (cột 15), `scoreLetter` (cột 16).
     * **Trường hợp môn đang học (chưa có điểm chữ):** Vẫn lưu môn học vào danh sách, giữ các điểm thành phần đã có (ví dụ: `GK = 5.0`), còn `score10 = null`, `score4 = null`, `scoreLetter = null`.
  6. Đóng gói vào đối tượng `Grade` và trả về `List<Grade>`.

#### Bước 6: Tầng BO 2: `GradeCalculatorBO.java` (Bộ máy tính điểm theo công thức & chuẩn CPA DUT)
* **Vị trí:** `src/main/java/com/gradecalc/model/Bo/GradeCalculatorBO.java`
* **1. Engine tính điểm theo công thức (`calculateScoreByFormula`):**
  * Đọc chuỗi công thức dạng `[GK]*0.20+[BT]*0.20+[CK]*0.60` hoặc `[QT]*0.40+[BV]*0.60`.
  * Thay thế các token `[BT]`, `[BV]`, `[CK]`, `[DA]`, `[GK]`, `[QT]`, `[TH]` bằng điểm số thực tế tương ứng.
  * Nếu các điểm thành phần tham gia vào công thức đã được nhập đủ:
    * Tính ra `score_10` (làm tròn 1 chữ số thập phân).
    * Tự động quy đổi ra `score_4` và `score_letter` theo chuẩn tín chỉ DUT:
      * $8.5 \le \text{Điểm 10} \le 10.0 \implies 4.0$ (Điểm A)
      * $8.0 \le \text{Điểm 10} < 8.5 \implies 3.5$ (Điểm B+)
      * $7.0 \le \text{Điểm 10} < 8.0 \implies 3.0$ (Điểm B)
      * $6.5 \le \text{Điểm 10} < 7.0 \implies 2.5$ (Điểm C+)
      * $5.5 \le \text{Điểm 10} < 6.5 \implies 2.0$ (Điểm C)
      * $5.0 \le \text{Điểm 10} < 5.5 \implies 1.5$ (Điểm D+)
      * $4.0 \le \text{Điểm 10} < 5.0 \implies 1.0$ (Điểm D)
      * $\text{Điểm 10} < 4.0 \implies 0.0$ (Điểm F)
* **2. Thuật toán tính GPA học kỳ và CPA tích lũy toàn khóa:**
  $$\text{GPA} = \frac{\sum (\text{Điểm hệ 4} \times \text{Số tín chỉ})}{\sum \text{Số tín chỉ}}$$
  * **Quy tắc vàng:**
    * Bỏ qua các môn có `credits == 0` (GDTC, GDQP).
    * Bỏ qua các môn **chưa có điểm tổng kết (`score4 == null`)** — tuyệt đối không cộng vào mẫu số tổng tín chỉ để không làm tụt CPA thực tế của sinh viên.
    * Khi tính toán đầy đủ các môn `2.5 TC`, điểm tích lũy thang 4 phải ra đúng **3.23** (khớp hoàn hảo 100% với cổng thông tin sinh viên `sv.dut.udn.vn`).
* **3. Xếp loại học lực:** Xuất sắc ($\ge 3.6$), Giỏi ($3.2 - 3.59$), Khá ($2.5 - 3.19$), Trung bình ($2.0 - 2.49$), Yếu/Cảnh báo ($< 2.0$).

#### Bước 7: Tầng View & Controller (`GradeController.java`, `dashboard.jsp`)
* **1. `GradeController.java` (`@WebServlet("/grades")`):**
  * `doGet()`: Lấy danh sách điểm từ DB ➔ Tính tổng kết toàn khóa và từng kỳ ➔ Gửi dữ liệu sang `dashboard.jsp`.
  * `doPost()`: Xử lý các nghiệp vụ qua tham số `action`:
    * `action=import` (mặc định): Bóc tách text copy từ DUT ➔ Lưu DB ➔ Hiển thị kết quả.
    * `action=update`: Cập nhật điểm của một môn học (khi sinh viên sửa điểm thành phần hoặc điểm tổng kết trên giao diện). Nếu có công thức, tự động gọi `GradeCalculatorBO` để tính lại điểm tổng kết.
    * `action=add`: Thêm môn học thủ công do người dùng tự nhập tay các thông tin và điểm số.
* **2. Trang Dashboard tính điểm (`dashboard.jsp`):**
  * **Khối tóm tắt (3 Cards):** Tổng tín chỉ tích lũy (ví dụ: `82 TC`), Điểm trung bình GPA/CPA (`3.23 / 4.0`), Xếp loại học lực (`Giỏi`).
  * **Khối nhập liệu bảng điểm:** Vùng `<textarea>` dán bảng điểm DUT.
  * **Nút bấm & Modal "Thêm môn học thủ công":** Nhập Học kỳ, Mã học phần, Tên môn học, Số TC (hỗ trợ nhập số lẻ như `2.5`), Công thức tính điểm, và các điểm thành phần.
  * **Bảng chi tiết điểm học phần:**
    * Cột hiển thị: STT, Học kỳ, Mã HP, Tên môn, Số TC, **Các điểm thành phần (`BT`, `BV`, `CK`, `DA`, `GK`, `QT`, `TH`)**, Điểm 10, Điểm 4, Điểm chữ, Thao tác.
    * *(Lưu ý UX: Cột Công thức được ẩn khỏi bảng hiển thị chính để bảng điểm thoáng đãng, công thức được giữ ngầm để tự động tính điểm 10 khi người dùng nhập điểm thành phần).*
    * **Thứ tự sắp xếp:** Sắp xếp theo tiến trình thời gian học (môn học trước lên trước, từ học kỳ năm nhất đến học kỳ mới nhất).
    * Với môn đã có kết quả: Hiển thị badge điểm chữ màu sắc theo chuẩn (A, B+, B, C+, C, D+, D, F).
    * Với môn 0 TC (GDTC/GDQP): Tự động nhận diện và gắn nhãn **Đạt** (`badge bg-success`) thay vì nhận nhầm là đang học.
    * Với môn chuyên ngành đang học (chưa có điểm tổng kết): Hiển thị badge mờ `Đang học`.
    * **GPA Simulator & Nút Sửa điểm (✏️):** Modal cho phép sinh viên click sửa bất kỳ ô điểm nào (nhập điểm GK, nhập thử điểm CK dự kiến) ➔ Bấm lưu ➔ Hệ thống tự động tính điểm 10, cập nhật điểm chữ và vẽ lại GPA toàn khóa tức thì!

---

### III. Nhiệm vụ Kỹ thuật trọng tâm của Hiếu: CÂN BẰNG TẢI & DEPLOY (Thầy chấm điểm)

Đây là phần Hiếu sẽ trực tiếp mở máy demo và bảo vệ trước giảng viên:

#### 1. Cấu hình Cân bằng tải trên Apache XAMPP:
* Tạo 2 bản sao của Tomcat:
  * Node 1: Thư mục `tomcat-node-1` (chạy cổng `8080`).
  * Node 2: Thư mục `tomcat-node-2` (chạy cổng `8081`).
* Mở file cấu hình Apache của XAMPP (`C:\xampp\apache\conf\httpd.conf`), bật module:
  ```apache
  LoadModule proxy_module modules/mod_proxy.so
  LoadModule proxy_balancer_module modules/mod_proxy_balancer.so
  LoadModule proxy_http_module modules/mod_proxy_http.so
  LoadModule lbmethod_byrequests_module modules/mod_lbmethod_byrequests.so
  ```
* Khai báo cụm cân bằng tải:
  ```apache
  <Proxy "balancer://gradecalc_cluster">
      BalancerMember "http://localhost:8080/grade-calculator"
      BalancerMember "http://localhost:8081/grade-calculator"
      ProxySet lbmethod=byrequests
  </Proxy>
  ProxyPass "/grade-calculator" "balancer://gradecalc_cluster"
  ProxyPassReverse "/grade-calculator" "balancer://gradecalc_cluster"
  ```
* Bật trang quản trị `/balancer-manager` của Apache để khi báo cáo, mở trang này lên cho thầy thấy số lượng request được chia đều 50/50 giữa 2 server.

#### 2. Deploy đưa website lên mạng (Public Internet):
* Dùng công cụ **Cloudflare Tunnel** (miễn phí):
  ```powershell
  cloudflared tunnel --url http://localhost:80
  ```
* Hệ thống sẽ sinh ra một đường link HTTPS trực tiếp (ví dụ: `https://grade-dut-calculator.trycloudflare.com`). Giảng viên và các bạn trong lớp có thể cầm điện thoại truy cập vào dùng thử ngay lập tức.

---

### IV. Câu hỏi vấn đáp mẫu giúp Hiếu đạt điểm tối đa

* **Thầy hỏi:** *"Em viết Connection Pool như thế nào và tại sao cần nó?"*
* **Hiếu trả lời:**
  > *"Thưa thầy, thông thường mỗi request tạo một kết nối mới tới MySQL sẽ tốn rất nhiều thời gian bắt tay TCP và dễ làm sập DB khi có nhiều người truy cập.  
  > Em đã tự xây dựng lớp CustomConnectionPool theo mô hình Singleton, sử dụng BlockingQueue để giữ sẵn một bể từ 5 đến 15 kết nối mở sẵn. Khi DAO cần truy vấn, nó mượn kết nối từ bể, thực hiện xong câu lệnh thì trả kết nối về bể chứ không đóng lại.  
  > Đặc biệt, em có cài đặt cơ chế kiểm tra kết nối `conn.isValid(2)` tự phục hồi khi database Cloud (TiDB) bị timeout ngắt socket, đảm bảo hệ thống luôn sẵn sàng 100%."*

* **Thầy hỏi:** *"Hệ thống bóc tách và tính điểm xử lý như thế nào đối với các môn đang học và công thức tính điểm?"*
* **Hiếu trả lời:**
  > *"Thưa thầy, cấu trúc bảng điểm DUT gồm 19 cột với 7 cột điểm thành phần (BT, BV, CK, DA, GK, QT, TH) và chuỗi công thức tỷ lệ điểm.  
  > Đối với môn đang học chưa có điểm cuối kỳ, thuật toán của em vẫn nhận diện và lưu lại các điểm thành phần đã có, đồng thời loại trừ môn này khỏi mẫu số tín chỉ tích lũy để không làm sai lệch CPA.  
  > Ngoài ra, hệ thống tích hợp bộ máy phân tích công thức `formula`: khi sinh viên nhập điểm thành phần hoặc thử nghiệm các mức điểm cuối kỳ dự kiến, hệ thống tự động tính ra điểm 10, quy đổi sang thang 4 và điểm chữ theo chuẩn quy chế tín chỉ của trường."*

---

### III. Nhiệm vụ Kỹ thuật trọng tâm của Hiếu: CÂN BẰNG TẢI & DEPLOY (Thầy chấm điểm)

Đây là phần Hiếu sẽ trực tiếp mở máy demo và bảo vệ trước giảng viên:

#### 1. Cấu hình Cân bằng tải trên Apache XAMPP:
* Tạo 2 bản sao của Tomcat:
  * Node 1: Thư mục `tomcat-node-1` (chạy cổng `8080`).
  * Node 2: Thư mục `tomcat-node-2` (chạy cổng `8081`).
* Mở file cấu hình Apache của XAMPP (`C:\xampp\apache\conf\httpd.conf`), bật module:
  ```apache
  LoadModule proxy_module modules/mod_proxy.so
  LoadModule proxy_balancer_module modules/mod_proxy_balancer.so
  LoadModule proxy_http_module modules/mod_proxy_http.so
  LoadModule lbmethod_byrequests_module modules/mod_lbmethod_byrequests.so
  ```
* Khai báo cụm cân bằng tải:
  ```apache
  <Proxy "balancer://gradecalc_cluster">
      BalancerMember "http://localhost:8080/grade-calculator"
      BalancerMember "http://localhost:8081/grade-calculator"
      ProxySet lbmethod=byrequests
  </Proxy>
  ProxyPass "/grade-calculator" "balancer://gradecalc_cluster"
  ProxyPassReverse "/grade-calculator" "balancer://gradecalc_cluster"
  ```
* Bật trang quản trị `/balancer-manager` của Apache để khi báo cáo, mở trang này lên cho thầy thấy số lượng request được chia đều 50/50 giữa 2 server.

#### 2. Deploy đưa website lên mạng (Public Internet):
* Dùng công cụ **Cloudflare Tunnel** (miễn phí):
  ```powershell
  cloudflared tunnel --url http://localhost:80
  ```
* Hệ thống sẽ sinh ra một đường link HTTPS trực tiếp (ví dụ: `https://grade-dut-calculator.trycloudflare.com`). Giảng viên và các bạn trong lớp có thể cầm điện thoại truy cập vào dùng thử ngay lập tức.

---

### IV. Câu hỏi vấn đáp mẫu giúp Hiếu đạt điểm tối đa

* **Thầy hỏi:** *"Em viết Connection Pool như thế nào và tại sao cần nó?"*
* **Hiếu trả lời:**
  > *"Thưa thầy, thông thường mỗi request tạo một kết nối mới tới MySQL sẽ tốn rất nhiều thời gian bắt tay TCP và dễ làm sập DB khi có nhiều người truy cập.  
  > Em đã tự xây dựng lớp CustomConnectionPool theo mô hình Singleton, sử dụng BlockingQueue để giữ sẵn một bể từ 5 đến 15 kết nối mở sẵn. Khi DAO cần truy vấn, nó mượn kết nối từ bể, thực hiện xong câu lệnh thì trả kết nối về bể chứ không đóng lại. Nhờ đó giảm tải 90% chi phí khởi tạo kết nối."*

* **Thầy hỏi:** *"Chế độ cân bằng tải hoạt động ra sao?"*
* **Hiếu trả lời:**
  > *"Thưa thầy, em sử dụng Apache HTTP Server trong XAMPP làm Reverse Proxy đứng ở cổng 80, phía sau là cụm 2 máy chủ Tomcat độc lập chạy ở cổng 8080 và 8081. Apache sử dụng thuật toán Round-Robin (byrequests) để phân phối đồng đều các lượt truy cập sang 2 máy chủ, đảm bảo nếu một máy bị quá tải hoặc gặp sự cố thì hệ thống vẫn tiếp tục phục vụ người dùng."*
