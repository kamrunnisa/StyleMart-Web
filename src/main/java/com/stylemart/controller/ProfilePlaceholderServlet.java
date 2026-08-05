package com.stylemart.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * TEMPORARY: the navbar's account icon points here. Real profile editing
 * lands with the User Dashboard/Profile module -- replace this mapping then.
 * Sits under /account/* so AuthFilter already requires a logged-in user.
 */
@WebServlet("/account/profile")
public class ProfilePlaceholderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/account/coming-soon.jsp").forward(request, response);
    }
}
