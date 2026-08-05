package com.stylemart.controller;

import com.google.gson.Gson;
import com.stylemart.dao.ProductDAO;
import com.stylemart.model.Product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.List;

@WebServlet("/api/products/trending")
public class TrendingProductsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TrendingProductsServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        List<Product> products;
        try {
            products = productDAO.getTrending(8);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load trending products", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            products = Collections.emptyList();
        }

        response.getWriter().write(gson.toJson(products));
    }
}
