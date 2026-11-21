<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng Điểm Học Tập - BKGrade Calc</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- FontAwesome Icon -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <style>
        .card-stat {
            transition: transform 0.2s;
            border-left: 5px solid;
        }
        .card-stat:hover {
            transform: translateY(-3px);
        }
        .badge-grade {
            font-size: 0.85rem;
            padding: 0.35em 0.65em;
        }
        .table-grades th, .table-grades td {
            vertical-align: middle;
            white-space: nowrap;
            padding: 0.55rem 0.4rem;
        }
        .col-comp {
            width: 42px;
            min-width: 38px;
            text-align: center;
            padding: 0.55rem 0.2rem !important;
            font-size: 0.88rem;
        }
        .text-null {
            color: #adb5bd;
            font-weight: normal;
        }
        .formula-pill {
            background-color: #f1f3f5;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 0.78rem;
            color: #495057;
            display: inline-block;
            max-width: 220px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    </style>
</head>
<body class="bg-light d-flex flex-column min-vh-100">

    <!-- 1. THANH MENU (NAVBAR) -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
        <div class="container-fluid px-lg-4">
            <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/">
                <i class="fa-solid fa-graduation-cap me-2"></i>BKGrade Calc
            </a>
            <div class="ms-auto d-flex align-items-center">
                <span class="text-white me-3 d-none d-md-inline">
                    <i class="fa-solid fa-circle-user me-1"></i> Sinh viên DUT
                </span>
                <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-light btn-sm">
                    <i class="fa-solid fa-right-from-bracket me-1"></i>Đăng xuất
                </a>
            </div>
        </div>
    </nav>

    <!-- NỘI DUNG CHÍNH (Dùng container-fluid để hiển thị trọn vẹn mọi cột) -->
    <main class="container-fluid px-lg-4 px-3 my-4 flex-grow-1">

        <!-- 2. THÔNG BÁO (LỖI / THÀNH CÔNG) -->
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
                <i class="fa-solid fa-triangle-exclamation me-2"></i>${errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
                <i class="fa-solid fa-circle-check me-2"></i>${successMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- 3. KHỐI THỐNG KÊ (3 CARDS) -->
        <div class="row g-3 mb-4">
            <!-- Thẻ Tổng tín chỉ -->
            <div class="col-md-4">
                <div class="card card-stat shadow-sm border-0 border-primary h-100">
                    <div class="card-body d-flex align-items-center">
                        <div class="bg-primary text-white rounded-circle p-3 me-3">
                            <i class="fa-solid fa-book-bookmark fa-2x"></i>
                        </div>
                        <div>
                            <div class="text-muted small fw-semibold text-uppercase">Tín chỉ tích lũy (CPA)</div>
                            <h3 class="fw-bold mb-0 text-primary">
                                <fmt:formatNumber value="${not empty totalSummary ? totalSummary.totalCredits : 0}" pattern="#.#"/> 
                                <span class="fs-6 text-muted fw-normal">TC</span>
                            </h3>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Thẻ Điểm TB (GPA) -->
            <div class="col-md-4">
                <div class="card card-stat shadow-sm border-0 border-warning h-100">
                    <div class="card-body d-flex align-items-center">
                        <div class="bg-warning text-dark rounded-circle p-3 me-3">
                            <i class="fa-solid fa-chart-line fa-2x"></i>
                        </div>
                        <div>
                            <div class="text-muted small fw-semibold text-uppercase">Điểm trung bình (CPA)</div>
                            <h3 class="fw-bold mb-0 text-warning">
                                ${not empty totalSummary ? totalSummary.gpa4 : 0.0} 
                                <span class="fs-6 text-muted fw-normal">/ 4.0 (${not empty totalSummary ? totalSummary.gpa10 : 0.0} / 10)</span>
                            </h3>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Thẻ Xếp loại học lực -->
            <div class="col-md-4">
                <div class="card card-stat shadow-sm border-0 border-success h-100">
                    <div class="card-body d-flex align-items-center">
                        <div class="bg-success text-white rounded-circle p-3 me-3">
                            <i class="fa-solid fa-award fa-2x"></i>
                        </div>
                        <div>
                            <div class="text-muted small fw-semibold text-uppercase">Xếp loại học lực</div>
                            <h3 class="fw-bold mb-0 text-success">${not empty totalSummary ? totalSummary.academicRank : 'Chưa có'}</h3>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 4. KHỐI NHẬP LIỆU (DÁN BẢNG ĐIỂM) -->
        <div class="card shadow-sm border-0 mb-4">
            <div class="card-header bg-white py-3">
                <h5 class="fw-bold mb-0 text-primary">
                    <i class="fa-solid fa-paste me-2"></i>Bóc tách bảng điểm từ DUT
                </h5>
            </div>
            <div class="card-body">
                <form action="grades" method="post">
                    <input type="hidden" name="action" value="import">
                    <div class="mb-3">
                        <label for="rawDutText" class="form-label text-muted">
                            Vào <code>sv.dut.udn.vn</code> ➔ <strong>Kết quả học tập</strong> ➔ Quét chuột tô các dòng (hỗ trợ cả các môn đang học và 7 cột điểm thành phần) ➔ <code>Ctrl + C</code> và dán vào đây:
                        </label>
                        <textarea class="form-control font-monospace" id="rawDutText" name="rawDutText" rows="4" 
                                  placeholder="Dán toàn bộ các dòng copy từ bảng điểm sv.dut.udn.vn vào đây..." required></textarea>
                    </div>
                    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                        <small class="text-muted">
                            <i class="fa-solid fa-circle-info me-1"></i>Tự động tính điểm theo công thức, bỏ qua môn 0 TC & môn chưa có kết quả khi tính CPA tích lũy.
                        </small>
                        <button type="submit" class="btn btn-primary px-4 fw-semibold shadow-sm">
                            <i class="fa-solid fa-calculator me-1"></i>Bóc tách & Lưu điểm
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- 5. KHỐI BẢNG ĐIỂM CHI TIẾT -->
        <div class="card shadow-sm border-0 mb-4">
            <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
                <div class="d-flex align-items-center">
                    <h5 class="fw-bold mb-0 text-dark me-2">
                        <i class="fa-solid fa-table-list me-2 text-primary"></i>Chi tiết các môn học
                    </h5>
                    <span class="badge bg-secondary">${not empty gradeList ? gradeList.size() : 0} môn</span>
                </div>
                <div>
                    <button type="button" class="btn btn-outline-primary btn-sm fw-semibold" data-bs-toggle="modal" data-bs-target="#addCourseModal">
                        <i class="fa-solid fa-plus me-1"></i>Thêm môn học thủ công
                    </button>
                </div>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover table-striped align-middle mb-0 table-grades">
                        <thead class="table-light text-uppercase small text-muted text-center">
                            <tr>
                                <th style="width: 40px;">STT</th>
                                <th>Học kỳ</th>
                                <th class="text-start">Tên môn học</th>
                                <th>Số TC</th>
                                <th>Công thức</th>
                                <th class="col-comp bg-body-tertiary">BT</th>
                                <th class="col-comp bg-body-tertiary">BV</th>
                                <th class="col-comp bg-body-tertiary">CK</th>
                                <th class="col-comp bg-body-tertiary">DA</th>
                                <th class="col-comp bg-body-tertiary">GK</th>
                                <th class="col-comp bg-body-tertiary">QT</th>
                                <th class="col-comp bg-body-tertiary">TH</th>
                                <th>Thang 10</th>
                                <th>Thang 4</th>
                                <th>Điểm chữ</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty gradeList}">
                                    <c:forEach var="g" items="${gradeList}" varStatus="loop">
                                        <tr>
                                            <td class="text-center fw-semibold text-muted">${loop.index + 1}</td>
                                            <td class="text-center"><span class="badge bg-light text-dark border">${g.semester}</span></td>
                                            <td class="fw-semibold text-start">${g.courseName}</td>
                                            <td class="text-center">
                                                <span class="${g.credits == 0 ? 'text-muted fst-italic' : 'fw-bold text-primary'}">
                                                    <fmt:formatNumber value="${g.credits}" pattern="#.#"/>
                                                </span>
                                            </td>

                                            <!-- Công thức điểm -->
                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${not empty g.formula}">
                                                        <span class="formula-pill font-monospace" title="${g.formula}">${g.formula}</span>
                                                    </c:when>
                                                    <c:otherwise><span class="text-null">-</span></c:otherwise>
                                                </c:choose>
                                            </td>

                                            <!-- 7 Cột Điểm thành phần -->
                                            <td class="col-comp">${g.scoreBt != null ? g.scoreBt : '<span class="text-null">-</span>'}</td>
                                            <td class="col-comp">${g.scoreBv != null ? g.scoreBv : '<span class="text-null">-</span>'}</td>
                                            <td class="col-comp">${g.scoreCk != null ? g.scoreCk : '<span class="text-null">-</span>'}</td>
                                            <td class="col-comp">${g.scoreDa != null ? g.scoreDa : '<span class="text-null">-</span>'}</td>
                                            <td class="col-comp">${g.scoreGk != null ? g.scoreGk : '<span class="text-null">-</span>'}</td>
                                            <td class="col-comp">${g.scoreQt != null ? g.scoreQt : '<span class="text-null">-</span>'}</td>
                                            <td class="col-comp">${g.scoreTh != null ? g.scoreTh : '<span class="text-null">-</span>'}</td>

                                            <!-- Điểm tổng kết -->
                                            <td class="text-center fw-bold text-secondary">
                                                ${g.score10 != null ? g.score10 : '<span class="text-null">-</span>'}
                                            </td>
                                            <td class="text-center fw-bold text-primary">
                                                ${g.score4 != null ? g.score4 : '<span class="text-null">-</span>'}
                                            </td>
                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${g.scoreLetter == 'Đạt'}">
                                                        <span class="badge bg-success badge-grade">
                                                            <i class="fa-solid fa-circle-check me-1"></i>Đạt
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${empty g.scoreLetter}">
                                                        <span class="badge bg-warning text-dark badge-grade">
                                                            <i class="fa-solid fa-hourglass-half me-1"></i>Đang học
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${g.scoreLetter == 'A' || g.scoreLetter == 'A+'}">
                                                        <span class="badge bg-success badge-grade">${g.scoreLetter}</span>
                                                    </c:when>
                                                    <c:when test="${g.scoreLetter == 'B' || g.scoreLetter == 'B+'}">
                                                        <span class="badge bg-primary badge-grade">${g.scoreLetter}</span>
                                                    </c:when>
                                                    <c:when test="${g.scoreLetter == 'C' || g.scoreLetter == 'C+'}">
                                                        <span class="badge bg-warning text-dark badge-grade">${g.scoreLetter}</span>
                                                    </c:when>
                                                    <c:when test="${g.scoreLetter == 'D' || g.scoreLetter == 'D+'}">
                                                        <span class="badge bg-secondary badge-grade">${g.scoreLetter}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-danger badge-grade">${g.scoreLetter}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <!-- Thao tác -->
                                            <td class="text-center">
                                                <button type="button" class="btn btn-sm btn-outline-secondary me-1" 
                                                        title="Sửa điểm / Dự báo GPA"
                                                        onclick="openEditModal(${g.id}, '${g.courseName}', '${g.semester}', ${g.credits}, '${g.formula}', '${g.scoreBt}', '${g.scoreBv}', '${g.scoreCk}', '${g.scoreDa}', '${g.scoreGk}', '${g.scoreQt}', '${g.scoreTh}', '${g.score10}', '${g.score4}', '${g.scoreLetter}')">
                                                    <i class="fa-solid fa-pen-to-square"></i>
                                                </button>
                                                <form action="grades" method="post" class="d-inline" onsubmit="return confirm('Bạn có chắc chắn muốn xóa môn này khỏi bảng điểm?');">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="gradeId" value="${g.id}">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger" title="Xóa môn">
                                                        <i class="fa-solid fa-trash-can"></i>
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="16" class="text-center py-5 text-muted">
                                            <i class="fa-solid fa-inbox fa-3x mb-3 text-secondary"></i>
                                            <p class="mb-0">Chưa có dữ liệu môn học nào. Hãy dán bảng điểm ở khung phía trên!</p>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </main>

    <!-- MODAL SỬA ĐIỂM (GPA SIMULATOR) -->
    <div class="modal fade" id="editGradeModal" tabindex="-1" aria-labelledby="editGradeModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content shadow">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title fw-bold" id="editGradeModalLabel">
                        <i class="fa-solid fa-sliders me-2"></i>Chỉnh sửa điểm & Dự báo GPA (Simulator)
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form action="grades" method="post">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" id="editGradeId" name="gradeId">
                    <div class="modal-body">
                        <!-- Thông tin môn -->
                        <div class="alert alert-light border mb-3">
                            <div class="row align-items-center">
                                <div class="col-md-8">
                                    <strong id="editCourseName" class="text-primary fs-6"></strong> 
                                    <div class="small text-muted">Học kỳ: <span id="editSemester" class="fw-semibold"></span></div>
                                </div>
                                <div class="col-md-4 text-md-end">
                                    <span class="badge bg-secondary" id="editCreditsBadge"></span>
                                </div>
                            </div>
                        </div>

                        <!-- Công thức điểm -->
                        <div class="mb-3">
                            <label class="form-label fw-semibold">Công thức điểm:</label>
                            <div class="input-group">
                                <input type="text" class="form-control font-monospace" id="editFormula" name="formula" 
                                       placeholder="Ví dụ: [GK]*0.20+[BT]*0.20+[CK]*0.60" oninput="calculateSimulator('edit')">
                                <button class="btn btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    Mẫu có sẵn
                                </button>
                                <ul class="dropdown-menu dropdown-menu-end">
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('edit', '[GK]*0.20+[BT]*0.20+[CK]*0.60')">Chuẩn: [GK]*0.20+[BT]*0.20+[CK]*0.60</a></li>
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('edit', '[GK]*0.30+[BT]*0.10+[CK]*0.60')">[GK]*0.30+[BT]*0.10+[CK]*0.60</a></li>
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('edit', '[GK]*0.30+[CK]*0.70')">[GK]*0.30+[CK]*0.70</a></li>
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('edit', '[DA]*0.30+[CK]*0.70')">PBL: [DA]*0.30+[CK]*0.70</a></li>
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('edit', '[QT]*0.40+[BV]*0.60')">PBL: [QT]*0.40+[BV]*0.60</a></li>
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('edit', '[CK]*1.00')">100% Cuối kỳ: [CK]*1.00</a></li>
                                </ul>
                            </div>
                        </div>

                        <!-- 7 Điểm thành phần -->
                        <label class="form-label fw-semibold">Điểm thành phần (BT, BV, CK, DA, GK, QT, TH):</label>
                        <div class="row g-2 mb-3">
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">BT</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="editScoreBt" name="scoreBt" oninput="calculateSimulator('edit')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">BV</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="editScoreBv" name="scoreBv" oninput="calculateSimulator('edit')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center text-primary fw-bold">CK</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center border-primary" id="editScoreCk" name="scoreCk" placeholder="Dự kiến" oninput="calculateSimulator('edit')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">DA</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="editScoreDa" name="scoreDa" oninput="calculateSimulator('edit')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">GK</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="editScoreGk" name="scoreGk" oninput="calculateSimulator('edit')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">QT</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="editScoreQt" name="scoreQt" oninput="calculateSimulator('edit')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">TH</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="editScoreTh" name="scoreTh" oninput="calculateSimulator('edit')">
                            </div>
                        </div>

                        <!-- Điểm tổng kết -->
                        <div class="card bg-body-tertiary border-0 p-3 mb-3">
                            <div class="row align-items-center">
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-muted">Điểm tổng kết (Thang 10)</label>
                                    <input type="number" step="0.1" min="0" max="10" class="form-control fw-bold text-center border-primary" id="editScore10" name="score10" placeholder="Tự tính hoặc gõ tay" oninput="syncFromScore10('edit')">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-muted">Thang 4 <span class="badge bg-secondary">Tự động</span></label>
                                    <input type="text" class="form-control fw-bold text-primary text-center bg-light" id="editScore4" name="score4" readonly tabindex="-1" placeholder="-">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-muted">Điểm chữ <span class="badge bg-secondary">Tự động</span></label>
                                    <input type="text" class="form-control fw-bold text-success text-center bg-light" id="editScoreLetter" name="scoreLetter" readonly tabindex="-1" placeholder="-">
                                </div>
                            </div>
                            <div class="small text-success mt-2" id="editSimNotice">
                                <i class="fa-solid fa-wand-magic-sparkles me-1"></i>Hệ thống tự động quy đổi Thang 4 và Điểm chữ từ Điểm 10 theo đúng quy chế đào tạo tín chỉ DUT.
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary px-4 fw-semibold">
                            <i class="fa-solid fa-floppy-disk me-1"></i>Lưu thay đổi
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- MODAL THÊM MÔN HỌC THỦ CÔNG (Không cần nhập mã học phần phức tạp) -->
    <div class="modal fade" id="addCourseModal" tabindex="-1" aria-labelledby="addCourseModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content shadow">
                <div class="modal-header bg-success text-white">
                    <h5 class="modal-title fw-bold" id="addCourseModalLabel">
                        <i class="fa-solid fa-plus-circle me-2"></i>Thêm môn học thủ công
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form action="grades" method="post">
                    <input type="hidden" name="action" value="add">
                    <div class="modal-body">
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label class="form-label fw-semibold">Học kỳ <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" name="semester" placeholder="Ví dụ: 1/2026-2027" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label fw-semibold">Số tín chỉ <span class="text-danger">*</span></label>
                                <input type="number" step="0.5" min="0" max="15" class="form-control" name="credits" placeholder="Ví dụ: 2 hoặc 2.5" required>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-semibold">Tên môn học <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="courseName" placeholder="Ví dụ: Công nghệ Web" required>
                        </div>

                        <!-- Công thức tính điểm -->
                        <div class="mb-3">
                            <label class="form-label fw-semibold">Công thức tính điểm (tùy chọn):</label>
                            <div class="input-group">
                                <input type="text" class="form-control font-monospace" id="addFormula" name="formula" 
                                       placeholder="Ví dụ: [GK]*0.20+[BT]*0.20+[CK]*0.60" oninput="calculateSimulator('add')">
                                <button class="btn btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    Mẫu có sẵn
                                </button>
                                <ul class="dropdown-menu dropdown-menu-end">
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('add', '[GK]*0.20+[BT]*0.20+[CK]*0.60')">Chuẩn: [GK]*0.20+[BT]*0.20+[CK]*0.60</a></li>
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('add', '[GK]*0.30+[BT]*0.10+[CK]*0.60')">[GK]*0.30+[BT]*0.10+[CK]*0.60</a></li>
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('add', '[GK]*0.30+[CK]*0.70')">[GK]*0.30+[CK]*0.70</a></li>
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('add', '[DA]*0.30+[CK]*0.70')">PBL: [DA]*0.30+[CK]*0.70</a></li>
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('add', '[QT]*0.40+[BV]*0.60')">PBL: [QT]*0.40+[BV]*0.60</a></li>
                                    <li><a class="dropdown-item" href="javascript:void(0)" onclick="setFormulaPreset('add', '[CK]*1.00')">100% Cuối kỳ: [CK]*1.00</a></li>
                                </ul>
                            </div>
                            <div class="form-text small text-muted">
                                Chọn mẫu có sẵn hoặc gõ mã điểm (<code>[BT]</code>, <code>[BV]</code>, <code>[CK]</code>, <code>[DA]</code>, <code>[GK]</code>, <code>[QT]</code>, <code>[TH]</code>) nhân với hệ số.
                            </div>
                        </div>

                        <!-- 7 Điểm thành phần -->
                        <label class="form-label fw-semibold">Điểm thành phần (để trống nếu chưa có):</label>
                        <div class="row g-2 mb-3">
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">BT</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="addScoreBt" name="scoreBt" oninput="calculateSimulator('add')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">BV</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="addScoreBv" name="scoreBv" oninput="calculateSimulator('add')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">CK</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="addScoreCk" name="scoreCk" oninput="calculateSimulator('add')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">DA</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="addScoreDa" name="scoreDa" oninput="calculateSimulator('add')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">GK</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="addScoreGk" name="scoreGk" oninput="calculateSimulator('add')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">QT</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="addScoreQt" name="scoreQt" oninput="calculateSimulator('add')">
                            </div>
                            <div class="col-4 col-sm">
                                <label class="small text-muted d-block text-center">TH</label>
                                <input type="number" step="0.1" min="0" max="10" class="form-control text-center" id="addScoreTh" name="scoreTh" oninput="calculateSimulator('add')">
                            </div>
                        </div>

                        <!-- Điểm tổng kết -->
                        <div class="card bg-body-tertiary border-0 p-3 mb-2">
                            <div class="row align-items-center">
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-muted">Điểm tổng kết 10 (tùy chọn)</label>
                                    <input type="number" step="0.1" min="0" max="10" class="form-control text-center border-primary fw-bold" id="addScore10" name="score10" placeholder="Để trống nếu đang học" oninput="syncFromScore10('add')">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-muted">Thang 4 <span class="badge bg-secondary">Tự động</span></label>
                                    <input type="text" class="form-control text-center text-primary fw-bold bg-light" id="addScore4" name="score4" readonly tabindex="-1" placeholder="-">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label small fw-semibold text-muted">Điểm chữ <span class="badge bg-secondary">Tự động</span></label>
                                    <input type="text" class="form-control text-center text-success fw-bold bg-light" id="addScoreLetter" name="scoreLetter" readonly tabindex="-1" placeholder="-">
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-success px-4 fw-semibold">
                            <i class="fa-solid fa-plus me-1"></i>Thêm môn học
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- 6. CHÂN TRANG (FOOTER) -->
    <footer class="py-3 bg-dark text-white text-center mt-auto">
        <div class="container-fluid px-lg-4">
            <small class="text-secondary">© 2026 DUT Grade Calculator. Phân hệ Bóc tách bảng điểm & Quản lý điểm DUT.</small>
        </div>
    </footer>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        function openEditModal(id, name, sem, credits, formula, bt, bv, ck, da, gk, qt, th, s10, s4, sLetter) {
            document.getElementById('editGradeId').value = id;
            document.getElementById('editCourseName').innerText = name;
            document.getElementById('editSemester').innerText = sem;
            document.getElementById('editCreditsBadge').innerText = credits + ' Tín chỉ';
            document.getElementById('editFormula').value = (formula === 'null' || !formula) ? '' : formula;

            document.getElementById('editScoreBt').value = (bt === 'null' || !bt) ? '' : bt;
            document.getElementById('editScoreBv').value = (bv === 'null' || !bv) ? '' : bv;
            document.getElementById('editScoreCk').value = (ck === 'null' || !ck) ? '' : ck;
            document.getElementById('editScoreDa').value = (da === 'null' || !da) ? '' : da;
            document.getElementById('editScoreGk').value = (gk === 'null' || !gk) ? '' : gk;
            document.getElementById('editScoreQt').value = (qt === 'null' || !qt) ? '' : qt;
            document.getElementById('editScoreTh').value = (th === 'null' || !th) ? '' : th;

            document.getElementById('editScore10').value = (s10 === 'null' || !s10) ? '' : s10;
            document.getElementById('editScore4').value = (s4 === 'null' || !s4) ? '' : s4;
            document.getElementById('editScoreLetter').value = (sLetter === 'null' || !sLetter) ? '' : sLetter;

            var modal = new bootstrap.Modal(document.getElementById('editGradeModal'));
            modal.show();
        }

        function calculateSimulator(prefix) {
            const formula = document.getElementById(prefix + 'Formula').value.trim();
            if (!formula) return;

            const scores = {
                'BT': parseNum(document.getElementById(prefix + 'ScoreBt').value),
                'BV': parseNum(document.getElementById(prefix + 'ScoreBv').value),
                'CK': parseNum(document.getElementById(prefix + 'ScoreCk').value),
                'DA': parseNum(document.getElementById(prefix + 'ScoreDa').value),
                'GK': parseNum(document.getElementById(prefix + 'ScoreGk').value),
                'QT': parseNum(document.getElementById(prefix + 'ScoreQt').value),
                'TH': parseNum(document.getElementById(prefix + 'ScoreTh').value)
            };

            const regex = /\[([A-Za-z0-9_]+)\]\s*\*\s*([0-9.]+)/g;
            let match;
            let total = 0;
            let totalWeight = 0;
            let complete = true;

            while ((match = regex.exec(formula)) !== null) {
                const comp = match[1].toUpperCase();
                const weight = parseFloat(match[2]);
                totalWeight += weight;

                if (scores[comp] === null || isNaN(scores[comp])) {
                    complete = false;
                } else {
                    total += scores[comp] * weight;
                }
            }

            if (complete && totalWeight > 0) {
                const s10 = Math.round(total * 10) / 10;
                document.getElementById(prefix + 'Score10').value = s10;
                syncFromScore10(prefix);
            }
        }

        function syncFromScore10(prefix) {
            const val = parseNum(document.getElementById(prefix + 'Score10').value);
            if (val === null) {
                document.getElementById(prefix + 'Score4').value = '';
                document.getElementById(prefix + 'ScoreLetter').value = '';
                return;
            }

            let s4 = 0.0;
            let letter = 'F';

            if (val >= 8.5) { s4 = 4.0; letter = 'A'; }
            else if (val >= 8.0) { s4 = 3.5; letter = 'B+'; }
            else if (val >= 7.0) { s4 = 3.0; letter = 'B'; }
            else if (val >= 6.5) { s4 = 2.5; letter = 'C+'; }
            else if (val >= 5.5) { s4 = 2.0; letter = 'C'; }
            else if (val >= 5.0) { s4 = 1.5; letter = 'D+'; }
            else if (val >= 4.0) { s4 = 1.0; letter = 'D'; }
            else { s4 = 0.0; letter = 'F'; }

            document.getElementById(prefix + 'Score4').value = s4.toFixed(1);
            document.getElementById(prefix + 'ScoreLetter').value = letter;
        }

        function setFormulaPreset(prefix, formulaStr) {
            document.getElementById(prefix + 'Formula').value = formulaStr;
            calculateSimulator(prefix);
        }

        function parseNum(val) {
            if (val === undefined || val === null || val.trim() === '') return null;
            const n = parseFloat(val);
            return isNaN(n) ? null : n;
        }
    </script>
</body>
</html>
