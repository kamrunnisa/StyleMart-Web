package com.stylemart.dao;

import com.stylemart.model.Payment;
import com.stylemart.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class PaymentDAO {

    /** The single payments row for an order, or null if it somehow doesn't exist. */
    public Payment getByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE order_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    /** Marks the payment (and its parent order) paid. Idempotent-safe: caller checks status first. */
    public void markSuccess(int orderId, String provider, String transactionId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE payments SET status = 'success', provider = ?, transaction_id = ?, " +
                                "failure_reason = NULL, paid_at = ? WHERE order_id = ?")) {
                    ps.setString(1, provider);
                    ps.setString(2, transactionId);
                    ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    ps.setInt(4, orderId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET payment_status = 'paid' WHERE id = ?")) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /** Marks the payment attempt failed; the order itself stays "placed" so the customer can retry. */
    public void markFailed(int orderId, String provider, String reason) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE payments SET status = 'failed', provider = ?, failure_reason = ?, " +
                                "transaction_id = NULL, paid_at = NULL WHERE order_id = ?")) {
                    ps.setString(1, provider);
                    ps.setString(2, reason);
                    ps.setInt(3, orderId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET payment_status = 'failed' WHERE id = ?")) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /** Resets a failed payment back to pending so the retry page has a clean state. */
    public void resetToPending(int orderId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE payments SET status = 'pending', failure_reason = NULL WHERE order_id = ?")) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET payment_status = 'pending' WHERE id = ?")) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setOrderId(rs.getInt("order_id"));
        p.setMethod(rs.getString("method"));
        p.setProvider(getStringIfPresent(rs, "provider"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setStatus(rs.getString("status"));
        p.setFailureReason(getStringIfPresent(rs, "failure_reason"));
        p.setPaidAt(rs.getTimestamp("paid_at"));
        return p;
    }

    /** Tolerates the `provider`/`failure_reason` columns not existing yet (pre-migration). */
    private String getStringIfPresent(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }
}
