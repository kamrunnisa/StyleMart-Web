package com.stylemart.dao;

import com.stylemart.model.Product;
import com.stylemart.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    /**
     * Single flexible entry point for the category / search / flag listing page.
     * Builds SQL dynamically with placeholders only for the filters actually set,
     * so every value still goes through PreparedStatement.
     */
    public ProductPage search(ProductFilter f) throws SQLException {
        StringBuilder where = new StringBuilder(" WHERE status = 'active' ");
        List<Object> params = new ArrayList<>();

        if (f.getCategoryId() != null) {
            where.append(" AND category_id = ? ");
            params.add(f.getCategoryId());
        }
        if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
            where.append(" AND (name LIKE ? OR brand LIKE ? OR description LIKE ?) ");
            String like = "%" + f.getKeyword().trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (f.getFlag() != null && !f.getFlag().isBlank()) {
            String column = switch (f.getFlag()) {
                case "trending" -> "is_trending";
                case "new_arrival" -> "is_new_arrival";
                case "best_seller" -> "is_best_seller";
                case "featured" -> "is_featured";
                case "flash_sale" -> "is_flash_sale";
                default -> null;
            };
            if (column != null) {
                where.append(" AND ").append(column).append(" = 1 ");
            }
        }
        if (f.getBrand() != null && !f.getBrand().isBlank()) {
            where.append(" AND brand = ? ");
            params.add(f.getBrand());
        }
        if (f.getSize() != null && !f.getSize().isBlank()) {
            where.append(" AND FIND_IN_SET(?, sizes) > 0 ");
            params.add(f.getSize());
        }
        if (f.getMinPrice() != null) {
            where.append(" AND final_price >= ? ");
            params.add(f.getMinPrice());
        }
        if (f.getMaxPrice() != null) {
            where.append(" AND final_price <= ? ");
            params.add(f.getMaxPrice());
        }

        String orderBy = switch (f.getSort() == null ? "" : f.getSort()) {
            case "price_low" -> " ORDER BY final_price ASC ";
            case "price_high" -> " ORDER BY final_price DESC ";
            case "newest" -> " ORDER BY created_at DESC ";
            case "rating" -> " ORDER BY avg_rating DESC ";
            default -> " ORDER BY is_trending DESC, created_at DESC "; // popularity default
        };

        int total = countMatches(where, params);

        String sql = "SELECT * FROM products " + where + orderBy + " LIMIT ? OFFSET ?";
        List<Product> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = bindParams(ps, params);
            ps.setInt(idx++, f.getPageSize());
            ps.setInt(idx, f.getOffset());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return new ProductPage(results, total, f.getPage(), f.getPageSize());
    }

    private int countMatches(StringBuilder where, List<Object> params) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products " + where;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        int idx = 1;
        for (Object param : params) {
            ps.setObject(idx++, param);
        }
        return idx;
    }

    public Product getById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ? AND status = 'active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Same lookup as getById, but for admin screens -- also returns inactive products. */
    public Product getByIdForAdmin(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Updates every editable field from the admin "Edit Product" form. Thumbnail is untouched here --
     *  that still goes through updateThumbnail() via the image upload flow. */
    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, name = ?, brand = ?, description = ?, " +
                "price = ?, discount_percent = ?, sizes = ?, colors = ?, stock = ?, status = ?, " +
                "is_trending = ?, is_new_arrival = ?, is_best_seller = ?, is_featured = ?, is_flash_sale = ? " +
                "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getCategoryId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getBrand());
            ps.setString(4, p.getDescription());
            ps.setBigDecimal(5, p.getPrice());
            ps.setBigDecimal(6, p.getDiscountPercent());
            ps.setString(7, p.getSizes());
            ps.setString(8, p.getColors());
            ps.setInt(9, p.getStock());
            ps.setString(10, p.getStatus());
            ps.setBoolean(11, p.isTrending());
            ps.setBoolean(12, p.isNewArrival());
            ps.setBoolean(13, p.isBestSeller());
            ps.setBoolean(14, p.isFeatured());
            ps.setBoolean(15, p.isFlashSale());
            ps.setInt(16, p.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /** Gallery images for the product-details page, ordered for display. */
    public List<String> getImages(int productId) throws SQLException {
        String sql = "SELECT image_url FROM product_images WHERE product_id = ? ORDER BY sort_order ASC";
        List<String> images = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    images.add(rs.getString("image_url"));
                }
            }
        }
        return images;
    }

    /** "You may also like" strip on the product-details page. */
    public List<Product> getRelated(int categoryId, int excludeProductId, int limit) throws SQLException {
        String sql = "SELECT * FROM products WHERE category_id = ? AND id <> ? AND status = 'active' " +
                     "ORDER BY avg_rating DESC LIMIT ?";
        List<Product> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setInt(2, excludeProductId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /** Distinct brand list for the filter sidebar, scoped to a category when given. */
    public List<String> getDistinctBrands(Integer categoryId) throws SQLException {
        String sql = categoryId == null
                ? "SELECT DISTINCT brand FROM products WHERE status = 'active' AND brand IS NOT NULL ORDER BY brand"
                : "SELECT DISTINCT brand FROM products WHERE status = 'active' AND brand IS NOT NULL AND category_id = ? ORDER BY brand";
        List<String> brands = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (categoryId != null) {
                ps.setInt(1, categoryId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    brands.add(rs.getString(1));
                }
            }
        }
        return brands;
    }

    public List<Product> getTrending(int limit) throws SQLException {
        String sql = "SELECT * FROM products WHERE is_trending = 1 AND status = 'active' " +
                     "ORDER BY created_at DESC LIMIT ?";
        List<Product> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /** Full catalog for the admin dashboard -- no status filter, simple id order. */
    public List<Product> getAllForAdmin() throws SQLException {
        String sql = "SELECT * FROM products ORDER BY id DESC";
        List<Product> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    /** Creates a new product from the admin "Add Product" form and returns its generated id. */
    public int create(Product p) throws SQLException {
        String sql = "INSERT INTO products " +
                "(category_id, name, brand, description, price, discount_percent, sizes, colors, stock, " +
                "thumbnail, is_trending, is_new_arrival, is_best_seller, is_featured, is_flash_sale, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'active')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getCategoryId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getBrand());
            ps.setString(4, p.getDescription());
            ps.setBigDecimal(5, p.getPrice());
            ps.setBigDecimal(6, p.getDiscountPercent());
            ps.setString(7, p.getSizes());
            ps.setString(8, p.getColors());
            ps.setInt(9, p.getStock());
            ps.setString(10, p.getThumbnail());
            ps.setBoolean(11, p.isTrending());
            ps.setBoolean(12, p.isNewArrival());
            ps.setBoolean(13, p.isBestSeller());
            ps.setBoolean(14, p.isFeatured());
            ps.setBoolean(15, p.isFlashSale());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    /** Only the filename is stored in the DB -- the upload servlet handles the actual file. */
    public boolean updateThumbnail(int productId, String filename) throws SQLException {
        String sql = "UPDATE products SET thumbnail = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, filename);
            ps.setInt(2, productId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Adds one more gallery photo for a product (does not touch the thumbnail). */
    public boolean addImage(int productId, String filename) throws SQLException {
        String sql = "INSERT INTO product_images (product_id, image_url, sort_order) " +
                     "VALUES (?, ?, (SELECT COALESCE(MAX(sort_order), 0) + 1 FROM product_images pi WHERE pi.product_id = ?))";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, filename);
            ps.setInt(3, productId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Removes one gallery photo, scoped to the product it claims to belong to. */
    public boolean deleteImage(int imageId, int productId) throws SQLException {
        String sql = "DELETE FROM product_images WHERE id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, imageId);
            ps.setInt(2, productId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Gallery images with their row ids, for the admin dashboard's delete buttons. */
    public List<com.stylemart.model.ProductImage> getImagesWithIds(int productId) throws SQLException {
        String sql = "SELECT id, image_url FROM product_images WHERE product_id = ? ORDER BY sort_order ASC";
        List<com.stylemart.model.ProductImage> images = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    images.add(new com.stylemart.model.ProductImage(rs.getInt("id"), rs.getString("image_url")));
                }
            }
        }
        return images;
    }

    Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setName(rs.getString("name"));
        p.setBrand(rs.getString("brand"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        p.setFinalPrice(rs.getBigDecimal("final_price"));
        p.setSizes(rs.getString("sizes"));
        p.setColors(rs.getString("colors"));
        p.setStock(rs.getInt("stock"));
        p.setThumbnail(rs.getString("thumbnail"));
        p.setStatus(rs.getString("status"));
        p.setAvgRating(rs.getDouble("avg_rating"));
        p.setTotalReviews(rs.getInt("total_reviews"));
        return p;
    }
}
