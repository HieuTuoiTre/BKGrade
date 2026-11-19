# KẾ HOẠCH PHÂN CÔNG CÔNG VIỆC: BẠN HIẾU (TRACK B)
## Phân hệ: Bóc Tách Bảng Điểm DUT, Quản Lý Điểm, Connection Pool & Cân Bằng Tải

> **Mục tiêu chính:** 
> Làm chủ toàn bộ chu trình MVC cho phân hệ Quản lý Điểm (từ CSDL đến Giao diện JSP), xây dựng tính năng "sát thủ" tự động bóc tách bảng điểm copy từ web DUT, tự viết **Custom Connection Pool**, cấu hình **Cân bằng tải (Load Balancing)** trên Apache XAMPP và **Deploy lên mạng**.

---

### I. Danh sách các tệp tin bạn Hiếu phụ trách (Không đụng chạm với bạn Nghĩa)

```text
src/
├── main/
│   ├── java/com/gradecalc/
│   │   ├── config/
│   │   │   └── CustomConnectionPool.java   <- [TRỌNG TÂM] Tự viết Connection Pool
│   │   ├── model/
│   │   │   ├── Bean/
│   │   │   │   ├── Course.java             <- Entity môn học (JavaBean)
│   │   │   │   ├── Grade.java              <- Entity bảng điểm chi tiết (JavaBean)
│   │   │   │   └── SemesterSummary.java    <- Đối tượng tổng kết GPA/CPA (DTO/Bean)
│   │   │   ├── Bo/
│   │   │   │   ├── DutParserBO.java        <- [TÍNH NĂNG ĐỈNH] Bóc tách text copy từ DUT
│   │   │   │   └── GradeCalculatorBO.java  <- Thuật toán tính GPA hệ 10, hệ 4, xếp loại
│   │   │   └── Dao/
│   │   │       ├── CourseDAO.java          <- Tương tác DB bảng courses
│   │   │       └── GradeDAO.java           <- Tương tác DB bảng grades
│   │   └── controller/
│   │       └── GradeController.java        <- Servlet xử lý /grades, /import-grades
│   └── webapp/
│       └── WEB-INF/
│           └── views/
│               └── grades/
│                   └── index.jsp           <- Giao diện Dashboard dán bảng điểm & xem kết quả
```

---

### II. Chi tiết từng bước thực hiện

#### Bước 1: Nhiệm vụ Kỹ thuật 1: Tự viết `CustomConnectionPool.java` (Thầy chấm điểm)
* **Vị trí:** `src/main/java/com/gradecalc/config/CustomConnectionPool.java`
* **Mục tiêu:** Cấp phát và quản lý kết nối CSDL MySQL cho toàn bộ ứng dụng, giải quyết bài toán chịu tải cao.
* **Cơ chế triển khai:**
  * Áp dụng mẫu thiết kế **Singleton Pattern** (chỉ có duy nhất 1 bể kết nối trong suốt vòng đời ứng dụng).
  * Sử dụng hàng đợi an toàn đa luồng `BlockingQueue<Connection>` (hoặc `ArrayBlockingQueue<Connection>`).
  * Khởi tạo sẵn các kết nối:
    * `INITIAL_POOL_SIZE = 5` (mở sẵn 5 kết nối khi khởi động web).
    * `MAX_POOL_SIZE = 15` (tối đa 15 kết nối cùng lúc).
  * Viết 2 phương thức cốt lõi:
    * `public Connection getConnection()`: Lấy một kết nối đang rảnh ra khỏi hàng đợi. Nếu hết kết nối thì chờ (blocking) hoặc tạo mới nếu chưa vượt quá `MAX_POOL_SIZE`.
    * `public void releaseConnection(Connection conn)`: Đưa kết nối trở lại hàng đợi sau khi DAO truy vấn xong (tuyệt đối không gọi `conn.close()` để tránh làm chết kết nối).
  * Viết một hàm `main()` độc lập ngay trong file để test cấp phát và thu hồi 10 kết nối cùng lúc thành công.

#### Bước 2: Cơ sở dữ liệu (Database)
Tạo 2 bảng trong MySQL (thêm vào file `database/schema.sql`):
1. Bảng `courses` (Danh mục môn học):
   * `id`: Khóa chính tự tăng.
   * `course_code`: Mã học phần (ví dụ: `1022940.2410.24.12`).
   * `course_name`: Tên môn học (`Nhập môn ngành`, `Triết học Mác - Lênin`).
   * `credits`: Số tín chỉ (`INT`).
2. Bảng `grades` (Điểm chi tiết của từng người dùng):
   * `id`: Khóa chính tự tăng.
   * `user_id`: Khóa ngoại liên kết với bảng `users.id` của bạn Nghĩa.
   * `course_id`: Khóa ngoại liên kết với bảng `courses.id`.
   * `semester`: Học kỳ (ví dụ: `1/2024-2025`, `2/2024-2025`).
   * `formula`: Công thức tính điểm (ví dụ: `[GK]*0.30+[BT]*0.20+[CK]*0.50`).
   * `score_10`: Điểm tổng kết hệ 10 (`DOUBLE`).
   * `score_4`: Điểm hệ 4 (`DOUBLE`).
   * `grade_letter`: Điểm chữ (`VARCHAR(5)`, ví dụ: `A`, `B+`, `C`...).

#### Bước 3: Tầng Model - Bean (`Course.java`, `Grade.java`, `SemesterSummary.java`)
* **Vị trí:** `src/main/java/com/gradecalc/model/Bean/`
* Tạo 3 lớp: `Course.java`, `Grade.java`, và `SemesterSummary.java` (chứa các thuộc tính: `totalCredits`, `gpa10`, `gpa4`, `academicRank`).
* Đầy đủ thuộc tính `private`, hàm khởi tạo (constructor rỗng và đầy đủ tham số), và các hàm Getter/Setter chuẩn JavaBean.

#### Bước 4: Tầng DAO (`CourseDAO.java`, `GradeDAO.java`)
* **Vị trí:** `src/main/java/com/gradecalc/model/Dao/`
* Các hàm DAO sẽ mượn kết nối từ `CustomConnectionPool.getInstance().getConnection()` và trả lại trong khối `finally { CustomConnectionPool.getInstance().releaseConnection(conn); }`.
* Viết hàm `saveGradeList(int userId, List<Grade> list)` để lưu một danh sách điểm vừa bóc tách vào DB.
* Viết hàm `getGradesByUserId(int userId)` để lấy toàn bộ điểm của sinh viên đang đăng nhập.

#### Bước 5: Tầng BO 1: `DutParserBO.java` (Bộ não bóc tách dữ liệu DUT)
* **Vị trí:** `src/main/java/com/gradecalc/model/Bo/`
* Nhận chuỗi thô người dùng paste vào từ giao diện.
* **Quy trình thuật toán:**
  1. Tách chuỗi thành các dòng: `String[] lines = rawText.split("\\r?\\n");`.
  2. Với mỗi dòng, tách thành các cột theo ký tự Tab: `String[] cols = line.split("\\t", -1);`.
  3. Trích xuất thông tin:
     * `semester = cols[1].trim()`
     * `courseCode = cols[3].trim()`
     * `courseName = cols[4].trim()`
     * `credits = Integer.parseInt(cols[5].trim())`
     * `formula = cols[6].trim()`
  4. **Thuật toán quét ngược (Reverse Scanning) lấy điểm:**
     * Quét từ cột cuối cùng của hàng trở về trước để tìm cột chứa chữ cái xếp loại (`A+`, `A`, `B+`, `B`, `C+`, `C`, `D+`, `D`, `F`).
     * Lấy giá trị ngay trước đó làm Điểm hệ 4 (`score_4`).
     * Lấy giá trị ngay trước nữa làm Điểm hệ 10 (`score_10`).
  5. Đóng gói vào đối tượng `Grade` và trả về danh sách `List<Grade>`.

#### Bước 6: Tầng BO 2: `GradeCalculatorBO.java` (Thuật toán tính GPA chuẩn đại học)
* **Vị trí:** `src/main/java/com/gradecalc/model/Bo/`
* Tính toán GPA học kỳ và CPA tích lũy:
  $$\text{GPA} = \frac{\sum (\text{Điểm hệ 4} \times \text{Số tín chỉ})}{\sum \text{Số tín chỉ}}$$
* **Xử lý môn 0 tín chỉ:** Môn có `credits == 0` (như Giáo dục thể chất, môn thực hành gộp) **không được cộng vào mẫu số tổng số tín chỉ tích lũy**.
* Xếp loại học lực theo quy chế:
  * Xuất sắc: $\text{GPA} \ge 3.6$
  * Giỏi: $3.2 \le \text{GPA} < 3.6$
  * Khá: $2.5 \le \text{GPA} < 3.2$
  * Trung bình: $2.0 \le \text{GPA} < 2.5$
  * Yếu / Cảnh báo: $\text{GPA} < 2.0$

#### Bước 7: Tầng Controller & View (`GradeController.java` & `index.jsp`)
* **`GradeController.java`** (`@WebServlet("/grades")`):
  * `doGet()`: Lấy dữ liệu điểm hiện có của sinh viên từ DB và chuyển tiếp sang JSP.
  * `doPost()`: Nhận chuỗi text từ form -> gọi `DutParserBO` -> gọi `GradeCalculatorBO` -> lưu vào DB qua `GradeDAO` -> trả kết quả ra bảng điểm.
* **`index.jsp`:**
  * Thiết kế khung `<textarea>` lớn có hướng dẫn: *"Copy bảng điểm từ trang sv.dut.udn.vn và dán vào đây"*.
  * Bảng điểm dạng Table Bootstrap hiển thị: Mã môn, Tên môn, Số TC, Công thức, Điểm 10, Điểm 4, Điểm chữ (có màu sắc bắt mắt: A màu xanh lá, F màu đỏ).
  * 3 thẻ tóm tắt (Cards) ở trên đầu: **Tổng tín chỉ tích lũy**, **Điểm trung bình (GPA/CPA)**, **Xếp loại học lực**.

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
