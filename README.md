# 🎓 DUT Grade Calculator (BKGrade Calc)

> **Hệ thống Quản lý Bảng điểm, Bóc tách Điểm tự động từ sv.dut.udn.vn & Bộ mô phỏng Dự đoán Mục tiêu GPA dành riêng cho sinh viên Đại học Bách Khoa - Đại học Đà Nẵng (DUT).**

---

## 📌 Mục lục
1. [Giới thiệu dự án](#-giới-thiệu-dự-án)
2. [Tính năng nổi bật](#-tính-năng-nổi-bật)
3. [Công nghệ sử dụng](#-công-nghệ-sử-dụng-tech-stack)
4. [Cấu trúc thư mục](#-cấu-trúc-thư-mục-dự-án)
5. [Quy chế tính điểm chuẩn DUT](#-quy-chế-tính-điểm-chuẩn-dut)
6. [Hướng dẫn cài đặt & Chạy ứng dụng](#-hướng-dẫn-cài-đặt--chạy-ứng-dụng)
7. [Hướng dẫn sử dụng chi tiết](#-hướng-dẫn-sử-dụng-chi-tiết)
8. [Quy trình phát triển & Git Workflow](#-quy-trình-phát-triển--git-workflow)

---

## 📖 Giới thiệu dự án

**BKGrade Calc** ra đời nhằm giải quyết những khó khăn thực tế của sinh viên Bách Khoa Đà Nẵng trong việc theo dõi điểm học tập:
* Trang tín chỉ `sv.dut.udn.vn` có giao diện điểm phân tán, không có công cụ tính ngược xem thi cuối kỳ cần mấy điểm để qua môn hay đạt A/B+.
* Điểm CPA toàn khóa tính toán phức tạp do mỗi môn có công thức trọng số khác nhau ([GK], [BT], [CK], [QT], [TH], [DA], [BV]) và các môn 0 tín chỉ (GDTC, GDQP) không tham gia tính điểm.
* Sinh viên thiếu một công cụ mô phỏng giả lập: *"Nếu kỳ này mình được các điểm này thì CPA sẽ nhảy lên bao nhiêu? Cần học những môn nào để kéo CPA lên 3.2 (Bằng Giỏi)?"*.

Hệ thống **BKGrade Calc** cung cấp giải pháp toàn diện từ bóc tách dữ liệu 1 chạm, quản lý điểm, tính toán CPA chuẩn quy chế, cho đến bộ mô phỏng chiến lược điểm số thời gian thực.

---

## 🚀 Tính năng nổi bật

### 1. ⚡ Bóc tách bảng điểm thông minh 1 chạm (DUT Smart Parser)
* Không cần nhập liệu thủ công từng môn!
* Chỉ cần vào trang `sv.dut.udn.vn` $\to$ **Kết quả học tập** $\to$ Quét chuột tô các dòng và bấm `Ctrl + C` $\to$ Dán vào hệ thống.
* Thuật toán bóc tách tự động nhận diện: Tên môn, Số tín chỉ, Học kỳ, 7 cột điểm thành phần (`BT`, `BV`, `CK`, `DA`, `GK`, `QT`, `TH`), Công thức môn học và Trạng thái môn đang học.

### 2. 🧮 Tính điểm CPA/GPA chuẩn quy chế đào tạo tín chỉ DUT
* Tự động nhận diện công thức điểm (ví dụ: `[GK]*0.20+[BT]*0.20+[CK]*0.60`, `[QT]*0.40+[BV]*0.60`...).
* Tự động loại trừ các môn 0 tín chỉ (Giáo dục thể chất, Giáo dục quốc phòng) và các môn chưa có kết quả (đang học) khỏi tử số và mẫu số khi tính CPA tích lũy.
* Quy đổi chính xác từ Thang điểm 10 $\to$ Thang điểm 4 $\to$ Điểm chữ ($A, B+, B, C+, C, D+, D, F$) theo đúng quy chế DUT.
* Bảng thống kê chi tiết theo từng học kỳ và toàn khóa (Tổng TC tích lũy, CPA hệ 4, CPA hệ 10, Xếp loại học lực).

### 3. 🎯 Dự đoán điểm thành phần môn học (Course Target Solver)
* Trả lời câu hỏi: *"Môn này đã có GK = 8, BT = 7, vậy thi cuối kỳ (CK) cần tối thiểu mấy điểm để được A, B+, B...?"*.
* Tự động giải phương trình trọng số tuyến tính và hiển thị bảng điểm CK cần đạt tương ứng từng mốc điểm chữ.
* **1-Click Autofill:** Bấm trực tiếp vào mốc điểm mong muốn $\to$ Hệ thống tự điền điểm thi CK dự kiến và tự cập nhật điểm tổng kết.

### 4. 🪄 Bộ mô phỏng mục tiêu GPA thời gian thực (GPA Goal Sandbox)
* **Chế độ Thử nghiệm an toàn (Sandbox):** Thử nghiệm hàng chục kịch bản điểm số trong RAM trình duyệt mà không làm sai lệch bảng điểm thật trong CSDL.
* **Live KPI:** Kéo chọn điểm chữ ($A \to F$) cho các môn đang học $\to$ Thước đo tiến độ và CPA dự phóng nhảy số tức thì (< 10ms độ trễ).
* **Smart Optimizer (Gợi ý kịch bản tối ưu):**
  * Nhập mốc CPA mục tiêu (ví dụ `3.20` - Bằng Giỏi, `3.60` - Bằng Xuất sắc) $\to$ Bấm *"Gợi ý kịch bản"*.
  * Hệ thống tự động sinh 2 kịch bản:
    * **Kịch bản Đồng đều (An toàn):** Phấn đấu điểm đồng đều cho tất cả các môn.
    * **Kịch bản Tối ưu tín chỉ (Chiến lược):** Dồn sức lấy A/B+ cho các môn 3-4 tín chỉ, giảm tải cho các môn 1-2 tín chỉ.
* **Lưu CSDL khi muốn:** Bấm *"Lưu điểm dự kiến vào CSDL"* để cập nhật chính thức vào bảng điểm khi đã hài lòng với kịch bản.

### 5. 🛠️ Quản lý học phần linh hoạt
* Thêm môn học thủ công với các mẫu công thức phổ biến (Chuẩn 20-20-60, PBL 30-70, PBL 40-60, 100% Cuối kỳ...).
* Chỉnh sửa điểm số, sửa công thức hoặc xóa môn học nhanh chóng.

---

## 💻 Công nghệ sử dụng (Tech Stack)

| Tầng (Layer) | Công nghệ | Chi tiết |
| :--- | :--- | :--- |
| **Backend** | Java Servlet / JSP (Java 21) | `HttpServlet`, JSP 2.3, JSTL 1.2 |
| **Web Server** | Apache Tomcat 9.0.x | Servlet API 4.0 (gói `javax.servlet`) |
| **Cơ sở dữ liệu** | MySQL 8.0 / TiDB Cloud | TiDB Cloud Serverless MySQL |
| **Connection Pool** | Custom Connection Pool | `CustomConnectionPool.java` có kiểm tra `conn.isValid(2)` tự động hồi phục kết nối |
| **Build Tool** | Apache Maven | Maven Wrapper (`mvnw`), Packaging `WAR` |
| **Frontend** | Bootstrap 5.3.3 + Vanilla JS | Bootstrap Nav-Pills, FontAwesome 6, Module JS ES6 (`dashboard.js`, `simulator.js`) |

---

## 📁 Cấu trúc thư mục dự án

```
grade-calculator/
├── src/
│   └── main/
│       ├── java/com/gradecalc/
│       │   ├── config/
│       │   │   └── CustomConnectionPool.java       # Bộ quản lý kết nối CSDL (Connection Pool)
│       │   ├── controller/
│       │   │   ├── AuthController.java             # Quản lý đăng nhập/đăng ký (Track A)
│       │   │   └── GradeController.java            # Điều hướng import, sửa, xóa, mô phỏng (Track B)
│       │   ├── filter/
│       │   │   └── AuthFilter.java                 # Bộ lọc phân quyền đăng nhập
│       │   └── model/
│       │       ├── Bean/
│       │       │   ├── Course.java                 # Entity Môn học (tên môn, tín chỉ)
│       │       │   ├── Grade.java                  # Entity Điểm (7 cột thành phần, điểm 10, 4, chữ)
│       │       │   ├── SemesterSummary.java        # Entity Thống kê điểm kỳ / toàn khóa
│       │       │   └── User.java                   # Entity Người dùng
│       │       ├── Bo/
│       │       │   ├── DutParserBO.java            # Thuật toán bóc tách dữ liệu từ text DUT
│       │       │   └── GradeCalculatorBO.java      # Thuật toán tính điểm & công thức quy chế DUT
│       │       └── Dao/
│       │           ├── CourseDAO.java              # Thao tác CSDL bảng courses
│       │           ├── GradeDAO.java               # Thao tác CSDL bảng grades
│       │           └── UserDAO.java                # Thao tác CSDL bảng users
│       └── webapp/
│           ├── assets/
│           │   ├── css/style.css                   # Custom CSS, hiệu ứng hover, thẻ điểm
│           │   └── js/
│           │       ├── dashboard.js                # Xử lý modal sửa điểm & tương tác bảng
│           │       └── simulator.js                # Engine dự đoán điểm & bộ mô phỏng GPA Sandbox
│           └── WEB-INF/
│               ├── views/
│               │   ├── auth/                       # Trang login, register
│               │   └── grades/
│               │       └── dashboard.jsp           # Giao diện chính: Bảng điểm & Tab Sandbox
│               └── web.xml                         # Cấu hình Servlet & URL mapping
├── docs/                                           # Tài liệu đặc tả thiết kế & kế hoạch
├── pom.xml                                         # Cấu hình Maven dependencies
└── README.md                                       # Tài liệu hướng dẫn dự án
```

---

## 📐 Quy chế tính điểm chuẩn DUT

Hệ thống áp dụng nghiêm ngặt quy chế đào tạo theo hệ thống tín chỉ của Đại học Bách Khoa - ĐHĐN:

### 1. Bảng quy đổi Thang điểm 10 sang Thang điểm 4 & Điểm chữ
| Điểm Thang 10 | Điểm Thang 4 | Điểm chữ | Xếp loại tương ứng |
| :---: | :---: | :---: | :--- |
| **8.5 – 10.0** | **4.0** | **A** | Giỏi / Xuất sắc |
| **8.0 – 8.4** | **3.5** | **B+** | Khá giỏi |
| **7.0 – 7.9** | **3.0** | **B** | Khá |
| **6.5 – 6.9** | **2.5** | **C+** | Trung bình khá |
| **5.5 – 6.4** | **2.0** | **C** | Trung bình |
| **5.0 – 5.4** | **1.5** | **D+** | Trung bình yếu |
| **4.0 – 4.9** | **1.0** | **D** | Yếu (Vừa đủ qua môn) |
| **< 4.0** | **0.0** | **F** | Kém (Phải học lại) |

### 2. Quy tắc tính CPA tích lũy
$$CPA = \frac{\sum (Tín\_chỉ_i \times Điểm\_Hệ\_4_i)}{\sum Tín\_chỉ_i}$$
* **Ngoại lệ:** Môn 0 tín chỉ (GDTC, GDQP) và các môn chưa có điểm tổng kết (đang học) **không tính** vào cả tử số lẫn mẫu số.

---

## 🛠️ Hướng dẫn cài đặt & Chạy ứng dụng

### 1. Yêu cầu môi trường
* **Java SDK:** Phiên bản 21 trở lên (khuyến nghị JDK 21).
* **Apache Tomcat:** Phiên bản 9.0.x (ví dụ: `C:\apache-tomcat-9.0.123`).
* **Maven:** Đã tích hợp sẵn Maven Wrapper (`mvnw`), không bắt buộc cài Maven ngoài.

### 2. Cấu hình CSDL
Dự án sử dụng Connection Pool cấu hình tại file `src/main/java/com/gradecalc/config/CustomConnectionPool.java`. Đảm bảo các thông số kết nối (URL, User, Password) trỏ đúng đến MySQL hoặc TiDB Cloud của bạn.

### 3. Mẹo cấu hình Tomcat chạy trực tiếp (Không cần copy/paste file WAR)
Để chạy code mới tức thì mỗi khi compile mà không cần copy file `.war` vào thư mục `webapps` của Tomcat:
1. Tạo file Context XML tại:
   `C:\apache-tomcat-9.0.123\conf\Catalina\localhost\grade-calculator.xml`
2. Điền nội dung sau (trỏ đường dẫn đến thư mục `target\grade-calculator` của dự án):
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <Context docBase="C:\Users\hieu8\OneDrive\Documents\Github\CNW\grade-calculator\target\grade-calculator" reloadable="true">
   </Context>
   ```
3. Xóa thư mục hoặc file `grade-calculator.war` cũ trong `C:\apache-tomcat-9.0.123\webapps` (nếu có).

### 4. Biên dịch dự án
Mỗi khi chỉnh sửa mã nguồn, mở Terminal tại thư mục gốc dự án và chạy:
```powershell
.\mvnw compile war:exploded
```
*(Nếu có sửa đổi class Java, bạn có thể chạy `.\mvnw clean compile war:exploded`)*.

### 5. Khởi động Tomcat & Truy cập
1. Khởi động Tomcat bằng cách chạy:
   ```cmd
   C:\apache-tomcat-9.0.123\bin\startup.bat
   ```
2. Mở trình duyệt và truy cập:
   👉 **`http://localhost:8080/grade-calculator/grades`**

---

## 📱 Hướng dẫn sử dụng chi tiết

### 1. Cách bóc tách bảng điểm từ `sv.dut.udn.vn`
1. Đăng nhập vào trang sinh viên DUT: `http://sv.dut.udn.vn`.
2. Vào mục **Kết quả học tập** $\to$ **Xem điểm học phần**.
3. Dùng chuột bôi đen toàn bộ các dòng bảng điểm (chọn từ tiêu đề cột hoặc từ dòng môn đầu tiên đến hết).
4. Nhấn `Ctrl + C` để copy.
5. Quay lại trang BKGrade Calc, dán toàn bộ vào ô text **"Bóc tách bảng điểm từ DUT"** rồi bấm **"Bóc tách & Lưu điểm"**.
6. Hệ thống sẽ tự động bóc tách và lưu tất cả các môn vào bảng điểm của bạn!

---

### 2. Cách xem dự đoán điểm thi Cuối kỳ (CK) cần đạt
1. Tại **Tab 1: Bảng điểm chính thức**, tìm môn học bạn đang muốn dự đoán.
2. Bấm vào biểu tượng cây bút sửa điểm (<i class="fa-solid fa-pen-to-square"></i>) ở cột Thao tác.
3. Nhập các điểm thành phần đã biết (ví dụ: GK = 8.0, BT = 7.5...).
4. Ngay bên dưới sẽ xuất hiện khung **"Dự đoán điểm thi Cuối kỳ [CK] cần đạt"**:
   - Thấy ngay: Cần CK $\ge$ bao nhiêu điểm để được A, B+, B, C+...
   - Nếu kết quả ghi *Chắc chắn đạt*: Bạn chỉ cần đi thi là đạt mốc đó.
   - Nếu kết quả ghi *Không thể đạt*: Dù CK được 10 cũng không đủ kéo lên mốc này.
5. **Bấm trực tiếp vào ô điểm chữ mong muốn** $\to$ Hệ thống sẽ tự điền điểm số đó vào ô CK và tính toán tự động!

---

### 3. Cách sử dụng Bộ mô phỏng mục tiêu GPA (Sandbox)
1. Trên thanh điều hướng, bấm sang **Tab 2: `🎯 Dự đoán & Mô phỏng mục tiêu GPA`**.
2. **Thử nghiệm What-if thủ công:**
   - Tại danh sách các môn đang học, chọn thử điểm chữ dự kiến ở cột **"Điểm chữ giả định"** (ví dụ chọn A cho môn 3 tín chỉ, B+ cho môn 2 tín chỉ).
   - Quan sát ô **CPA dự phóng** ở trên cùng: Số điểm CPA sẽ lập tức nhảy theo thời gian thực kèm mức chênh lệch ($\pm$) và thanh tiến độ.
3. **Gợi ý kịch bản tự động:**
   - Nhập mức CPA mong muốn vào ô **"Mục tiêu CPA"** (ví dụ: `3.20`).
   - Bấm nút **"🪄 Gợi ý kịch bản tối ưu để đạt mục tiêu"**.
   - Hệ thống hiển thị 2 kịch bản: *Kịch bản Đồng đều* và *Kịch bản Tối ưu tín chỉ*.
   - Bấm nút **"Áp dụng kịch bản"** $\to$ Các môn học tự động được điền điểm gợi ý.
4. **Lưu kết quả:**
   - Khi bạn đã có được kịch bản điểm số ưng ý, bấm nút **"💾 Lưu điểm dự kiến vào CSDL"**. Điểm dự kiến này sẽ được lưu thành điểm chính thức trong CSDL.

---

## 👥 Quy trình phát triển & Git Workflow

Dự án được phân chia thành 2 phân hệ rõ ràng để tránh xung đột mã nguồn:

* **Track A:** Phân hệ Xác thực, Đăng nhập, Đăng ký, Phân quyền người dùng (`User*`, `Auth*`, `login.jsp`, `register.jsp`).
* **Track B (Nhánh `hieu-sub`):** Phân hệ Quản lý điểm, Bóc tách điểm DUT Parser, Thuật toán tính điểm và Bộ mô phỏng GPA Simulator (`Grade*`, `Course*`, `DutParserBO`, `GradeCalculatorBO`, `dashboard.jsp`, `simulator.js`, `dashboard.js`).

### Các lệnh Git cơ bản khi làm việc trên nhánh:
```powershell
# Kiểm tra trạng thái
git status

# Thêm thay đổi
git add .

# Tạo commit
git commit -m "feat: your commit message"

# Đẩy code lên nhánh hieu-sub
git push origin hieu-sub
```

---

## 📄 Bản quyền & Tác giả
- **Đồ án môn học:** Công nghệ Web (CNW) - Trường Đại học Bách Khoa, Đại học Đà Nẵng.
- **Tác giả phát triển:** Hiếu & Nhóm phát triển BKGrade.
- © 2026 DUT Grade Calculator. All rights reserved.
