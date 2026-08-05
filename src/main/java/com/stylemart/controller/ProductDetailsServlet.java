package com.stylemart.controller;

import com.stylemart.dao.CategoryDAO;
import com.stylemart.dao.ProductDAO;
import com.stylemart.dao.WishlistDAO;
import com.stylemart.model.Category;
import com.stylemart.model.Product;

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
import java.util.List;

@WebServlet("/product")
public class ProductDetailsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProductDetailsServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final WishlistDAO wishlistDAO = new WishlistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = parseInt(request.getParameter("id"));
        if (id <= 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Product product = productDAO.getById(id);
            if (product == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            List<String> images = productDAO.getImages(id);
            List<Product> related = productDAO.getRelated(product.getCategoryId(), id, 4);
            Category category = categoryDAO.getById(product.getCategoryId());

            HttpSession session = request.getSession(false);
            Integer userId = session == null ? null : (Integer) session.getAttribute("userId");
            boolean inWishlist = userId != null && wishlistDAO.isInWishlist(userId, id);

            request.setAttribute("product", product);
            request.setAttribute("images", images);
            request.setAttribute("related", related);
            request.setAttribute("category", category);
            request.setAttribute("inWishlist", inWishlist);

            request.getRequestDispatcher("/WEB-INF/views/product-details.jsp").forward(request, response);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load product id=" + id, e);
            request.setAttribute("errorMessage", "Unable to load this product right now. Please try again shortly.");
            request.getRequestDispatcher("/WEB-INF/views/error/500.jsp").forward(request, response);
        }
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
