package com.stylemart.controller;

import com.stylemart.dao.CategoryDAO;
import com.stylemart.dao.ProductDAO;
import com.stylemart.model.Category;
import com.stylemart.model.Product;
import com.stylemart.util.FileUploadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Admin "Add Product" form. Sits under /admin/*, so AuthFilter already
 * guarantees an admin session before either method here runs.
 *   GET  /admin/products/new     -> the form
 *   POST /admin/products/create  -> validate, insert, optionally save a main image
 */
@WebServlet("/admin/products/*")
@MultipartConfig(
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024
)
public class AdminProductServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminProductServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo(); // everything after /admin/products

        if ("/new".equals(path)) {
            try {
                List<Category> categories = categoryDAO.getAllActive();
                request.setAttribute("categories", categories);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to load categories for add-product form", e);
                request.setAttribute("errorMessage", "Unable to load categories right now.");
            }
            request.getRequestDispatcher("/WEB-INF/views/admin/add-product.jsp").forward(request, response);
            return;
        }

        if ("/edit".equals(path)) {
            int productId = parseInt(request.getParameter("id"), -1);
            if (productId <= 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            try {
                Product product = productDAO.getByIdForAdmin(productId);
                if (product == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("product", product);
                request.setAttribute("categories", categoryDAO.getAllActive());
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to load product id=" + productId + " for edit form", e);
                request.setAttribute("errorMessage", "Unable to load this product right now.");
            }
            request.getRequestDispatcher("/WEB-INF/views/admin/edit-product.jsp").forward(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();
        String contextPath = request.getContextPath();

        if ("/update".equals(path)) {
            handleUpdate(request, response, contextPath);
            return;
        }

        if (!"/create".equals(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String name = trim(request.getParameter("name"));
        String brand = trim(request.getParameter("brand"));
        String description = trim(request.getParameter("description"));
        int categoryId = parseInt(request.getParameter("categoryId"), -1);
        BigDecimal price = parseDecimal(request.getParameter("price"));
        BigDecimal discountPercent = parseDecimal(request.getParameter("discountPercent"));
        String sizes = joinChecked(request.getParameterValues("sizes"));
        String colors = trim(request.getParameter("colors"));
        int stock = parseInt(request.getParameter("stock"), 0);

        if (isBlank(name) || isBlank(brand) || categoryId <= 0 || price == null || price.signum() < 0) {
            redirectToForm(request, response, "Please fill in product name, brand, category and a valid price.");
            return;
        }
        if (discountPercent == null) discountPercent = BigDecimal.ZERO;

        Product p = new Product();
        p.setName(name);
        p.setBrand(brand);
        p.setDescription(description);
        p.setCategoryId(categoryId);
        p.setPrice(price);
        p.setDiscountPercent(discountPercent);
        p.setSizes(sizes);
        p.setColors(colors);
        p.setStock(Math.max(stock, 0));
        p.setTrending(request.getParameter("isTrending") != null);
        p.setNewArrival(request.getParameter("isNewArrival") != null);
        p.setBestSeller(request.getParameter("isBestSeller") != null);
        p.setFeatured(request.getParameter("isFeatured") != null);
        p.setFlashSale(request.getParameter("isFlashSale") != null);

        // Optional main image, uploaded the same way ProductImageUploadServlet does.
        String savedFilename = null;
        try {
            Part filePart = request.getPart("productImage");
            if (filePart != null && filePart.getSize() > 0) {
                String originalFilename = filePart.getSubmittedFileName();
                if (!FileUploadUtil.isAllowedExtension(originalFilename)) {
                    redirectToForm(request, response, "Only JPG, JPEG, PNG, or WEBP images are allowed.");
                    return;
                }
                String contentType = filePart.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectToForm(request, response, "Uploaded file is not a valid image.");
                    return;
                }
                Path targetDir;
                try {
                    targetDir = FileUploadUtil.resolveUploadDir();
                } catch (Exception ex) {
                    redirectToForm(request, response, "Server storage path is not available.");
                    return;
                }
                Path destination = FileUploadUtil.resolveSafeUniquePath(targetDir, originalFilename);
                filePart.write(destination.toString());
                savedFilename = destination.getFileName().toString();
            }
        } catch (Exception e) {
            // Thrown by the container when maxFileSize/maxRequestSize is exceeded, or a bad multipart body.
            redirectToForm(request, response, "Image is too large or invalid. Max size is 5 MB.");
            return;
        }
        p.setThumbnail(savedFilename); // null is fine -- dashboard/listing pages already fall back to a placeholder

        try {
            int productId = productDAO.create(p);
            String encoded = URLEncoder.encode("Product \"" + name + "\" added (ID #" + productId + ")", StandardCharsets.UTF_8);
            response.sendRedirect(contextPath + "/admin/dashboard?success=" + encoded);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create product name=" + name, e);
            redirectToForm(request, response, "Could not save the product: database error.");
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response, String contextPath)
            throws IOException {

        int productId = parseInt(request.getParameter("id"), -1);
        if (productId <= 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String name = trim(request.getParameter("name"));
        String brand = trim(request.getParameter("brand"));
        String description = trim(request.getParameter("description"));
        int categoryId = parseInt(request.getParameter("categoryId"), -1);
        BigDecimal price = parseDecimal(request.getParameter("price"));
        BigDecimal discountPercent = parseDecimal(request.getParameter("discountPercent"));
        String sizes = joinChecked(request.getParameterValues("sizes"));
        String colors = trim(request.getParameter("colors"));
        int stock = parseInt(request.getParameter("stock"), 0);
        String status = trim(request.getParameter("status"));

        if (isBlank(name) || isBlank(brand) || categoryId <= 0 || price == null || price.signum() < 0) {
            redirectToEdit(request, response, productId, "Please fill in product name, brand, category and a valid price.");
            return;
        }
        if (discountPercent == null) discountPercent = BigDecimal.ZERO;
        if (isBlank(status) || !(status.equals("active") || status.equals("inactive"))) {
            status = "active";
        }

        Product p = new Product();
        p.setId(productId);
        p.setName(name);
        p.setBrand(brand);
        p.setDescription(description);
        p.setCategoryId(categoryId);
        p.setPrice(price);
        p.setDiscountPercent(discountPercent);
        p.setSizes(sizes);
        p.setColors(colors);
        p.setStock(Math.max(stock, 0));
        p.setStatus(status);
        p.setTrending(request.getParameter("isTrending") != null);
        p.setNewArrival(request.getParameter("isNewArrival") != null);
        p.setBestSeller(request.getParameter("isBestSeller") != null);
        p.setFeatured(request.getParameter("isFeatured") != null);
        p.setFlashSale(request.getParameter("isFlashSale") != null);

        try {
            boolean updated = productDAO.update(p);
            if (!updated) {
                redirectToEdit(request, response, productId, "Could not save changes: product not found.");
                return;
            }
            String encoded = URLEncoder.encode("Product \"" + name + "\" updated", StandardCharsets.UTF_8);
            response.sendRedirect(contextPath + "/admin/dashboard?success=" + encoded);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update product id=" + productId, e);
            redirectToEdit(request, response, productId, "Could not save changes: database error.");
        }
    }

    private void redirectToEdit(HttpServletRequest request, HttpServletResponse response, int productId, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/admin/products/edit?id=" + productId + "&error=" + encoded);
    }

    private void redirectToForm(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/admin/products/new?error=" + encoded);
    }

    private String joinChecked(String[] values) {
        return values == null ? null : String.join(",", values);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
