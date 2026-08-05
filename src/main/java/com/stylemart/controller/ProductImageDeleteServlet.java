package com.stylemart.controller;

import com.stylemart.dao.ProductDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Admin-only gallery photo removal. Sits under /admin/*, so AuthFilter
 * already guarantees only an authenticated admin reaches this.
 */
@WebServlet("/admin/products/delete-image")
public class ProductImageDeleteServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProductImageDeleteServlet.class.getName());
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String contextPath = request.getContextPath();
        int imageId = parseInt(request.getParameter("imageId"));
        int productId = parseInt(request.getParameter("productId"));

        if (imageId <= 0 || productId <= 0) {
            redirect(response, contextPath, "error", "Invalid image or product");
            return;
        }

        try {
            boolean deleted = productDAO.deleteImage(imageId, productId);
            redirect(response, contextPath, deleted ? "success" : "error",
                    deleted ? "Gallery photo removed" : "Photo not found");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete image id=" + imageId, e);
            redirect(response, contextPath, "error", "Delete failed: database error");
        }
    }

    private void redirect(HttpServletResponse response, String contextPath, String key, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(contextPath + "/admin/dashboard?" + key + "=" + encoded);
    }

    private int parseInt(String raw) {
        if (raw == null || raw.isBlank()) return -1;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
