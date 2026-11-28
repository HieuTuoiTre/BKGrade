# KẾ HOẠCH PHÂN CÔNG CÔNG VIỆC: BẠN NGHĨA (TRACK A)
## Phân hệ: Xác thực, Quản lý Tài khoản & Kiến trúc An ninh Bảo mật

> **Mục tiêu chính:** 
> Làm chủ toàn bộ chu trình MVC cho phân hệ Người dùng (từ CSDL đến Giao diện JSP) và phụ trách toàn bộ **Tiêu chí An ninh & Bảo mật hệ thống** theo yêu cầu bắt buộc của giảng viên.

---

### I. Danh sách các tệp tin bạn Nghĩa phụ trách (Không đụng chạm với bạn Hiếu)

```text
src/
├── main/
│   ├── java/com/gradecalc/
│   │   ├── model/
│   │   │   ├── Bean/
│   │   │   │   └── User.java                 <- Entity người dùng (JavaBean)
│   │   │   ├── Bo/
│   │   │   │   └── UserBO.java               <- Nghiệp vụ đăng nhập, đăng ký, băm BCrypt
│   │   │   └── Dao/
│   │   │       └── UserDAO.java              <- Tương tác DB bảng users
│   │   ├── controller/
│   │   │   └── AuthController.java           <- Servlet xử lý /login, /register, /logout
│   │   └── filter/
│   │       ├── AuthFilter.java               <- [TRỌNG TÂM] Chặn truy cập trái phép
│   │       └── EncodingFilter.java           <- [TRỌNG TÂM] Chuẩn hóa font UTF-8
│   └── webapp/
│       └── WEB-INF/
│           └── views/
│               └── auth/
│                   ├── login.jsp             <- Giao diện form đăng nhập
│                   ├── register.jsp          <- Giao diện form đăng ký
│                   └── profile.jsp           <- Trang xem thông tin cá nhân
```

---

### II. Chi tiết từng bước thực hiện

#### Bước 1: Cơ sở dữ liệu (Database)
Tạo bảng `users` trong MySQL (thêm vào file `database/schema.sql`):
* Các trường:
  * `id`: Khóa chính, tự tăng (`INT AUTO_INCREMENT PRIMARY KEY`).
  * `username`: Tên đăng nhập (`VARCHAR(50) UNIQUE NOT NULL`).
  * `password`: Mật khẩu đã băm BCrypt (`VARCHAR(255) NOT NULL`).
  * `full_name`: Họ và tên sinh viên (`VARCHAR(100)`).
  * `student_code`: Mã sinh viên (ví dụ: `102220xxx`).
  * `role`: Vai trò (`VARCHAR(20) DEFAULT 'STUDENT'`).
  * `created_at`: Thời gian tạo tài khoản.

#### Bước 2: Tầng Model - Bean (`User.java`)
* **Vị trí:** `src/main/java/com/gradecalc/model/Bean/User.java`
* Tạo các thuộc tính tương ứng với bảng `users`: `id`, `username`, `password`, `fullName`, `studentCode`, `role`, `createdAt`.
* Viết đầy đủ Constructor không tham số, Constructor đầy đủ tham số, và các hàm `getter/setter`.
* Lưu ý: Không lưu mật khẩu thô trong Model khi đã xác thực xong.

#### Bước 3: Tầng DAO (`UserDAO.java`) — Tiêu chuẩn chống SQL Injection
* **Vị trí:** `src/main/java/com/gradecalc/model/Dao/UserDAO.java`
* Viết các phương thức tương tác CSDL bằng **100% `PreparedStatement`** (mượn connection từ `CustomConnectionPool`):
  * `save(User user)`: Thêm tài khoản mới vào database.
  * `findByUsername(String username)`: Tìm kiếm user theo tên đăng nhập.
  * `checkUsernameExists(String username)`: Kiểm tra trùng lặp username.

#### Bước 4: Tầng BO (`UserBO.java`) — Xử lý nghiệp vụ & Bảo mật BCrypt
* **Vị trí:** `src/main/java/com/gradecalc/model/Bo/UserBO.java`
* Tách biệt nghiệp vụ khỏi Controller và DAO:
  * **Hàm `register(User user, String rawPassword)`:**
    * Kiểm tra username đã tồn tại chưa (gọi `UserDAO.checkUsernameExists()`).
    * Băm mật khẩu bằng thư viện `BCrypt`:
      ```java
      String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
      user.setPassword(hashedPassword);
      ```
    * Gọi `UserDAO.save(user)`.
  * **Hàm `login(String username, String rawPassword)`:**
    * Gọi `UserDAO.findByUsername(username)`.
    * So khớp mật khẩu:
      ```java
      if (user != null && BCrypt.checkpw(rawPassword, user.getPassword())) {
          return user; // Đăng nhập thành công
      }
      return null; // Sai mật khẩu hoặc không tồn tại
      ```

#### Bước 5: Tầng Controller (`AuthController.java`)
* Khai báo Servlet ánh xạ tới các URL: `@WebServlet(urlPatterns = {"/login", "/register", "/logout", "/profile"})`.
* **Phương thức `doGet()`:**
  * Nếu vào `/login`: `request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);`.
  * Nếu vào `/register`: forward sang `register.html`.
  * Nếu vào `/logout`: Hủy session (`session.invalidate()`) và redirect về trang `/login`.
* **Phương thức `doPost()`:**
  * Nhận `username`, `password` từ form.
  * Gọi `UserBO.login()`:
    * Nếu thành công: Tạo phiên `HttpSession session = request.getSession();` và lưu `session.setAttribute("currentUser", user);`, sau đó redirect tới `/grades`.
    * Nếu thất bại: `request.setAttribute("errorMessage", "Sai tên đăng nhập hoặc mật khẩu!");` và forward lại trang đăng nhập.

#### Bước 6: Tầng View JSP (`login.jsp`, `register.html`, `profile.jsp`)
* Sử dụng thư viện JSTL: `<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>`.
* Dùng Bootstrap 5 để thiết kế giao diện form căn giữa màn hình, hiện đại và thân thiện trên điện thoại.
* **Tái sử dụng Layout dùng chung do Hiếu xây dựng (`views/layout/`):**
  * Nhúng thanh điều hướng vào đầu trang:
    ```jsp
    <jsp:include page="/WEB-INF/views/layout/navbar.jsp" />
    ```
  * Nhúng chân trang vào cuối trang:
    ```jsp
    <jsp:include page="/WEB-INF/views/layout/footer.jsp" />
    ```
  *(Giúp giao diện Đăng nhập, Đăng ký đồng bộ hoàn toàn với Trang chủ và Dashboard)*.
* Hiển thị thông báo lỗi đẹp mắt nếu có:
  ```jsp
  <c:if test="${not empty errorMessage}">
      <div class="alert alert-danger">${errorMessage}</div>
  </c:if>
  ```

---

### III. Nhiệm vụ Kỹ thuật trọng tâm của Nghĩa: AN NINH & BẢO MẬT (Thầy chấm điểm)

Đây là phần Nghĩa sẽ trực tiếp đứng ra bảo vệ trước giảng viên:

1. **Bộ lọc kiểm soát truy cập (`AuthFilter.java`):**
   * Ánh xạ bảo vệ các đường dẫn nhạy cảm: `@WebFilter(urlPatterns = {"/grades/*", "/profile/*"})`.
   * Logic kiểm tra:
     * Lấy `HttpSession` hiện tại.
     * Kiểm tra `session.getAttribute("currentUser") == null` thì lập tức chuyển hướng người dùng về `/login?error=unauthorized`, không cho xem trộm điểm.
2. **Bộ lọc bảng mã tiếng Việt (`EncodingFilter.java`):**
   * Ánh xạ toàn bộ trang web: `@WebFilter(urlPatterns = {"/*"})`.
   * Ép mã: `request.setCharacterEncoding("UTF-8");` và `response.setCharacterEncoding("UTF-8");`.
   * Đảm bảo tên sinh viên, tên môn học tiếng Việt có dấu không bao giờ bị lỗi font `???`.
3. **Chống tấn công XSS (Cross-Site Scripting):**
   * Trong các file JSP, khi in tên người dùng hoặc dữ liệu nhập vào, luôn dùng thẻ `<c:out value="${user.fullName}" />` thay vì viết thẳng `${user.fullName}` để triệt tiêu các đoạn mã JavaScript độc hại.
4. **Bảo mật mật khẩu:**
   * Chứng minh cho thầy thấy trong CSDL MySQL, mật khẩu được lưu dưới dạng chuỗi băm BCrypt `$2a$12$...` chứ không lưu dạng bản rõ (plaintext).

---

### IV. Câu hỏi vấn đáp mẫu giúp Nghĩa đạt điểm tối đa

* **Thầy hỏi:** *"Em đã làm gì để bảo mật cho hệ thống chương trình?"*
* **Nghĩa trả lời:**
  > *"Thưa thầy, em đã triển khai 4 lớp phòng thủ:  
  > 1. Tầng dữ liệu: 100% câu truy vấn trong DAO dùng PreparedStatement để chống SQL Injection.  
  > 2. Mật khẩu người dùng được băm một chiều bằng giải thuật BCrypt với Salt độ phức tạp 12, đảm bảo nếu lộ CSDL cũng không thể giải mã.  
  > 3. Tầng mạng: Em dùng AuthFilter để chặn toàn bộ request trái phép vào phân hệ tính điểm khi chưa có Session hợp lệ.  
  > 4. Tầng giao diện: Dùng JSTL c:out để escape toàn bộ ký tự đặc biệt, ngăn ngừa tấn công XSS."*
