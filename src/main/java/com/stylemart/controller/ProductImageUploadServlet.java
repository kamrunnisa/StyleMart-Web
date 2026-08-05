package com.stylemart.controller;

import com.stylemart.dao.ProductDAO;
import com.stylemart.util.FileUploadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Admin-only product image upload. Reachable at /admin/products/upload-image,
 * which sits under the /admin/* URL pattern the AuthFilter already protects,
 * so this never has to re-check the session role itself.
 */
@WebServlet("/admin/products/upload-image")
@MultipartConfig(
        maxFileSize = 5L * 1024 * 1024,       // 5 MB per file (enforced again below, defense in depth)
        maxRequestSize = 6L * 1024 * 1024
)
public class ProductImageUploadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProductImageUploadServlet.class.getName());

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String contextPath = request.getContextPath();
        int productId = parseInt(request.getParameter("productId"));

        if (productId <= 0) {
            redirect(response, contextPath, "error", "Missing or invalid product");
            return;
        }

        Part filePart;
        try {
            filePart = request.getPart("productImage");
        } catch (Exception e) {
            // Thrown by the container when maxFileSize/maxRequestSize is exceeded.
            redirect(response, contextPath, "error", "File is too large. Max size is 5 MB.");
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            redirect(response, contextPath, "error", "Please choose an image file");
            return;
        }
        if (filePart.getSize() > FileUploadUtil.MAX_FILE_SIZE_BYTES) {
            redirect(response, contextPath, "error", "File is too large. Max size is 5 MB.");
            return;
        }

        String originalFilename = filePart.getSubmittedFileName();
        if (!FileUploadUtil.isAllowedExtension(originalFilename)) {
            redirect(response, contextPath, "error", "Only JPG, JPEG, PNG, or WEBP images are allowed");
            return;
        }

        // Belt-and-braces: also check the declared content type, not just the extension.
        String contentType = filePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            redirect(response, contextPath, "error", "Uploaded file is not a valid image");
            return;
        }

        boolean asGallery = "gallery".equals(request.getParameter("mode"));

        try {
            Path targetDir;
            try {
                targetDir = FileUploadUtil.resolveUploadDir();
            } catch (Exception ex) {
                redirect(response, contextPath, "error", "Server storage path is not available");
                return;
            }

            Path destination = FileUploadUtil.resolveSafeUniquePath(targetDir, originalFilename);
            filePart.write(destination.toString());

            String savedFilename = destination.getFileName().toString();
            boolean updated = asGallery
                    ? productDAO.addImage(productId, savedFilename)
                    : productDAO.updateThumbnail(productId, savedFilename);

            if (!updated) {
                Files.deleteIfExists(destination); // don't leave an orphaned file behind
                redirect(response, contextPath, "error", "Product not found");
                return;
            }

            String what = asGallery ? "Gallery photo added for" : "Main image updated for";
            redirect(response, contextPath, "success", what + " product #" + productId);

        } catch (SecurityException e) {
            redirect(response, contextPath, "error", "Invalid file name");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Image upload DB update failed for productId=" + productId, e);
            redirect(response, contextPath, "error", "Upload failed: database error");
        } catch (IOException e) {
            redirect(response, contextPath, "error", "Upload failed: could not save file");
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
