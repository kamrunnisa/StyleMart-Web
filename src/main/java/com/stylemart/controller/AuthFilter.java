package com.stylemart.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Blocks access to /account/* and /admin/* for anyone without a valid session.
 * Registered in web.xml. Also enforces the "admin" role for /admin/*.
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String path = request.getRequestURI();
        boolean loggedIn = session != null && session.getAttribute("userId") != null;
        boolean isAdminPath = path.contains("/admin/");
        boolean isAdmin = session != null && "admin".equals(session.getAttribute("role"));
        boolean isAdminLoginPath = path.endsWith("/admin/login");

        if (isAdminLoginPath) {
            chain.doFilter(req, res);
            return;
        }

        if (isAdminPath) {
            if (!isAdmin) {
                response.sendRedirect(request.getContextPath() + "/admin/login");
                return;
            }
            chain.doFilter(req, res);
            return;
        }

        if (!loggedIn) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        chain.doFilter(req, res);
    }
}
