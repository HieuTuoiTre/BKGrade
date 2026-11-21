package com.gradecalc.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gradecalc.model.Bean.Grade;
import com.gradecalc.model.Bean.SemesterSummary;
import com.gradecalc.model.Bo.DutParserBO;
import com.gradecalc.model.Bo.GradeCalculatorBO;
import com.gradecalc.model.Dao.GradeDAO;

@WebServlet("/grades")
public class GradeController extends HttpServlet {
    private DutParserBO parserBO;
    private GradeCalculatorBO calculatorBO;
    private GradeDAO gradeDAO;

    @Override
    public void init() {
        parserBO = new DutParserBO();
        calculatorBO = new GradeCalculatorBO();
        gradeDAO = new GradeDAO();
    }

    private int getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        // Khi ghép nối với Track A của Nghĩa:
        // if (session != null && session.getAttribute("currentUser") != null) {
        //     return ((User) session.getAttribute("currentUser")).getId();
        // }
        return 1; // User test mặc định
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int userId = getUserId(request);

            List<Grade> gradeList = gradeDAO.getGradeByUserId(userId);
            SemesterSummary totalSummary = calculatorBO.calculate(gradeList);
            List<SemesterSummary> semesterSummaries = calculatorBO.calculateBySemester(gradeList);

            request.setAttribute("gradeList", gradeList);
            request.setAttribute("totalSummary", totalSummary);
            request.setAttribute("semesterSummaries", semesterSummaries);

            request.getRequestDispatcher("/WEB-INF/views/grades/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Lỗi khi tải bảng điểm: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/grades/dashboard.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "import";
        }

        try {
            int userId = getUserId(request);

            if ("import".equals(action)) {
                String rawText = request.getParameter("rawDutText");
                if (rawText == null || rawText.trim().isEmpty()) {
                    request.setAttribute("errorMessage", "Bạn chưa dán dữ liệu bảng điểm vào ô nhập!");
                } else {
                    List<Grade> gradeList = parserBO.parse(rawText);
                    if (gradeList.isEmpty()) {
                        request.setAttribute("errorMessage", "Không tìm thấy dữ liệu hợp lệ. Hãy copy đúng các dòng bảng điểm từ sv.dut.udn.vn!");
                    } else {
                        gradeDAO.saveGradeList(userId, gradeList);
                        request.setAttribute("successMessage", "Đã bóc tách và lưu " + gradeList.size() + " môn học thành công!");
                    }
                }
            } else if ("update".equals(action)) {
                int gradeId = Integer.parseInt(request.getParameter("gradeId"));
                String formula = request.getParameter("formula");
                Double scoreBt = parseDoubleParam(request, "scoreBt");
                Double scoreBv = parseDoubleParam(request, "scoreBv");
                Double scoreCk = parseDoubleParam(request, "scoreCk");
                Double scoreDa = parseDoubleParam(request, "scoreDa");
                Double scoreGk = parseDoubleParam(request, "scoreGk");
                Double scoreQt = parseDoubleParam(request, "scoreQt");
                Double scoreTh = parseDoubleParam(request, "scoreTh");

                Double score10 = parseDoubleParam(request, "score10");

                // Nếu người dùng không nhập tay điểm 10, tự động tính bằng formula
                if (score10 == null && formula != null && !formula.trim().isEmpty()) {
                    score10 = GradeCalculatorBO.evaluateFormula(formula, scoreBt, scoreBv, scoreCk, scoreDa, scoreGk, scoreQt, scoreTh);
                }

                // Cách 1: score4 và scoreLetter luôn tự động quy đổi từ score10 chuẩn quy chế
                Double score4 = null;
                String scoreLetter = null;
                if (score10 != null) {
                    score4 = GradeCalculatorBO.convertScore10ToScore4(score10);
                    scoreLetter = GradeCalculatorBO.convertScore10ToLetter(score10);
                } else if ("Đạt".equals(request.getParameter("scoreLetter"))) {
                    scoreLetter = "Đạt";
                }

                Grade grade = new Grade();
                grade.setId(gradeId);
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

                gradeDAO.updateGrade(grade);
                request.setAttribute("successMessage", "Đã cập nhật điểm thành công!");

            } else if ("add".equals(action)) {
                String semester = request.getParameter("semester");
                String courseName = request.getParameter("courseName");
                double credits = Double.parseDouble(request.getParameter("credits"));
                String formula = request.getParameter("formula");

                Double scoreBt = parseDoubleParam(request, "scoreBt");
                Double scoreBv = parseDoubleParam(request, "scoreBv");
                Double scoreCk = parseDoubleParam(request, "scoreCk");
                Double scoreDa = parseDoubleParam(request, "scoreDa");
                Double scoreGk = parseDoubleParam(request, "scoreGk");
                Double scoreQt = parseDoubleParam(request, "scoreQt");
                Double scoreTh = parseDoubleParam(request, "scoreTh");

                Double score10 = parseDoubleParam(request, "score10");
                if (score10 == null && formula != null && !formula.trim().isEmpty()) {
                    score10 = GradeCalculatorBO.evaluateFormula(formula, scoreBt, scoreBv, scoreCk, scoreDa, scoreGk, scoreQt, scoreTh);
                }

                Double score4 = null;
                String scoreLetter = null;
                if (score10 != null) {
                    score4 = GradeCalculatorBO.convertScore10ToScore4(score10);
                    scoreLetter = GradeCalculatorBO.convertScore10ToLetter(score10);
                } else if (credits == 0 || "Đạt".equals(request.getParameter("scoreLetter"))) {
                    scoreLetter = "Đạt";
                }

                Grade grade = new Grade();
                grade.setUserId(userId);
                grade.setSemester(semester);
                grade.setCourseName(courseName);
                grade.setCredits(credits);
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

                gradeDAO.addManualGrade(userId, grade);
                request.setAttribute("successMessage", "Đã thêm môn học " + courseName + " thành công!");

            } else if ("delete".equals(action)) {
                int gradeId = Integer.parseInt(request.getParameter("gradeId"));
                gradeDAO.deleteGrade(gradeId);
                request.setAttribute("successMessage", "Đã xóa môn học khỏi bảng điểm!");
            }

            // Tải lại danh sách sau khi thao tác
            List<Grade> gradeList = gradeDAO.getGradeByUserId(userId);
            SemesterSummary totalSummary = calculatorBO.calculate(gradeList);
            List<SemesterSummary> semesterSummaries = calculatorBO.calculateBySemester(gradeList);

            request.setAttribute("gradeList", gradeList);
            request.setAttribute("totalSummary", totalSummary);
            request.setAttribute("semesterSummaries", semesterSummaries);

            request.getRequestDispatcher("/WEB-INF/views/grades/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("errorMessage", "Lỗi xử lý: " + e.getMessage());
            doGet(request, response);
        }
    }
}
