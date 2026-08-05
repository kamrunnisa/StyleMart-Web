package com.stylemart.dao;

import com.stylemart.model.Address;
import com.stylemart.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {

    public List<Address> getByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";
        List<Address> addresses = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) addresses.add(mapRow(rs));
            }
        }
        return addresses;
    }

    /** Returns null if the address doesn't exist or doesn't belong to this user (ownership check baked in). */
    public Address getByIdForUser(int id, int userId) throws SQLException {
        String sql = "SELECT * FROM addresses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Address getDefaultForUser(int userId) throws SQLException {
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Inserts a new address. If it's the user's first, or isDefault was requested, makes it the default. */
    public int insert(Address a) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean makeDefault = a.isDefault() || countForUser(conn, a.getUserId()) == 0;
                if (makeDefault) {
                    clearDefault(conn, a.getUserId());
                }
                String sql = "INSERT INTO addresses " +
                        "(user_id, label, full_name, phone, address_line1, address_line2, city, state, pincode, is_default) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                int newId;
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    bindInsert(ps, a, makeDefault);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        newId = keys.next() ? keys.getInt(1) : -1;
                    }
                }
                conn.commit();
                return newId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /** Updates an existing address owned by the user; returns false if no matching row. */
    public boolean update(Address a) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (a.isDefault()) {
                    clearDefault(conn, a.getUserId());
                }
                String sql = "UPDATE addresses SET label=?, full_name=?, phone=?, address_line1=?, address_line2=?, " +
                        "city=?, state=?, pincode=?, is_default=? WHERE id=? AND user_id=?";
                boolean ok;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    bindUpdate(ps, a, a.isDefault());
                    ps.setInt(10, a.getId());
                    ps.setInt(11, a.getUserId());
                    ok = ps.executeUpdate() == 1;
                }
                conn.commit();
                return ok;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM addresses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            boolean ok = ps.executeUpdate() == 1;
            if (ok) {
                // If that happened to be the default, promote the most recent remaining one.
                try (PreparedStatement check = conn.prepareStatement(
                        "SELECT COUNT(*) FROM addresses WHERE user_id = ? AND is_default = 1")) {
                    check.setInt(1, userId);
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            try (PreparedStatement promote = conn.prepareStatement(
                                    "UPDATE addresses SET is_default = 1 WHERE user_id = ? " +
                                            "ORDER BY created_at DESC LIMIT 1")) {
                                promote.setInt(1, userId);
                                promote.executeUpdate();
                            }
                        }
                    }
                }
            }
            return ok;
        }
    }

    public boolean setDefault(int id, int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                clearDefault(conn, userId);
                boolean ok;
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE addresses SET is_default = 1 WHERE id = ? AND user_id = ?")) {
                    ps.setInt(1, id);
                    ps.setInt(2, userId);
                    ok = ps.executeUpdate() == 1;
                }
                conn.commit();
                return ok;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private int countForUser(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM addresses WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void clearDefault(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE addresses SET is_default = 0 WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void bindInsert(PreparedStatement ps, Address a, boolean isDefault) throws SQLException {
        ps.setInt(1, a.getUserId());
        ps.setString(2, a.getLabel());
        ps.setString(3, a.getFullName());
        ps.setString(4, a.getPhone());
        ps.setString(5, a.getAddressLine1());
        ps.setString(6, a.getAddressLine2());
        ps.setString(7, a.getCity());
        ps.setString(8, a.getState());
        ps.setString(9, a.getPincode());
        ps.setBoolean(10, isDefault);
    }

    /** UPDATE's SQL omits user_id from the SET list (it never changes), so the param order differs from insert. */
    private void bindUpdate(PreparedStatement ps, Address a, boolean isDefault) throws SQLException {
        ps.setString(1, a.getLabel());
        ps.setString(2, a.getFullName());
        ps.setString(3, a.getPhone());
        ps.setString(4, a.getAddressLine1());
        ps.setString(5, a.getAddressLine2());
        ps.setString(6, a.getCity());
        ps.setString(7, a.getState());
        ps.setString(8, a.getPincode());
        ps.setBoolean(9, isDefault);
    }

    private Address mapRow(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setLabel(rs.getString("label"));
        a.setFullName(rs.getString("full_name"));
        a.setPhone(rs.getString("phone"));
        a.setAddressLine1(rs.getString("address_line1"));
        a.setAddressLine2(rs.getString("address_line2"));
        a.setCity(rs.getString("city"));
        a.setState(rs.getString("state"));
        a.setPincode(rs.getString("pincode"));
        a.setDefault(rs.getBoolean("is_default"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
