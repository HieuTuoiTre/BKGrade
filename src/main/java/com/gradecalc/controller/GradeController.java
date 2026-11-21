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

@WebServlet ("/grades")
public class GradeController extends HttpServlet {
    private DutParserBO parserBO;
    private GradeCalculatorBO calculatorBO;
    private GradeDAO gradeDAO;

    @Override 
    public void init(){           // Khởi tạo khi Tomcat load Servlet
        parserBO = new DutParserBO();
        calculatorBO = new GradeCalculatorBO();
        gradeDAO = new GradeDAO();
    }
    @Override 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{       // Xử lí khi người dùng vào trang
        try {
            // 1. Lấy user đang đăng nhập từ session.
            HttpSession session = request.getSession();
            // int userId = ((User) session.getAttribute("currentUser")).getId();
            int userId = 1;  // hardcode tạm để test

            // 2. Lấy danh sách bảng điểm từ database
            List<Grade> gradeList = gradeDAO.getGradeByUserId(userId);

            // 3. Tính tổng kết toàn khóa
            SemesterSummary totalSummary = calculatorBO.calculate(gradeList);

            // 4. Tính tổng kết từng học kì
            List<SemesterSummary> semesterSummaries = calculatorBO.calculateBySemester(gradeList);

            // 5. Gắn vào request để JSP đọc được
            request.setAttribute("gradeList", gradeList);
            request.setAttribute("totalSummary", totalSummary);
            request.setAttribute("semesterSummaries", semesterSummaries);

            // 6. Chuyển sang giao diện
            request.getRequestDispatcher("/WEB-INF/views/grades/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Lỗi khi tải danh sách điểm: " + e.getMessage(), e);
        }
    }

    @Override 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{           // Xửa lí khi người dùng bấm nút gửi form
        try {
            // 1. Nhận chuỗi text người dùng paste vào
            String rawText = request.getParameter("rawDutText");

            if(rawText == null || rawText.trim().isEmpty()){
                request.setAttribute("errorMessage", "Bạn chưa dán dữ liệu bảng điểm vào ô nhập!");
                request.getRequestDispatcher("/WEB-INF/views/grades/dashboard.jsp").forward(request, response);
                return;
            }

            // 2. Bóc tách
            List<Grade> gradeList = parserBO.parse(rawText);

            if(gradeList.isEmpty()){
                request.setAttribute("errorMessage", "Không tìm thấy dữ liệu hợp lệ. Hãy copy đúng bảng điểm từ sv.dut.udn.vn!");
                request.getRequestDispatcher("/WEB-INF/views/grades/dashboard.jsp");
                return;
            }

            // 3. Lấy userId từ session
            // int userId = ((User) request.getSession().getAttribute("currentUser")).getId();
            int userId = 1; // Dùng tạm để test

            // 4. Lưu vào DB
            gradeDAO.saveGradeList(userId, gradeList);

            // 5. Tính toán tổng kết
            SemesterSummary totalSummary = calculatorBO.calculate(gradeList);
            List<SemesterSummary> semesterSummaries = calculatorBO.calculateBySemester(gradeList);

            // 6. Thông báo thành công
            request.setAttribute("successMessage", "Đã bóc tách và lưu" + gradeList.size() + "môn học thành công!");

            // 7. Gắn data vào request
            request.setAttribute("gradeList", gradeList);
            request.setAttribute("totalSummary", totalSummary);
            request.setAttribute("semesterSummaries", semesterSummaries);

            // 8. Chuyển sang dashboard (FORWARD, không REDIRECT)
            request.getRequestDispatcher("/WEB-INF/views/grades/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Lỗi hệ thống " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/grades/dashboard.jsp").forward(request, response);
        }
    }
}
