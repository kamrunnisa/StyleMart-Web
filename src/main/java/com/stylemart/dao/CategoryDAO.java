package com.stylemart.dao;

import com.stylemart.model.Category;
import com.stylemart.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    /** All active top-level + child categories, used to drive the navbar mega-menu. */
    public List<Category> getAllActive() throws SQLException {
        String sql = "SELECT * FROM categories WHERE is_active = 1 ORDER BY parent_id IS NULL DESC, name ASC";
        List<Category> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    public Category getBySlug(String slug) throws SQLException {
        String sql = "SELECT * FROM categories WHERE slug = ? AND is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Category getById(int id) throws SQLException {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setSlug(rs.getString("slug"));
        c.setIconUrl(rs.getString("icon_url"));
        int parentId = rs.getInt("parent_id");
        c.setParentId(rs.wasNull() ? null : parentId);
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }
}
