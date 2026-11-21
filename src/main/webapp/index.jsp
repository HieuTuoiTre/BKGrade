<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DUT Grade Calculator - Tính Điểm Tự Động</title>
    <!-- Nhúng Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Nhúng FontAwesome Icon (tùy chọn) -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
</head>
<body class="bg-light">

    <!-- 1. THANH MENU (NAVBAR) -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary sticky-top shadow-sm">
        <div class="container">
            <a class="navbar-brand fw-bold" href="#">
                <i class="fa-solid fa-graduation-cap me-2"></i> BKGrade Calc
            </a>
            <div class="ms-auto">
                <a href="login" class="btn btn-outline-light me-2">Đăng nhập</a>
                <a href="register" class="btn btn-warning fw-bold">Đăng ký</a>
            </div>
        </div>
    </nav>

    <!-- 2. HERO BANNER -->
    <header class="py-5 bg-white text-center border-bottom">
        <div class="container">
            <h1 class="display-5 fw-bold text-primary mb-3">Tính Điểm Học Tập BKGrade Calc</h1>
            <p class="lead text-muted mx-auto" style="max-width: 700px;">
                Hệ thống hỗ trợ tính điểm học tập, tương thích tốt với sinh viên DUT. Thao tác đơn giản và nhanh chóng.
            </p>
            <div class="mt-4">
                <a href="login" class="btn btn-primary btn-lg px-4 me-2 shadow">
                    <i class="fa-solid fa-bolt me-2"></i>Trải nghiệm ngay
                </a>
                <a href="#guide-video" class="btn btn-outline-secondary btn-lg px-4">
                    <i class="fa-solid fa-play me-2"></i>Xem hướng dẫn
                </a>
            </div>
        </div>
    </header>

    <!-- 3. KHỐI VIDEO HƯỚNG DẪN (Ở GIỮA) -->
    <section id="guide-video" class="py-5 bg-light">
        <div class="container text-center">
            <h2 class="fw-bold mb-3">Cách Sử Dụng Đơn Giản</h2>
            <p class="text-muted mb-4">Xem video 30 giây dưới đây để biết cách sử dụng</p>
            
            <div class="row justify-content-center">
                <div class="col-lg-8">
                    <!-- Khung video tỉ lệ chuẩn 16:9 của Bootstrap -->
                    <div class="ratio ratio-16x9 shadow-lg rounded-4 overflow-hidden border">
                        <!-- Cách 1: Video mp4 đặt trong thư mục assets/videos/ -->
                        <video controls poster="https://placehold.co/800x450?text=Bam+de+xem+huong+dan">
                            <source src="assets/videos/huong-dan-copy-dut.mp4" type="video/mp4">
                            Trình duyệt của bạn không hỗ trợ thẻ video.
                        </video>

                        <!-- Cách 2: Nếu bạn tải video lên YouTube thì dùng iframe:
                        <iframe src="https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=RDdQw4w9WgXcQ&start_radio=1" title="Hướng dẫn" allowfullscreen></iframe>
                        -->
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- 4. THÔNG TIN NHÓM TÁC GIẢ -->
    <section class="py-5 bg-white border-top">
        <div class="container">
            <h2 class="text-center fw-bold mb-5">Đội Ngũ Phát Triển Dự Án</h2>
            <div class="row g-4 justify-content-center">
                <!-- Thẻ của bạn Hiếu -->
                <div class="col-md-5 col-lg-4">
                    <div class="card h-100 shadow-sm border-0 text-center p-3">
                        <div class="card-body">
                            <div class="bg-primary text-white rounded-circle d-inline-flex justify-content-center align-items-center mb-3" style="width: 80px; height: 80px;">
                                <i class="fa-solid fa-code fa-2x"></i>
                            </div>
                            <h5 class="card-title fw-bold">Nguyễn Hoàng Hiếu</h5>
                            <p class="text-primary mb-2 fw-semibold">Track A - Algorithm & Infrastructure</p>
                            <p class="card-text text-muted small">
                                Phụ trách: Thuật toán bóc tách dữ liệu DUT, Bộ nhớ đệm Connection Pool, Cân bằng tải Apache & Cloudflare.
                            </p>
                        </div>
                    </div>
                </div>

                <!-- Thẻ của bạn Nghĩa -->
                <div class="col-md-5 col-lg-4">
                    <div class="card h-100 shadow-sm border-0 text-center p-3">
                        <div class="card-body">
                            <div class="bg-success text-white rounded-circle d-inline-flex justify-content-center align-items-center mb-3" style="width: 80px; height: 80px;">
                                <i class="fa-solid fa-shield-halved fa-2x"></i>
                            </div>
                            <h5 class="card-title fw-bold">Huỳnh Hiếu Nghĩa</h5>
                            <p class="text-success mb-2 fw-semibold">Track B - Security & Authentication</p>
                            <p class="card-text text-muted small">
                                Phụ trách: Hệ thống tài khoản người dùng, Mã hóa mật khẩu BCrypt, Kiến trúc bảo mật đa tầng Filter.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="text-center text-muted mt-4">
                <small>Khoa Công nghệ Thông tin — Trường Đại học Bách Khoa, Đại học Đà Nẵng</small>
            </div>
        </div>
    </section>

    <!-- 5. CHÂN TRANG (FOOTER) -->
    <footer class="py-4 bg-dark text-white text-center">
        <div class="container">
            <p class="mb-0 small">© 2026 BKGrade Calc. Dự án môn học Công nghệ Web.</p>
        </div>
    </footer>

    <!-- Nhúng Bootstrap 5 JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>