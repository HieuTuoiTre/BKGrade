package com.gradecalc.controller;

import com.gradecalc.model.Bo.UserBO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

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

}
