package com.stylemart.controller;

import com.stylemart.dao.ProductDAO;
import com.stylemart.model.Product;
import com.stylemart.model.ProductImage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Product> products = productDAO.getAllForAdmin();
            request.setAttribute("products", products);

            Map<Integer, List<ProductImage>> galleryByProduct = new HashMap<>();
            for (Product p : products) {
                galleryByProduct.put(p.getId(), productDAO.getImagesWithIds(p.getId()));
            }
            request.setAttribute("galleryByProduct", galleryByProduct);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load admin product list", e);
            request.setAttribute("errorMessage", "Unable to load products right now.");
        }
        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }
}
