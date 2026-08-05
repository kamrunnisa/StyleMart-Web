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

@WebServlet("/verify-otp")
public class VerifyOtpServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String otp = request.getParameter("otp");
        String contextPath = request.getContextPath();

        try {
            boolean verified = authService.verifyOtp(email, otp);
            if (verified) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.removeAttribute("pendingOtpEmail");
                }
                response.sendRedirect(contextPath + "/login.jsp?verified=1");
            } else {
                redirectWithError(response, contextPath, email, "Invalid or expired code. Please try again.");
            }
        } catch (AuthService.AuthException e) {
            redirectWithError(response, contextPath, email, e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Direct GET hits (bookmarked/typed URL) just show the entry form.
        request.getRequestDispatcher("/verify-otp.jsp").forward(request, response);
    }

    private void redirectWithError(HttpServletResponse response, String contextPath, String email, String message)
            throws IOException {
        String encodedEmail = URLEncoder.encode(email == null ? "" : email, StandardCharsets.UTF_8);
        String encodedError = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(contextPath + "/verify-otp.jsp?email=" + encodedEmail + "&error=" + encodedError);
    }
}
