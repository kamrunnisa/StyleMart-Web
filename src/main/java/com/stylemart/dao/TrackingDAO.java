package com.stylemart.dao;

import com.stylemart.model.TrackingEvent;
import com.stylemart.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrackingDAO {

    /** The ordered stages of the delivery timeline (index = display order). */
    public static final String[] STAGES = {
            "placed", "confirmed", "packed", "shipped", "out_for_delivery", "delivered"
    };

    /** Called once, right after an order is created. */
    public void seedPlaced(int orderId) throws SQLException {
        insert(orderId, "placed", "Order placed successfully.");
    }

    public List<TrackingEvent> getByOrder(int orderId) throws SQLException {
        String sql = "SELECT * FROM tracking_history WHERE order_id = ? ORDER BY created_at ASC, id ASC";
        List<TrackingEvent> events = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) events.add(map(rs));
            }
        }
        return events;
    }

    /**
     * Demo-only helper: advances an order to the next tracking stage (there's no admin
     * warehouse system behind this, so the customer's own order-detail page can step
     * it forward to see the timeline in action). Keeps `orders.status` roughly in sync
     * with the coarser state machine other code already relies on.
     *
     * @return the stage that was just recorded, or null if the order is already at the
     *         last stage (or not in a state that can progress, e.g. cancelled).
     */
    public String advance(int orderId, int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String currentStatus;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT status FROM orders WHERE id = ? AND user_id = ? FOR UPDATE")) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); return null; }
                        currentStatus = rs.getString("status");
                    }
                }
                if ("cancelled".equals(currentStatus) || "returned".equals(currentStatus)) {
                    conn.rollback();
                    return null;
                }

                int reachedCount;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM tracking_history WHERE order_id = ?")) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        reachedCount = rs.getInt(1);
                    }
                }
                if (reachedCount >= STAGES.length) {
                    conn.rollback();
                    return null;
                }
                String nextStage = STAGES[reachedCount];

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO tracking_history (order_id, stage, note) VALUES (?, ?, ?)")) {
                    ps.setInt(1, orderId);
                    ps.setString(2, nextStage);
                    ps.setString(3, noteFor(nextStage));
                    ps.executeUpdate();
                }

                String mappedStatus = mapStageToOrderStatus(nextStage);
                if (mappedStatus != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE orders SET status = ? WHERE id = ?")) {
                        ps.setString(1, mappedStatus);
                        ps.setInt(2, orderId);
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

    private void insert(int orderId, String stage, String note) throws SQLException {
        String sql = "INSERT INTO tracking_history (order_id, stage, note) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, stage);
            ps.setString(3, note);
            ps.executeUpdate();
        }
    }

    /** Only "confirmed"/"shipped"/"delivered" have a matching coarse order status; the rest are sub-stages. */
    private String mapStageToOrderStatus(String stage) {
        return switch (stage) {
            case "confirmed" -> "accepted";
            case "shipped" -> "shipped";
            case "delivered" -> "delivered";
            default -> null;
        };
    }

    private String noteFor(String stage) {
        return switch (stage) {
            case "confirmed" -> "Order confirmed by seller.";
            case "packed" -> "Your item has been packed.";
            case "shipped" -> "Order shipped from warehouse.";
            case "out_for_delivery" -> "Out for delivery.";
            case "delivered" -> "Delivered successfully.";
            default -> null;
        };
    }

    private TrackingEvent map(ResultSet rs) throws SQLException {
        TrackingEvent e = new TrackingEvent();
        e.setId(rs.getInt("id"));
        e.setOrderId(rs.getInt("order_id"));
        e.setStage(rs.getString("stage"));
        e.setNote(rs.getString("note"));
        e.setCreatedAt(rs.getTimestamp("created_at"));
        return e;
    }
}
