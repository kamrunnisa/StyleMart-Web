package com.stylemart.dao;

import com.stylemart.model.Coupon;
import com.stylemart.util.DBConnection;

import java.sql.*;

public class CouponDAO {

    public Coupon findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM coupons WHERE code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code == null ? "" : code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Coupon findById(int id) throws SQLException {
        String sql = "SELECT * FROM coupons WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    private Coupon mapRow(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDescription(rs.getString("description"));
        c.setDiscountType(rs.getString("discount_type"));
        c.setDiscountValue(rs.getBigDecimal("discount_value"));
        c.setMinOrderValue(rs.getBigDecimal("min_order_value"));
        c.setMaxDiscount(rs.getBigDecimal("max_discount"));
        c.setValidFrom(rs.getDate("valid_from"));
        c.setValidUntil(rs.getDate("valid_until"));
        int usageLimit = rs.getInt("usage_limit");
        c.setUsageLimit(rs.wasNull() ? null : usageLimit);
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }
}
