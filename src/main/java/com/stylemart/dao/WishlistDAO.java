package com.stylemart.dao;

import com.stylemart.model.Product;
import com.stylemart.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO {

    private final ProductDAO productDAO = new ProductDAO();

    public boolean isInWishlist(int userId, int productId) throws SQLException {
        String sql = "SELECT id FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Adds if absent, removes if present. Returns the new state (true = now saved). */
    public boolean toggle(int userId, int productId) throws SQLException {
        if (isInWishlist(userId, productId)) {
            remove(userId, productId);
            return false;
        }
        String sql = "INSERT INTO wishlist (user_id, product_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
        return true;
    }

    public boolean remove(int userId, int productId) throws SQLException {
        String sql = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Product> getByUser(int userId) throws SQLException {
        String sql = "SELECT p.* FROM wishlist w JOIN products p ON w.product_id = p.id " +
                     "WHERE w.user_id = ? ORDER BY w.created_at DESC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(productDAO.mapRow(rs));
                }
            }
        }
        return products;
    }

    public int getCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM wishlist WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
