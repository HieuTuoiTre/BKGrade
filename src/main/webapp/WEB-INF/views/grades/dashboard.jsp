<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="csrf-token" content="${fn:escapeXml(sessionScope.csrfToken)}">
    <title>Bảng Điểm Học Tập - BKGrade Calc</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- FontAwesome Icon -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
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
                <i class="fa-solid fa-triangle-exclamation me-2"></i>${fn:escapeXml(errorMessage)}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
                <i class="fa-solid fa-circle-check me-2"></i>${fn:escapeXml(successMessage)}
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
                            <h3 class="fw-bold mb-0 text-success">${not empty totalSummary ? fn:escapeXml(totalSummary.academicRank) : 'Chưa có'}</h3>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 4. THANH ĐIỀU HƯỚNG TABS (NAV-PILLS) -->
        <ul class="nav nav-pills mb-4 nav-justified bg-white p-2 rounded shadow-sm border" id="dashboardTabs" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active fw-bold py-2" id="grades-tab" data-bs-toggle="pill" data-bs-target="#gradesTabPane" type="button" role="tab" aria-controls="gradesTabPane" aria-selected="true">
                    <i class="fa-solid fa-table-list me-2"></i>Bảng điểm chính thức
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link fw-bold py-2" id="simulator-tab" data-bs-toggle="pill" data-bs-target="#simulatorTabPane" type="button" role="tab" aria-controls="simulatorTabPane" aria-selected="false">
                    <i class="fa-solid fa-bullseye me-2"></i>Dự đoán & Mô phỏng mục tiêu GPA <span class="badge bg-danger ms-1">Mới</span>
                </button>
            </li>
        </ul>

        <div class="tab-content" id="dashboardTabsContent">
            <!-- TAB 1: BẢNG ĐIỂM CHÍNH THỨC -->
            <div class="tab-pane fade show active" id="gradesTabPane" role="tabpanel" aria-labelledby="grades-tab">

                <!-- KHỐI NHẬP LIỆU (DÁN BẢNG ĐIỂM) -->
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-white py-3">
                        <h5 class="fw-bold mb-0 text-primary">
                            <i class="fa-solid fa-paste me-2"></i>Bóc tách bảng điểm từ DUT
                        </h5>
                    </div>
            <div class="card-body">
                <form action="grades" method="post">
                    <input type="hidden" name="action" value="import">
                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}">
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
                <div class="d-flex align-items-center gap-2 flex-wrap">
                    <c:if test="${not empty gradeList}">
                        <button type="button" class="btn btn-outline-danger btn-sm fw-semibold" id="btnStartBulkSelect" onclick="toggleBulkSelectMode(true)">
                            <i class="fa-solid fa-list-check me-1"></i>Chọn nhiều để xóa
                        </button>
                        <div id="bulkActionGroup" class="d-none align-items-center gap-2">
                            <button type="button" class="btn btn-danger btn-sm fw-semibold" id="btnOpenBulkDeleteModal" onclick="openBulkDeleteModal()" disabled>
                                <i class="fa-solid fa-trash-can me-1"></i>Xóa đã chọn (<span id="bulkSelectedCount">0</span>)
                            </button>
                            <button type="button" class="btn btn-outline-secondary btn-sm fw-semibold" id="btnCancelBulkSelect" onclick="toggleBulkSelectMode(false)">
                                <i class="fa-solid fa-xmark me-1"></i>Hủy
                            </button>
                        </div>
                    </c:if>
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
                                <th class="bulk-select-col d-none text-center" style="width: 38px;">
                                    <input type="checkbox" class="form-check-input" id="selectAllGrades" title="Chọn tất cả" onchange="toggleSelectAllGrades(this)">
                                </th>
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
                                        <tr class="grade-row" onclick="handleGradeRowClick(event, this)">
                                            <td class="bulk-select-col d-none text-center">
                                                <input type="checkbox" class="form-check-input grade-select-checkbox" value="${g.id}" onchange="updateBulkSelectionUI()">
                                            </td>
                                            <td class="text-center fw-semibold text-muted">${loop.index + 1}</td>
                                            <td class="text-center"><span class="badge bg-light text-dark border">${fn:escapeXml(g.semester)}</span></td>
                                            <td class="fw-semibold text-start">${fn:escapeXml(g.courseName)}</td>
                                            <td class="text-center">
                                                <span class="${g.credits == 0 ? 'text-muted fst-italic' : 'fw-bold text-primary'}">
                                                    <fmt:formatNumber value="${g.credits}" pattern="#.#"/>
                                                </span>
                                            </td>

                                            <!-- Công thức điểm -->
                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${not empty g.formula}">
                                                        <span class="formula-pill font-monospace" title="${fn:escapeXml(g.formula)}">${fn:escapeXml(g.formula)}</span>
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
                                                    <c:when test="${g.predicted}">
                                                        <span class="badge bg-info text-dark badge-grade" title="Điểm dự kiến (không tính vào CPA chính thức)">
                                                            <i class="fa-solid fa-wand-magic-sparkles me-1"></i>Dự kiến: ${fn:escapeXml(g.scoreLetter)}
                                                        </span>
                                                    </c:when>
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
                                                        <span class="badge bg-success badge-grade">${fn:escapeXml(g.scoreLetter)}</span>
                                                    </c:when>
                                                    <c:when test="${g.scoreLetter == 'B' || g.scoreLetter == 'B+'}">
                                                        <span class="badge bg-primary badge-grade">${fn:escapeXml(g.scoreLetter)}</span>
                                                    </c:when>
                                                    <c:when test="${g.scoreLetter == 'C' || g.scoreLetter == 'C+'}">
                                                        <span class="badge bg-warning text-dark badge-grade">${fn:escapeXml(g.scoreLetter)}</span>
                                                    </c:when>
                                                    <c:when test="${g.scoreLetter == 'D' || g.scoreLetter == 'D+'}">
                                                        <span class="badge bg-secondary badge-grade">${fn:escapeXml(g.scoreLetter)}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-danger badge-grade">${fn:escapeXml(g.scoreLetter)}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <!-- Thao tác -->
                                            <td class="text-center">
                                                <button type="button" class="btn btn-sm btn-outline-secondary me-1" 
                                                        title="Sửa điểm / Dự báo GPA"
                                                        data-id="${g.id}"
                                                        data-name="${fn:escapeXml(g.courseName)}"
                                                        data-sem="${fn:escapeXml(g.semester)}"
                                                        data-credits="${g.credits}"
                                                        data-formula="${fn:escapeXml(g.formula)}"
                                                        data-bt="${g.scoreBt}"
                                                        data-bv="${g.scoreBv}"
                                                        data-ck="${g.scoreCk}"
                                                        data-da="${g.scoreDa}"
                                                        data-gk="${g.scoreGk}"
                                                        data-qt="${g.scoreQt}"
                                                        data-th="${g.scoreTh}"
                                                        data-s10="${g.score10}"
                                                        data-s4="${g.score4}"
                                                        data-sletter="${fn:escapeXml(g.scoreLetter)}"
                                                        data-predicted="${g.predicted}"
                                                        onclick="openEditModalFromBtn(this)">
                                                    <i class="fa-solid fa-pen-to-square"></i>
                                                </button>
                                                <form action="grades" method="post" class="d-inline" onsubmit="return confirm('Bạn có chắc chắn muốn xóa môn này khỏi bảng điểm?');">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}">
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
            </div> <!-- KẾT THÚC TAB 1: BẢNG ĐIỂM CHÍNH THỨC -->

            <!-- TAB 2: DỰ ĐOÁN & MÔ PHỎNG GPA (SANDBOX) -->
            <div class="tab-pane fade" id="simulatorTabPane" role="tabpanel" aria-labelledby="simulator-tab">
                <!-- KHỐI ĐIỀU KHIỂN MỤC TIÊU GPA -->
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-white py-3">
                        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                            <h5 class="fw-bold mb-0 text-primary">
                                <i class="fa-solid fa-bullseye me-2"></i>Mô phỏng mục tiêu GPA (What-If Sandbox)
                            </h5>
                            <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 fw-semibold">
                                <i class="fa-solid fa-shield-halved me-1"></i>Chế độ thử nghiệm an toàn (Không ảnh hưởng CSDL thật)
                            </span>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="row g-4 align-items-center">
                            <!-- Cột 1: CPA hiện tại & Mục tiêu -->
                            <div class="col-lg-5">
                                <div class="p-3 bg-light rounded-3 border">
                                    <div class="row align-items-center g-2 mb-3">
                                        <div class="col-6">
                                            <span class="small text-muted fw-semibold text-uppercase d-block">CPA hiện tại</span>
                                            <span class="fs-4 fw-bold text-secondary">
                                                ${not empty totalSummary ? totalSummary.gpa4 : 0.0}
                                            </span>
                                            <small class="text-muted">/ 4.0</small>
                                        </div>
                                        <div class="col-6">
                                            <label for="simTargetGpaInput" class="small text-muted fw-semibold text-uppercase d-block">Mục tiêu CPA</label>
                                            <div class="input-group input-group-sm">
                                                <input type="number" step="0.05" min="1.0" max="4.0" class="form-control fw-bold text-center border-primary" 
                                                       id="simTargetGpaInput" value="3.20" onchange="window.recalculateGpaSim()">
                                                <span class="input-group-text bg-white">/ 4.0</span>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Nút gợi ý kịch bản -->
                                    <button type="button" class="btn btn-primary w-100 fw-semibold shadow-sm" onclick="window.suggestOptimalScenarios()">
                                        <i class="fa-solid fa-wand-magic-sparkles me-2"></i>Gợi ý kịch bản tối ưu để đạt mục tiêu
                                    </button>
                                </div>
                            </div>

                            <!-- Cột 2: CPA dự kiến theo thời gian thực (Live KPI) -->
                            <div class="col-lg-7">
                                <div class="p-3 bg-white rounded-3 border shadow-sm">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <span class="small fw-bold text-uppercase text-muted">
                                            <i class="fa-solid fa-chart-line text-success me-1"></i>CPA dự phóng thời gian thực
                                        </span>
                                        <span id="simTargetBadge" class="badge bg-secondary p-2">Đang tính toán...</span>
                                    </div>
                                    <div class="d-flex align-items-baseline mb-2 flex-wrap">
                                        <h2 class="display-6 fw-bold text-primary mb-0 me-2" id="simLiveCpa">--</h2>
                                        <span class="text-muted fs-5 me-3">/ 4.0</span>
                                        <span id="simCpaDiff" class="badge bg-secondary me-3">Giữ nguyên</span>
                                        <span class="small text-muted ms-auto">Tổng tín chỉ: <strong id="simTotalCredits" class="text-dark">-- TC</strong></span>
                                    </div>
                                    <div class="progress" style="height: 10px;">
                                        <div id="simProgressBar" class="progress-bar bg-primary" role="progressbar" style="width: 0%;" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"></div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Khu vực hiển thị kịch bản gợi ý -->
                        <div id="simScenarioResults"></div>
                    </div>
                </div>

                <!-- KHỐI DANH SÁCH MÔN HỌC MÔ PHỎNG -->
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
                        <div class="d-flex align-items-center">
                            <h5 class="fw-bold mb-0 text-dark me-2">
                                <i class="fa-solid fa-sliders me-2 text-primary"></i>Các môn học đang học & dự kiến
                            </h5>
                            <span class="badge bg-primary" id="simCourseCountBadge">0 môn</span>
                        </div>
                        <div class="d-flex gap-2 flex-wrap">
                            <button type="button" class="btn btn-outline-secondary btn-sm fw-semibold" onclick="window.resetSimulation()">
                                <i class="fa-solid fa-rotate-left me-1"></i>Đặt lại
                            </button>
                            <form action="grades" method="post" class="d-inline" onsubmit="return confirm('Xóa tất cả điểm dự kiến đã lưu và trả các môn đó về trạng thái Đang học?');">
                                <input type="hidden" name="action" value="clear_predictions">
                                <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}">
                                <button type="submit" class="btn btn-outline-danger btn-sm fw-semibold">
                                    <i class="fa-solid fa-eraser me-1"></i>Xóa điểm dự kiến
                                </button>
                            </form>
                            <button type="button" class="btn btn-success btn-sm fw-semibold shadow-sm" onclick="window.saveSimulationToDb()">
                                <i class="fa-solid fa-floppy-disk me-1"></i>Lưu điểm dự kiến vào CSDL
                            </button>
                        </div>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover table-striped align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th class="text-center" style="width: 50px;">STT</th>
                                        <th>Môn học & Học kỳ</th>
                                        <th class="text-center" style="width: 100px;">Số TC</th>
                                        <th class="text-center" style="width: 160px;">Điểm chữ giả định</th>
                                        <th class="text-center" style="width: 100px;">Trạng thái</th>
                                    </tr>
                                </thead>
                                <tbody id="simCoursesTableBody">
                                    <!-- JS sẽ render các môn học ở đây -->
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="card-footer bg-light py-2">
                        <small class="text-muted">
                            <i class="fa-solid fa-circle-info me-1"></i>Chọn điểm chữ ở cột <strong>"Điểm chữ giả định"</strong> để xem CPA dự phóng nhảy số tức thì. Bấm <strong>"Lưu điểm dự kiến vào CSDL"</strong> khi bạn muốn áp dụng kết quả này vào bảng điểm.
                        </small>
                    </div>
                </div>
            </div> <!-- KẾT THÚC TAB 2: DỰ ĐOÁN & MÔ PHỎNG GPA -->
        </div> <!-- KẾT THÚC tab-content -->

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
                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}">
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

                        <!-- Khối dự đoán điểm CK cần đạt (Course Target Prediction) -->
                        <div id="editQuickPredictionBox" class="mb-3"></div>

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
                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}">
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

                        <!-- Khối dự đoán điểm CK cần đạt (Course Target Prediction) -->
                        <div id="addQuickPredictionBox" class="mb-3"></div>

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

    <!-- MODAL XÁC NHẬN XÓA HÀNG LOẠT -->
    <div class="modal fade" id="bulkDeleteConfirmModal" tabindex="-1" aria-labelledby="bulkDeleteConfirmModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content shadow">
                <div class="modal-header bg-danger text-white">
                    <h5 class="modal-title fw-bold" id="bulkDeleteConfirmModalLabel">
                        <i class="fa-solid fa-triangle-exclamation me-2"></i>Xác nhận xóa nhiều môn học
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form action="grades" method="post">
                    <input type="hidden" name="action" value="delete_batch">
                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.csrfToken)}">
                    <div id="bulkDeleteHiddenInputs"></div>
                    <div class="modal-body">
                        <p class="mb-2 fs-6">
                            Bạn có chắc chắn muốn xóa <strong id="bulkConfirmCount" class="text-danger">0</strong> môn học đã chọn khỏi bảng điểm không?
                        </p>
                        <p class="text-muted small mb-0">
                            <i class="fa-solid fa-circle-info me-1"></i>Thao tác này sẽ xóa vĩnh viễn các môn đã chọn và không thể hoàn tác.
                        </p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-danger px-4 fw-semibold">
                            <i class="fa-solid fa-trash-can me-1"></i>Xác nhận xóa
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
    <!-- Simulator Engine JS -->
    <script src="${pageContext.request.contextPath}/assets/js/simulator.js"></script>
    <!-- Dashboard Interactions JS -->
    <script src="${pageContext.request.contextPath}/assets/js/dashboard.js"></script>
    <script>
        document.addEventListener("DOMContentLoaded", function () {
            // Nạp dữ liệu các môn từ DOM vào Simulator engine
            var rows = document.querySelectorAll('button[data-id]');
            var rawCourses = [];
            rows.forEach(function (btn) {
                var d = btn.dataset;
                rawCourses.push({
                    id: parseInt(d.id),
                    courseName: d.name,
                    semester: d.sem,
                    credits: parseFloat(d.credits) || 0,
                    formula: (d.formula === 'null' || !d.formula) ? '' : d.formula,
                    score10: (d.s10 === 'null' || !d.s10) ? null : parseFloat(d.s10),
                    score4: (d.s4 === 'null' || !d.s4) ? null : parseFloat(d.s4),
                    scoreLetter: (d.sletter === 'null' || !d.sletter) ? '' : d.sletter,
                    isPredicted: d.predicted === 'true'
                });
            });

            if (window.DutSimulator && typeof window.DutSimulator.initGpaSimulator === 'function') {
                window.DutSimulator.initGpaSimulator(rawCourses);
            }
        });
    </script>
</body>
</html>
