package com.stylemart.controller;

import com.stylemart.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");

        try {
            authService.register(fullName, email, phone, password);

            // Redirect (not forward) so a page refresh on verify-otp.jsp re-GETs
            // instead of resubmitting this POST -- resubmitting used to hit
            // "Email is already registered" and strand the user mid-signup.
            HttpSession session = request.getSession(true);
            session.setAttribute("pendingOtpEmail", email.trim().toLowerCase());

            String encodedEmail = URLEncoder.encode(email.trim().toLowerCase(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/verify-otp.jsp?email=" + encodedEmail);

        } catch (AuthService.AuthException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }
}
