package com.stylemart.controller;

import com.stylemart.dao.AdminDAO;
import com.stylemart.model.Admin;
import com.stylemart.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminLoginServlet.class.getName());

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && "admin".equals(session.getAttribute("role"))) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            Admin admin = adminDAO.findByEmail(email);
            if (admin == null || !PasswordUtil.verify(password, admin.getPasswordHash())) {
                request.setAttribute("errorMessage", "Invalid admin email or password");
                request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
                return;
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("adminId", admin.getId());
            session.setAttribute("adminName", admin.getName());
            session.setAttribute("role", "admin");

            response.sendRedirect(request.getContextPath() + "/admin/dashboard");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Admin login failed", e);
            request.setAttribute("errorMessage", "Login failed: database error");
            request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
        }
    }
}
