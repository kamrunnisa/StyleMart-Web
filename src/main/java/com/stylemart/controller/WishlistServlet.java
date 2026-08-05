package com.stylemart.controller;

import com.stylemart.dao.WishlistDAO;
import com.stylemart.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Real wishlist, backed by the `wishlist` table.
 *   GET  /wishlist        -> the wishlist page
 *   POST /wishlist/toggle -> add/remove a product (used by the product-details heart button)
 */
@WebServlet({"/wishlist", "/wishlist/toggle"})
public class WishlistServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(WishlistServlet.class.getName());
    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!"/wishlist".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Integer userId = currentUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?redirect=wishlist");
            return;
        }

        try {
            List<Product> items = wishlistDAO.getByUser(userId);
            request.setAttribute("wishlistItems", items);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load wishlist for userId=" + userId, e);
            request.setAttribute("errorMessage", "Unable to load your wishlist right now.");
        }
        request.getRequestDispatcher("/wishlist.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer userId = currentUserId(request);
        if (userId == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "{\"loggedIn\":false}");
            return;
        }

        if (!"/wishlist/toggle".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int productId = parseInt(request.getParameter("productId"), -1);
        if (productId <= 0) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"Invalid product\"}");
            return;
        }

        try {
            boolean nowSaved = wishlistDAO.toggle(userId, productId);
            int count = wishlistDAO.getCount(userId);
            writeJson(response, HttpServletResponse.SC_OK,
                    "{\"saved\":" + nowSaved + ",\"wishlistCount\":" + count + "}");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Wishlist toggle failed for userId=" + userId + " productId=" + productId, e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "{\"error\":\"Database error\"}");
        }
    }

    private Integer currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Integer) session.getAttribute("userId");
    }

    private void writeJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }

    private int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
