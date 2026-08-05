package com.stylemart.dao;

import com.stylemart.model.CartItem;
import com.stylemart.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    /**
     * Adds a line, or bumps the quantity if this exact product+size+color
     * combo is already in the cart (matches the `uniq_cart_item` key).
     */
    public void addOrIncrement(int userId, int productId, String size, String color, int quantity) throws SQLException {
        String sql = "INSERT INTO cart (user_id, product_id, size, color, quantity) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setString(3, size);
            ps.setString(4, color);
            ps.setInt(5, quantity);
            ps.executeUpdate();
        }
    }

    /** Sets the quantity for one cart row outright; removes the row if it drops to 0 or below. */
    public boolean updateQuantity(int cartId, int userId, int quantity) throws SQLException {
        if (quantity <= 0) {
            return remove(cartId, userId);
        }
        String sql = "UPDATE cart SET quantity = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, cartId);
            ps.setInt(3, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean remove(int cartId, int userId) throws SQLException {
        String sql = "DELETE FROM cart WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public List<CartItem> getByUser(int userId) throws SQLException {
        String sql = "SELECT c.id, c.product_id, c.size, c.color, c.quantity, " +
                     "p.name, p.brand, p.thumbnail, p.final_price, p.stock " +
                     "FROM cart c JOIN products p ON c.product_id = p.id " +
                     "WHERE c.user_id = ? ORDER BY c.created_at DESC";
        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getInt("id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setSize(rs.getString("size"));
                    item.setColor(rs.getString("color"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setName(rs.getString("name"));
                    item.setBrand(rs.getString("brand"));
                    item.setThumbnail(rs.getString("thumbnail"));
                    item.setUnitPrice(rs.getBigDecimal("final_price"));
                    item.setStock(rs.getInt("stock"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    /** Total item count (sum of quantities), e.g. for a navbar badge. */
    public int getItemCount(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
