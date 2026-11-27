package com.gradecalc.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.http.HttpRequest;

//make sure that logged-in services are not accessible when not supposed to
//apply to the paths that need login
@WebFilter(urlPatterns = {"/grades/*", "/profile/*"})
public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig){}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        //cast type to access session attributes
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        HttpSession httpSession = httpServletRequest.getSession();
        boolean isLoggedIn = (httpSession != null && httpSession.getAttribute("currentUser") != null);

        if (isLoggedIn){
            filterChain.doFilter(servletRequest, servletResponse);
        }else{
            //send back to root along with an error message
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login?error=unauthorized");
        }
    }

    @Override
    public void destroy(){}
}
