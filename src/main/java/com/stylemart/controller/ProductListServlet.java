package com.stylemart.controller;

import com.stylemart.dao.CategoryDAO;
import com.stylemart.dao.ProductDAO;
import com.stylemart.dao.ProductFilter;
import com.stylemart.dao.ProductPage;
import com.stylemart.model.Category;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles /products for three cases that all share the same grid UI:
 *   - Category browse:  products?category=men
 *   - Free-text search:  products?search=shirt
 *   - Flag-based lists:  products?flag=flash_sale
 * Any combination of category/search/flag/sort/price/brand/size/page can be
 * combined, matching how a real Myntra-style filter sidebar works.
 */
@WebServlet("/products")
public class ProductListServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProductListServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ProductFilter filter = new ProductFilter();
        Category activeCategory = null;

        try {
            String categorySlug = request.getParameter("category");
            if (categorySlug != null && !categorySlug.isBlank()) {
                activeCategory = categoryDAO.getBySlug(categorySlug);
                if (activeCategory != null) {
                    filter.setCategoryId(activeCategory.getId());
                }
            }

            filter.setKeyword(request.getParameter("search"));
            filter.setFlag(request.getParameter("flag"));
            filter.setBrand(request.getParameter("brand"));
            filter.setSize(request.getParameter("size"));
            filter.setSort(request.getParameter("sort"));
            filter.setMinPrice(parseDecimal(request.getParameter("minPrice")));
            filter.setMaxPrice(parseDecimal(request.getParameter("maxPrice")));

            int page = parseInt(request.getParameter("page"), 1);
            filter.setPage(page);

            ProductPage results = productDAO.search(filter);
            List<String> brands = productDAO.getDistinctBrands(filter.getCategoryId());
            List<Category> categories = categoryDAO.getAllActive();

            request.setAttribute("results", results);
            request.setAttribute("brands", brands);
            request.setAttribute("categories", categories);
            request.setAttribute("activeCategory", activeCategory);
            request.setAttribute("filter", filter);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load product listing", e);
            request.setAttribute("errorMessage", "Unable to load products right now. Please try again shortly.");
        }

        request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request, response);
    }

    private BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            return null;
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
