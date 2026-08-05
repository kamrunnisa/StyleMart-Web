package com.stylemart.dao;

import com.stylemart.model.CartItem;
import com.stylemart.model.Order;
import com.stylemart.model.OrderItem;
import com.stylemart.model.OrderSummary;
import com.stylemart.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class OrderDAO {

    /**
     * Places an order from the given cart lines in one transaction:
     * inserts `orders` + `order_items` + a `payments` row, decrements product
     * stock, and clears the user's cart -- all or nothing.
     *
     * @return the new order's id
     * @throws IllegalStateException if any line's stock changed since the cart was last read
     */
    public int placeOrder(int userId, int addressId, List<CartItem> items, OrderSummary summary,
                           Integer couponId, String paymentMethod) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Re-check stock inside the transaction to avoid overselling on a race.
                try (PreparedStatement stockCheck = conn.prepareStatement(
                        "SELECT stock FROM products WHERE id = ? FOR UPDATE")) {
                    for (CartItem item : items) {
                        stockCheck.setInt(1, item.getProductId());
                        try (ResultSet rs = stockCheck.executeQuery()) {
                            if (!rs.next() || rs.getInt("stock") < item.getQuantity()) {
                                throw new IllegalStateException(
                                        "\"" + item.getName() + "\" no longer has enough stock");
                            }
                        }
                    }
                }

                String orderNumber = generateOrderNumber();
                int orderId;
                String orderSql = "INSERT INTO orders " +
                        "(user_id, address_id, order_number, subtotal, discount_amount, delivery_charge, " +
                        "tax_amount, total_amount, coupon_id, status, payment_status, payment_method) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'placed', ?, ?)";
                // Both COD and online orders start "pending" -- online no longer fakes an
                // instant success here. The new /payment flow (PaymentServlet) is what
                // actually flips payment_status to 'paid' once the demo gateway succeeds.
                try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, addressId);
                    ps.setString(3, orderNumber);
                    ps.setBigDecimal(4, summary.getSubtotal());
                    ps.setBigDecimal(5, summary.getDiscount());
                    ps.setBigDecimal(6, summary.getDeliveryCharge());
                    ps.setBigDecimal(7, summary.getTax());
                    ps.setBigDecimal(8, summary.getTotal());
                    if (couponId != null) ps.setInt(9, couponId); else ps.setNull(9, Types.INTEGER);
                    ps.setString(10, "pending");
                    ps.setString(11, paymentMethod);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        orderId = keys.next() ? keys.getInt(1) : -1;
                    }
                }

                String itemSql = "INSERT INTO order_items " +
                        "(order_id, product_id, product_name, size, color, quantity, unit_price, line_total) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                    for (CartItem item : items) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, item.getProductId());
                        ps.setString(3, item.getName());
                        ps.setString(4, item.getSize());
                        ps.setString(5, item.getColor());
                        ps.setInt(6, item.getQuantity());
                        ps.setBigDecimal(7, item.getUnitPrice());
                        ps.setBigDecimal(8, item.getLineTotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET stock = stock - ? WHERE id = ?")) {
                    for (CartItem item : items) {
                        ps.setInt(1, item.getQuantity());
                        ps.setInt(2, item.getProductId());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                String paymentSql = "INSERT INTO payments (order_id, method, amount, status, paid_at) " +
                        "VALUES (?, ?, ?, 'pending', NULL)";
                try (PreparedStatement ps = conn.prepareStatement(paymentSql)) {
                    ps.setInt(1, orderId);
                    ps.setString(2, paymentMethod);
                    ps.setBigDecimal(3, summary.getTotal());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO tracking_history (order_id, stage, note) VALUES (?, 'placed', ?)")) {
                    ps.setInt(1, orderId);
                    ps.setString(2, "Order placed successfully.");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    // Tolerate migration 003 (tracking_history table) not being applied yet --
                    // the order itself still gets created fine, tracking is just unavailable.
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                conn.commit();
                return orderId;
            } catch (SQLException | IllegalStateException e) {
                conn.rollback();
                if (e instanceof SQLException se) throw se;
                throw new SQLException(e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Order> getByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY placed_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) orders.add(mapOrder(rs));
            }
        }
        return orders;
    }

    /** Bare order row (no items/address hydration) -- enough for the payment page. */
    public Order getById(int orderId, int userId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapOrder(rs) : null;
            }
        }
    }

    /** Full detail view: order + its address + its line items (with a best-effort current thumbnail). */
    public Order getDetailForUser(int orderId, int userId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ? AND user_id = ?";
        Order order;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                order = mapOrder(rs);
            }
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM addresses WHERE id = ?")) {
            ps.setInt(1, order.getAddressId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    com.stylemart.model.Address a = new com.stylemart.model.Address();
                    a.setId(rs.getInt("id"));
                    a.setLabel(rs.getString("label"));
                    a.setFullName(rs.getString("full_name"));
                    a.setPhone(rs.getString("phone"));
                    a.setAddressLine1(rs.getString("address_line1"));
                    a.setAddressLine2(rs.getString("address_line2"));
                    a.setCity(rs.getString("city"));
                    a.setState(rs.getString("state"));
                    a.setPincode(rs.getString("pincode"));
                    order.setAddress(a);
                }
            }
        }

        String itemSql = "SELECT oi.*, p.thumbnail FROM order_items oi " +
                "LEFT JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(itemSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem oi = new OrderItem();
                    oi.setId(rs.getInt("id"));
                    oi.setOrderId(rs.getInt("order_id"));
                    oi.setProductId(rs.getInt("product_id"));
                    oi.setProductName(rs.getString("product_name"));
                    oi.setSize(rs.getString("size"));
                    oi.setColor(rs.getString("color"));
                    oi.setQuantity(rs.getInt("quantity"));
                    oi.setUnitPrice(rs.getBigDecimal("unit_price"));
                    oi.setLineTotal(rs.getBigDecimal("line_total"));
                    oi.setThumbnail(rs.getString("thumbnail"));
                    items.add(oi);
                }
            }
        }
        order.setItems(items);
        return order;
    }

    /** Cancels an order (only while it's still placed/accepted), restocks its items, and records why. */
    public boolean cancel(int orderId, int userId, String reason) throws SQLException {
        boolean cancelled = transitionStatus(orderId, userId, new String[]{"placed", "accepted"}, "cancelled", true);
        if (cancelled && reason != null && !reason.isBlank()) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE orders SET cancel_reason = ? WHERE id = ? AND user_id = ?")) {
                ps.setString(1, reason);
                ps.setInt(2, orderId);
                ps.setInt(3, userId);
                ps.executeUpdate();
            } catch (SQLException e) {
                // Tolerate migration 003 not having been applied yet -- the cancellation
                // itself already succeeded above, only the reason label is lost.
            }
        }
        return cancelled;
    }

    /** Requests a return (only once delivered). Does not restock -- a real flow would wait for the item back. */
    public boolean requestReturn(int orderId, int userId) throws SQLException {
        return transitionStatus(orderId, userId, new String[]{"delivered"}, "returned", false);
    }

    private boolean transitionStatus(int orderId, int userId, String[] allowedFrom, String toStatus,
                                      boolean restock) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String currentStatus;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT status FROM orders WHERE id = ? AND user_id = ? FOR UPDATE")) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        currentStatus = rs.getString("status");
                    }
                }
                boolean allowed = false;
                for (String s : allowedFrom) if (s.equals(currentStatus)) allowed = true;
                if (!allowed) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET status = ? WHERE id = ? AND user_id = ?")) {
                    ps.setString(1, toStatus);
                    ps.setInt(2, orderId);
                    ps.setInt(3, userId);
                    ps.executeUpdate();
                }

                if (restock) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE products p JOIN order_items oi ON oi.product_id = p.id " +
                                    "SET p.stock = p.stock + oi.quantity WHERE oi.order_id = ?")) {
                        ps.setInt(1, orderId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setAddressId(rs.getInt("address_id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setSubtotal(rs.getBigDecimal("subtotal"));
        o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        o.setDeliveryCharge(rs.getBigDecimal("delivery_charge"));
        o.setTaxAmount(rs.getBigDecimal("tax_amount"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        int couponId = rs.getInt("coupon_id");
        o.setCouponId(rs.wasNull() ? null : couponId);
        o.setStatus(rs.getString("status"));
        o.setCancelReason(getStringIfPresent(rs, "cancel_reason"));
        o.setPaymentStatus(rs.getString("payment_status"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setPlacedAt(rs.getTimestamp("placed_at"));
        o.setUpdatedAt(rs.getTimestamp("updated_at"));
        return o;
    }

    /** Tolerates a column not existing yet (e.g. migration 003 not yet applied). */
    private String getStringIfPresent(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private String generateOrderNumber() {
        long ts = System.currentTimeMillis() % 1_000_000_000L;
        int rand = ThreadLocalRandom.current().nextInt(100, 999);
        return "SM" + ts + rand;
    }
}
