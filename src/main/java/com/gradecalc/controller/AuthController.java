package com.gradecalc.controller;

import com.gradecalc.model.Bean.User;
import com.gradecalc.model.Bo.UserBO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

//only call this controller if the request end in those endings
@WebServlet(urlPatterns = {"/login", "/register", "/logout", "/profile"})
public class AuthController extends HttpServlet {
    private final UserBO userBO = new UserBO();

    //redirection
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();
        switch (path){
            //simple redirection the desired pages
            case "/login":
                request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
                break;
            case "/resigter":
                request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
                break;
            case "/profile":
                request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
                break;
            //special case, destroy the session and return back to login anew
            case "/logout":
                HttpSession session = request.getSession(false);
                if (session != null){
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/login");
                break;
        }
    }

    //when user login via "/login" or "/register", they send a POST form
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String path = request.getServletPath();

        if ("/login".equals(path)){
            handleLogin(request, response);
        } else if ("/register".equals(path)){
            handleRegister(request, response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        try {
            //check user's credential
            User user = userBO.login(username, password);
            if (user != null){
                HttpSession session = request.getSession();
                session.setAttribute("currentUser", user);
                response.sendRedirect(request.getContextPath() + "/grades");
            }else {
                request.setAttribute("Error","Sai tên đăng nhập hoặc mật khẩu.");
                request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            }
        } catch (SQLException | InterruptedException e){
            throw new ServletException("Lỗi kết nối cơ sở dữ liệu khi đăng nhập: ", e);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String fullname = request.getParameter("fullname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try{
            User user = new User();
            user.setFullname(fullname);
            user.setUsername(username);

            userBO.register(user, password);

            response.sendRedirect(request.getContextPath() + "/login?registered=true");
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
        } catch (SQLException | InterruptedException e){
            throw new ServletException("Lỗi kết nối cơ sở dữ liệu khi đăng ký tài khoản: ", e);
        }
    }

}
