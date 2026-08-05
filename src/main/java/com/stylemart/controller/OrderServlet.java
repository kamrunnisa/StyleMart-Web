package com.stylemart.controller;

import com.stylemart.dao.CartDAO;
import com.stylemart.dao.OrderDAO;
import com.stylemart.dao.ReturnDAO;
import com.stylemart.dao.TrackingDAO;
import com.stylemart.model.Order;
import com.stylemart.model.OrderItem;
import com.stylemart.model.ReturnRequest;
import com.stylemart.model.TrackingEvent;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Order history, tracking, cancel and return/refund. Sits under /account/*, so
 * AuthFilter already guarantees a logged-in user before any method here runs.
 *
 *   GET  /account/orders               -> order history list
 *   GET  /account/orders/view          -> one order's detail
 *   GET  /account/orders/track         -> delivery timeline (placed..delivered)
 *   GET  /account/orders/return-status -> return/refund timeline
 *   POST /account/orders/cancel        -> cancel (only while placed/accepted), with a reason
 *   POST /account/orders/return        -> request a return (only once delivered), with a reason
 *   POST /account/orders/track/advance -> demo: step the delivery timeline forward one stage
 *   POST /account/orders/return/advance-> demo: step the return/refund timeline forward one stage
 *   POST /account/orders/buy-again     -> re-adds every item from a past order to the cart
 */
@WebServlet({
        "/account/orders", "/account/orders/view", "/account/orders/track", "/account/orders/return-status",
        "/account/orders/cancel", "/account/orders/return",
        "/account/orders/track/advance", "/account/orders/return/advance", "/account/orders/buy-again"
})
public class OrderServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(OrderServlet.class.getName());
    private final OrderDAO orderDAO = new OrderDAO();
    private final TrackingDAO trackingDAO = new TrackingDAO();
    private final ReturnDAO returnDAO = new ReturnDAO();
    private final CartDAO cartDAO = new CartDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = currentUserId(request);
        String path = request.getServletPath();

        try {
            if ("/account/orders".equals(path)) {
                List<Order> orders = orderDAO.getByUser(userId);
                request.setAttribute("orders", orders);
                request.getRequestDispatcher("/WEB-INF/views/account/orders.jsp").forward(request, response);
                return;
            }

            if ("/account/orders/view".equals(path)) {
                Order order = loadOwnedOrder(request, userId);
                if (order == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("order", order);
                request.setAttribute("justPlaced", "1".equals(request.getParameter("placed")));
                request.getRequestDispatcher("/WEB-INF/views/account/order-detail.jsp").forward(request, response);
                return;
            }

            if ("/account/orders/track".equals(path)) {
                Order order = loadOwnedOrder(request, userId);
                if (order == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                List<TrackingEvent> events = trackingDAO.getByOrder(order.getId());
                request.setAttribute("order", order);
                request.setAttribute("events", events);
                request.setAttribute("stages", TrackingDAO.STAGES);
                request.getRequestDispatcher("/WEB-INF/views/account/track-order.jsp").forward(request, response);
                return;
            }

            if ("/account/orders/return-status".equals(path)) {
                Order order = loadOwnedOrder(request, userId);
                if (order == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                ReturnRequest ret = returnDAO.getByOrder(order.getId(), userId);
                if (ret == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("order", order);
                request.setAttribute("ret", ret);
                request.getRequestDispatcher("/WEB-INF/views/account/return-status.jsp").forward(request, response);
                return;
            }

            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load orders for userId=" + userId, e);
            request.setAttribute("errorMessage", "Unable to load your orders right now.");
            request.getRequestDispatcher("/WEB-INF/views/account/orders.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = currentUserId(request);
        String path = request.getServletPath();
        int orderId = parseInt(request.getParameter("id"), parseInt(request.getParameter("orderId"), -1));
        String ctx = request.getContextPath();

        try {
            switch (path) {
                case "/account/orders/cancel": {
                    String reason = trim(request.getParameter("reason"));
                    orderDAO.cancel(orderId, userId, reason);
                    response.sendRedirect(ctx + "/account/orders/view?id=" + orderId);
                    return;
                }
                case "/account/orders/return": {
                    String reason = trim(request.getParameter("reason"));
                    String comment = trim(request.getParameter("comment"));
                    if (reason == null || reason.isBlank()) reason = "Other";
                    returnDAO.create(orderId, userId, reason, comment);
                    response.sendRedirect(ctx + "/account/orders/return-status?id=" + orderId);
                    return;
                }
                case "/account/orders/track/advance": {
                    trackingDAO.advance(orderId, userId);
                    response.sendRedirect(ctx + "/account/orders/track?id=" + orderId);
                    return;
                }
                case "/account/orders/return/advance": {
                    Order order = orderDAO.getById(orderId, userId);
                    BigDecimal amount = order != null ? order.getTotalAmount() : BigDecimal.ZERO;
                    String method = order != null && "online".equals(order.getPaymentMethod())
                            ? "Original payment method" : "Bank transfer";
                    returnDAO.advance(orderId, userId, amount, method);
                    response.sendRedirect(ctx + "/account/orders/return-status?id=" + orderId);
                    return;
                }
                case "/account/orders/buy-again": {
                    Order order = orderDAO.getDetailForUser(orderId, userId);
                    if (order != null && order.getItems() != null) {
                        for (OrderItem item : order.getItems()) {
                            cartDAO.addOrIncrement(userId, item.getProductId(), item.getSize(), item.getColor(), item.getQuantity());
                        }
                    }
                    response.sendRedirect(ctx + "/cart");
                    return;
                }
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Order action failed for userId=" + userId + " orderId=" + orderId + " path=" + path, e);
            response.sendRedirect(ctx + "/account/orders/view?id=" + orderId);
        }
    }

    private Order loadOwnedOrder(HttpServletRequest request, int userId) throws SQLException {
        int orderId = parseInt(request.getParameter("id"), -1);
        return orderId > 0 ? orderDAO.getDetailForUser(orderId, userId) : null;
    }

    private int currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Integer id = session == null ? null : (Integer) session.getAttribute("userId");
        return id == null ? -1 : id;
    }

    private int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
