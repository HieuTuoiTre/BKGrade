package com.gradecalc.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gradecalc.config.CustomConnectionPool;
import com.gradecalc.model.Bean.Grade;
import com.gradecalc.model.Bean.SemesterSummary;
import com.gradecalc.model.Bo.DutParserBO;
import com.gradecalc.model.Bo.GradeCalculatorBO;
import com.gradecalc.model.Dao.GradeDAO;

@WebServlet("/grades")
public class GradeController extends HttpServlet {
    private static final String CSRF_TOKEN_ATTR = "csrfToken";
    private static final String FLASH_SUCCESS_ATTR = "flashSuccess";
    private static final String FLASH_ERROR_ATTR = "flashError";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private DutParserBO parserBO;
    private GradeCalculatorBO calculatorBO;
    private GradeDAO gradeDAO;

    @Override
    public void init() {
        parserBO = new DutParserBO();
        calculatorBO = new GradeCalculatorBO();
        gradeDAO = new GradeDAO();
    }

    @Override
    public void destroy() {
        CustomConnectionPool.shutdownIfInitialized();
    }

    private int getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            return (Integer) session.getAttribute("userId");
        }
        return 1; // User test mặc định khi chưa ghép phân hệ Auth (Track A)
    }

    private void applySecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    private String ensureCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        if (token == null || token.isEmpty()) {
            byte[] bytes = new byte[32];
            SECURE_RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(CSRF_TOKEN_ATTR, token);
        }
        return token;
    }

    private boolean isValidCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String expected = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        String actual = request.getParameter("csrfToken");
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private Double parseDoubleParam(HttpServletRequest request, String paramName) {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isValidScore(Double score) {
        return score == null || (score >= 0.0 && score <= 10.0);
    }

    private boolean populateAndValidateGradeScores(HttpServletRequest request, Grade grade, double credits, boolean isAdd) {
        String formula = request.getParameter("formula");
        Double scoreBt = parseDoubleParam(request, "scoreBt");
        Double scoreBv = parseDoubleParam(request, "scoreBv");
        Double scoreCk = parseDoubleParam(request, "scoreCk");
        Double scoreDa = parseDoubleParam(request, "scoreDa");
        Double scoreGk = parseDoubleParam(request, "scoreGk");
        Double scoreQt = parseDoubleParam(request, "scoreQt");
        Double scoreTh = parseDoubleParam(request, "scoreTh");
        Double score10 = parseDoubleParam(request, "score10");

        if (!isValidScore(scoreBt) || !isValidScore(scoreBv) || !isValidScore(scoreCk)
                || !isValidScore(scoreDa) || !isValidScore(scoreGk) || !isValidScore(scoreQt)
                || !isValidScore(scoreTh) || !isValidScore(score10)) {
            return false;
        }

        if (score10 == null && formula != null && !formula.trim().isEmpty()) {
            score10 = GradeCalculatorBO.evaluateFormula(formula, scoreBt, scoreBv, scoreCk, scoreDa, scoreGk, scoreQt, scoreTh);
        }

        Double score4 = null;
        String scoreLetter = null;
        if (score10 != null) {
            score4 = GradeCalculatorBO.convertScore10ToScore4(score10);
            scoreLetter = GradeCalculatorBO.convertScore10ToLetter(score10);
        } else if ((isAdd && credits == 0) || "Đạt".equals(request.getParameter("scoreLetter"))) {
            scoreLetter = "Đạt";
        }

        grade.setFormula(formula);
        grade.setScoreBt(scoreBt);
        grade.setScoreBv(scoreBv);
        grade.setScoreCk(scoreCk);
        grade.setScoreDa(scoreDa);
        grade.setScoreGk(scoreGk);
        grade.setScoreQt(scoreQt);
        grade.setScoreTh(scoreTh);
        grade.setScore10(score10);
        grade.setScore4(score4);
        grade.setScoreLetter(scoreLetter);
        return true;
    }

    private void setFlash(HttpServletRequest request, String attr, String message) {
        request.getSession(true).setAttribute(attr, message);
    }

    private void consumeFlashMessages(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object success = session.getAttribute(FLASH_SUCCESS_ATTR);
        if (success != null) {
            request.setAttribute("successMessage", success);
            session.removeAttribute(FLASH_SUCCESS_ATTR);
        }
        Object error = session.getAttribute(FLASH_ERROR_ATTR);
        if (error != null) {
            request.setAttribute("errorMessage", error);
            session.removeAttribute(FLASH_ERROR_ATTR);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        applySecurityHeaders(response);
        ensureCsrfToken(request);
        consumeFlashMessages(request);

        try {
            int userId = getUserId(request);
            List<Grade> gradeList = gradeDAO.getGradeByUserId(userId);
            SemesterSummary totalSummary = calculatorBO.calculate(gradeList);
            List<SemesterSummary> semesterSummaries = calculatorBO.calculateBySemester(gradeList);

            request.setAttribute("gradeList", gradeList);
            request.setAttribute("totalSummary", totalSummary);
            request.setAttribute("semesterSummaries", semesterSummaries);
        } catch (Exception e) {
            log("Lỗi khi tải bảng điểm", e);
            request.setAttribute("errorMessage", "Không thể tải dữ liệu bảng điểm lúc này. Vui lòng thử lại sau!");
        }

        request.getRequestDispatcher("/WEB-INF/views/grades/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        applySecurityHeaders(response);

        String redirectTarget = request.getContextPath() + "/grades";

        if (!isValidCsrfToken(request)) {
            setFlash(request, FLASH_ERROR_ATTR, "Phiên làm việc không hợp lệ hoặc đã hết hạn (CSRF). Vui lòng thử lại!");
            response.sendRedirect(redirectTarget);
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "import";
        }

        try {
            int userId = getUserId(request);

            if ("import".equals(action)) {
                String rawText = request.getParameter("rawDutText");
                if (rawText == null || rawText.trim().isEmpty()) {
                    setFlash(request, FLASH_ERROR_ATTR, "Bạn chưa dán dữ liệu bảng điểm vào ô nhập!");
                } else {
                    List<Grade> gradeList = parserBO.parse(rawText);
                    if (gradeList.isEmpty()) {
                        setFlash(request, FLASH_ERROR_ATTR, "Không tìm thấy dữ liệu hợp lệ. Hãy copy đúng các dòng bảng điểm từ sv.dut.udn.vn!");
                    } else {
                        gradeDAO.saveGradeList(userId, gradeList);
                        setFlash(request, FLASH_SUCCESS_ATTR, "Đã bóc tách và lưu " + gradeList.size() + " môn học thành công!");
                    }
                }
            } else if ("update".equals(action)) {
                int gradeId = Integer.parseInt(request.getParameter("gradeId"));
                Grade grade = new Grade();
                grade.setId(gradeId);
                if (!populateAndValidateGradeScores(request, grade, 1.0, false)) {
                    setFlash(request, FLASH_ERROR_ATTR, "Điểm nhập vào phải nằm trong khoảng từ 0.0 đến 10.0!");
                } else if (gradeDAO.updateGrade(userId, grade)) {
                    setFlash(request, FLASH_SUCCESS_ATTR, "Đã cập nhật điểm thành công!");
                } else {
                    setFlash(request, FLASH_ERROR_ATTR, "Không tìm thấy môn học hợp lệ để cập nhật!");
                }
            } else if ("add".equals(action)) {
                String semester = request.getParameter("semester");
                String courseName = request.getParameter("courseName");
                Double credits = parseDoubleParam(request, "credits");

                if (semester == null || semester.trim().isEmpty()
                        || courseName == null || courseName.trim().isEmpty()
                        || credits == null || credits < 0.0 || credits > 15.0) {
                    setFlash(request, FLASH_ERROR_ATTR, "Thông tin học kỳ, tên môn hoặc số tín chỉ (0 - 15) không hợp lệ!");
                } else {
                    Grade grade = new Grade();
                    grade.setUserId(userId);
                    grade.setSemester(semester.trim());
                    grade.setCourseName(courseName.trim());
                    grade.setCredits(credits);
                    if (!populateAndValidateGradeScores(request, grade, credits, true)) {
                        setFlash(request, FLASH_ERROR_ATTR, "Điểm nhập vào phải nằm trong khoảng từ 0.0 đến 10.0!");
                    } else {
                        gradeDAO.addManualGrade(userId, grade);
                        setFlash(request, FLASH_SUCCESS_ATTR, "Đã thêm môn học " + courseName.trim() + " thành công!");
                    }
                }
            } else if ("delete".equals(action)) {
                int gradeId = Integer.parseInt(request.getParameter("gradeId"));
                if (gradeDAO.deleteGrade(userId, gradeId)) {
                    setFlash(request, FLASH_SUCCESS_ATTR, "Đã xóa môn học khỏi bảng điểm!");
                } else {
                    setFlash(request, FLASH_ERROR_ATTR, "Không tìm thấy môn học hợp lệ để xóa!");
                }
            } else if ("delete_batch".equals(action)) {
                List<Integer> ids = parsePositiveUniqueIds(request.getParameterValues("gradeIds"));
                if (ids.isEmpty()) {
                    setFlash(request, FLASH_ERROR_ATTR, "Bạn chưa chọn môn học hợp lệ nào để xóa!");
                } else {
                    int deleted = gradeDAO.deleteGradesBatch(userId, ids);
                    if (deleted > 0) {
                        setFlash(request, FLASH_SUCCESS_ATTR, "Đã xóa thành công " + deleted + " môn học khỏi bảng điểm!");
                    } else {
                        setFlash(request, FLASH_ERROR_ATTR, "Không tìm thấy môn học hợp lệ để xóa!");
                    }
                }
            } else if ("apply_simulation".equals(action)) {
                String[] gradeIds = request.getParameterValues("simGradeIds");
                String[] simLetters = request.getParameterValues("simLetters");

                if (gradeIds != null && simLetters != null && gradeIds.length == simLetters.length) {
                    List<Integer> idList = new ArrayList<>();
                    List<Double> s10List = new ArrayList<>();
                    List<Double> s4List = new ArrayList<>();
                    List<String> letterList = new ArrayList<>();

                    for (int i = 0; i < gradeIds.length; i++) {
                        try {
                            int gid = Integer.parseInt(gradeIds[i]);
                            String letter = simLetters[i] != null ? simLetters[i].trim().toUpperCase() : "";
                            Double s4 = GradeCalculatorBO.convertLetterToScore4(letter);
                            Double minS10 = GradeCalculatorBO.convertLetterToMinScore10(letter);
                            if (s4 != null && minS10 != null) {
                                idList.add(gid);
                                s10List.add(minS10);
                                s4List.add(s4);
                                letterList.add(letter);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    if (!idList.isEmpty()) {
                        gradeDAO.updateGradeScoresBatch(userId, idList, s10List, s4List, letterList);
                        setFlash(request, FLASH_SUCCESS_ATTR,
                                "Đã lưu " + idList.size() + " điểm dự kiến (được đánh dấu riêng, không tính vào CPA chính thức)!");
                    } else {
                        setFlash(request, FLASH_ERROR_ATTR, "Dữ liệu điểm dự kiến không hợp lệ!");
                    }
                } else {
                    setFlash(request, FLASH_ERROR_ATTR, "Không có môn học mô phỏng nào để lưu!");
                }
            } else if ("clear_predictions".equals(action)) {
                int cleared = gradeDAO.clearPredictedGrades(userId);
                setFlash(request, FLASH_SUCCESS_ATTR, "Đã xóa trạng thái dự kiến của " + cleared + " môn học!");
            } else {
                setFlash(request, FLASH_ERROR_ATTR, "Thao tác không được hỗ trợ!");
            }
        } catch (Exception e) {
            log("Lỗi xử lý POST /grades", e);
            setFlash(request, FLASH_ERROR_ATTR, "Đã xảy ra lỗi khi xử lý yêu cầu. Vui lòng kiểm tra dữ liệu và thử lại!");
        }

        response.sendRedirect(redirectTarget);
    }

    static List<Integer> parsePositiveUniqueIds(String[] rawIds) {
        Set<Integer> uniqueIds = new LinkedHashSet<>();
        if (rawIds == null) {
            return new ArrayList<>();
        }
        for (String raw : rawIds) {
            if (raw == null || raw.trim().isEmpty()) {
                continue;
            }
            try {
                int id = Integer.parseInt(raw.trim());
                if (id > 0) {
                    uniqueIds.add(id);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return new ArrayList<>(uniqueIds);
    }
}
