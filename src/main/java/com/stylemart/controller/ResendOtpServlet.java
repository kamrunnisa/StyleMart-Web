package com.stylemart.controller;

import com.stylemart.service.AuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/resend-otp")
public class ResendOtpServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String contextPath = request.getContextPath();

        try {
            authService.resendOtp(email);
            redirect(response, contextPath, email, "sent", "A new code has been sent to your email.");
        } catch (AuthService.AuthException e) {
            redirect(response, contextPath, email, "error", e.getMessage());
        }
    }

    private void redirect(HttpServletResponse response, String contextPath, String email,
                           String statusKey, String message) throws IOException {
        String encodedEmail = URLEncoder.encode(email == null ? "" : email, StandardCharsets.UTF_8);
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(contextPath + "/verify-otp.jsp?email=" + encodedEmail + "&" + statusKey + "=" + encodedMessage);
    }
}
