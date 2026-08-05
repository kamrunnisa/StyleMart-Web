package com.stylemart.dao;

import com.stylemart.model.User;
import com.stylemart.util.DBConnection;

import java.sql.*;

public class UserDAO {

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public int create(User user, String otpCode) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, phone, password_hash, otp_code, otp_expires_at, otp_sent_at) " +
                     "VALUES (?, ?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL 5 MINUTE), NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, otpCode);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public boolean verifyOtp(String email, String otp) throws SQLException {
        String sql = "UPDATE users SET is_verified = 1, otp_code = NULL, otp_expires_at = NULL, otp_sent_at = NULL " +
                     "WHERE email = ? AND otp_code = ? AND otp_expires_at > NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, otp);
            return ps.executeUpdate() == 1;
        }
    }

    /** Seconds since the last OTP was sent for this email, or -1 if none is on file. */
    public long secondsSinceLastOtp(String email) throws SQLException {
        String sql = "SELECT TIMESTAMPDIFF(SECOND, otp_sent_at, NOW()) AS secs FROM users " +
                     "WHERE email = ? AND otp_sent_at IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("secs") : -1;
            }
        }
    }

    /** Regenerates the OTP for an unverified account (used by the resend flow). */
    public boolean resendOtp(String email, String newOtp) throws SQLException {
        String sql = "UPDATE users SET otp_code = ?, otp_expires_at = DATE_ADD(NOW(), INTERVAL 5 MINUTE), " +
                     "otp_sent_at = NOW() WHERE email = ? AND is_verified = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newOtp);
            ps.setString(2, email);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setProfileImage(rs.getString("profile_image"));
        u.setVerified(rs.getBoolean("is_verified"));
        u.setBlocked(rs.getBoolean("is_blocked"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
