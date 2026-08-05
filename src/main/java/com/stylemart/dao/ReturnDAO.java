package com.stylemart.dao;

import com.stylemart.model.Refund;
import com.stylemart.model.ReturnRequest;
import com.stylemart.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class ReturnDAO {

    private static final String[] STAGES = {
            "requested", "pickup_scheduled", "picked_up", "refund_initiated", "refund_completed"
    };

    /** Creates the return request and (consistent with the existing coarse state machine) marks the order 'returned'. */
    public int create(int orderId, int userId, String reason, String comment) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String status;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT status FROM orders WHERE id = ? AND user_id = ? FOR UPDATE")) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); return -1; }
                        status = rs.getString("status");
                    }
                }
                if (!"delivered".equals(status)) {
                    conn.rollback();
                    return -1;
                }

                int returnId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO returns (order_id, user_id, reason, comment) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, userId);
                    ps.setString(3, reason);
                    ps.setString(4, comment);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        returnId = keys.next() ? keys.getInt(1) : -1;
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET status = 'returned' WHERE id = ?")) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                conn.commit();
                return returnId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public ReturnRequest getByOrder(int orderId, int userId) throws SQLException {
        String sql = "SELECT * FROM returns WHERE order_id = ? AND user_id = ? ORDER BY id DESC LIMIT 1";
        ReturnRequest r;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                r = map(rs);
            }
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM refunds WHERE return_id = ?")) {
            ps.setInt(1, r.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) r.setRefund(mapRefund(rs));
            }
        }
        return r;
    }

    /**
     * Demo-only helper (no logistics/finance backend behind this): steps a return
     * forward one stage, e.g. requested -> pickup_scheduled -> ... -> refund_completed.
     * Creates the `refunds` row the moment refund_initiated is reached, and completes
     * it (and sets orders.payment_status = 'refunded') at refund_completed.
     *
     * @return the stage just reached, or null if there's nothing further to advance.
     */
    public String advance(int orderId, int userId, BigDecimal refundAmount, String refundMethod) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int returnId;
                String currentStatus;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, status FROM returns WHERE order_id = ? AND user_id = ? " +
                                "ORDER BY id DESC LIMIT 1 FOR UPDATE")) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); return null; }
                        returnId = rs.getInt("id");
                        currentStatus = rs.getString("status");
                    }
                }

                int currentIndex = indexOf(currentStatus);
                if (currentIndex < 0 || currentIndex >= STAGES.length - 1) {
                    conn.rollback();
                    return null; // already at refund_completed, or in a rejected state
                }
                String nextStage = STAGES[currentIndex + 1];

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE returns SET status = ? WHERE id = ?")) {
                    ps.setString(1, nextStage);
                    ps.setInt(2, returnId);
                    ps.executeUpdate();
                }

                if ("refund_initiated".equals(nextStage)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO refunds (return_id, order_id, amount, method, status) " +
                                    "VALUES (?, ?, ?, ?, 'initiated')")) {
                        ps.setInt(1, returnId);
                        ps.setInt(2, orderId);
                        ps.setBigDecimal(3, refundAmount);
                        ps.setString(4, refundMethod);
                        ps.executeUpdate();
                    }
                } else if ("refund_completed".equals(nextStage)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE refunds SET status = 'completed', completed_at = ? WHERE return_id = ?")) {
                        ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                        ps.setInt(2, returnId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE orders SET payment_status = 'refunded' WHERE id = ?")) {
                        ps.setInt(1, orderId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return nextStage;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private int indexOf(String stage) {
        for (int i = 0; i < STAGES.length; i++) if (STAGES[i].equals(stage)) return i;
        return -1;
    }

    private ReturnRequest map(ResultSet rs) throws SQLException {
        ReturnRequest r = new ReturnRequest();
        r.setId(rs.getInt("id"));
        r.setOrderId(rs.getInt("order_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setReason(rs.getString("reason"));
        r.setComment(rs.getString("comment"));
        r.setStatus(rs.getString("status"));
        r.setRequestedAt(rs.getTimestamp("requested_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }

    private Refund mapRefund(ResultSet rs) throws SQLException {
        Refund f = new Refund();
        f.setId(rs.getInt("id"));
        f.setReturnId(rs.getInt("return_id"));
        f.setOrderId(rs.getInt("order_id"));
        f.setAmount(rs.getBigDecimal("amount"));
        f.setMethod(rs.getString("method"));
        f.setStatus(rs.getString("status"));
        f.setInitiatedAt(rs.getTimestamp("initiated_at"));
        f.setCompletedAt(rs.getTimestamp("completed_at"));
        return f;
    }
}
