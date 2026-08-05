package com.stylemart.controller;

import com.stylemart.dao.CartDAO;
import com.stylemart.dao.CouponDAO;
import com.stylemart.model.CartItem;
import com.stylemart.model.Coupon;
import com.stylemart.model.OrderSummary;
import com.stylemart.util.PricingUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Real cart, backed by the `cart` table.
 *   GET  /cart                -> the cart page (with coupon/GST/delivery summary)
 *   POST /cart/add            -> add a product (used by the product-details "Add to Cart" button)
 *   POST /cart/update         -> change a line's quantity
 *   POST /cart/remove         -> remove a line
 *   POST /cart/coupon/apply   -> validate + attach a coupon code to the session
 *   POST /cart/coupon/remove  -> detach the coupon
 * The AJAX endpoints reply with tiny JSON so the calling page can update itself
 * without a full reload. The applied coupon code lives in the session (not the
 * DB) until an order is actually placed -- it's just "what this session wants
 * to try", re-validated against the live cart total on every read.
 */
@WebServlet({"/cart", "/cart/add", "/cart/update", "/cart/remove", "/cart/coupon/apply", "/cart/coupon/remove"})
public class CartServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CartServlet.class.getName());
    private final CartDAO cartDAO = new CartDAO();
    private final CouponDAO couponDAO = new CouponDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!"/cart".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Integer userId = currentUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?redirect=cart");
            return;
        }

        try {
            List<CartItem> items = cartDAO.getByUser(userId);
            BigDecimal subtotal = items.stream()
                    .map(CartItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            OrderSummary summary = buildSummary(request, subtotal);

            request.setAttribute("cartItems", items);
            request.setAttribute("cartTotal", subtotal);
            request.setAttribute("summary", summary);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load cart for userId=" + userId, e);
            request.setAttribute("errorMessage", "Unable to load your cart right now.");
        }
        request.getRequestDispatcher("/cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer userId = currentUserId(request);
        if (userId == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "{\"loggedIn\":false}");
            return;
        }

        String path = request.getServletPath();
        try {
            switch (path) {
                case "/cart/add" -> handleAdd(request, response, userId);
                case "/cart/update" -> handleUpdate(request, response, userId);
                case "/cart/remove" -> handleRemove(request, response, userId);
                case "/cart/coupon/apply" -> handleApplyCoupon(request, response, userId);
                case "/cart/coupon/remove" -> handleRemoveCoupon(request, response);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cart operation failed for userId=" + userId + " path=" + path, e);
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "{\"error\":\"Database error\"}");
        }
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException, SQLException {
        int productId = parseInt(request.getParameter("productId"), -1);
        if (productId <= 0) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"Invalid product\"}");
            return;
        }
        String size = emptyToNull(request.getParameter("size"));
        String color = emptyToNull(request.getParameter("color"));
        int quantity = Math.max(1, parseInt(request.getParameter("quantity"), 1));

        cartDAO.addOrIncrement(userId, productId, size, color, quantity);
        int count = cartDAO.getItemCount(userId);
        writeJson(response, HttpServletResponse.SC_OK, "{\"added\":true,\"cartCount\":" + count + "}");
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException, SQLException {
        int cartId = parseInt(request.getParameter("cartId"), -1);
        int quantity = parseInt(request.getParameter("quantity"), -1);
        if (cartId <= 0) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"Invalid line\"}");
            return;
        }
        boolean ok = cartDAO.updateQuantity(cartId, userId, quantity);
        BigDecimal subtotal = currentSubtotal(userId);
        OrderSummary summary = buildSummary(request, subtotal);
        writeJson(response, ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND,
                summaryJson(ok, "updated", subtotal, summary));
    }

    private void handleRemove(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException, SQLException {
        int cartId = parseInt(request.getParameter("cartId"), -1);
        if (cartId <= 0) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"Invalid line\"}");
            return;
        }
        boolean ok = cartDAO.remove(cartId, userId);
        BigDecimal subtotal = currentSubtotal(userId);
        OrderSummary summary = buildSummary(request, subtotal);
        writeJson(response, ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND,
                summaryJson(ok, "removed", subtotal, summary));
    }

    private void handleApplyCoupon(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException, SQLException {
        String code = emptyToNull(request.getParameter("code"));
        if (code == null) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"Enter a coupon code\"}");
            return;
        }
        BigDecimal subtotal = currentSubtotal(userId);
        Coupon coupon = couponDAO.findByCode(code);
        String reason = PricingUtil.ineligibilityReason(coupon, subtotal);
        if (reason != null) {
            writeJson(response, HttpServletResponse.SC_OK,
                    "{\"applied\":false,\"error\":" + jsonString(reason) + "}");
            return;
        }
        request.getSession(true).setAttribute("cartCouponCode", coupon.getCode());
        OrderSummary summary = PricingUtil.computeSummary(subtotal, coupon);
        writeJson(response, HttpServletResponse.SC_OK, "{\"applied\":true," + summaryFields(summary) + "}");
    }

    private void handleRemoveCoupon(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) session.removeAttribute("cartCouponCode");
        writeJson(response, HttpServletResponse.SC_OK, "{\"removed\":true}");
    }

    /** Re-validates any coupon code sitting in the session against the current subtotal. */
    private OrderSummary buildSummary(HttpServletRequest request, BigDecimal subtotal) throws SQLException {
        HttpSession session = request.getSession(false);
        String code = session == null ? null : (String) session.getAttribute("cartCouponCode");
        Coupon coupon = code == null ? null : couponDAO.findByCode(code);
        OrderSummary summary = PricingUtil.computeSummary(subtotal, coupon);
        if (coupon != null && summary.getCouponError() != null) {
            // Coupon became invalid (e.g. cart dropped below min order) -- drop it from the session too.
            session.removeAttribute("cartCouponCode");
        }
        return summary;
    }

    private BigDecimal currentSubtotal(int userId) throws SQLException {
        return cartDAO.getByUser(userId).stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String summaryJson(boolean ok, String verb, BigDecimal subtotal, OrderSummary summary) {
        return "{\"" + verb + "\":" + ok + "," + summaryFields(summary) + "}";
    }

    private String summaryFields(OrderSummary s) {
        return "\"subtotal\":" + s.getSubtotal() +
                ",\"discount\":" + s.getDiscount() +
                ",\"tax\":" + s.getTax() +
                ",\"delivery\":" + s.getDeliveryCharge() +
                ",\"total\":" + s.getTotal() +
                ",\"couponCode\":" + (s.getCouponCode() == null ? "null" : jsonString(s.getCouponCode())) +
                ",\"couponError\":" + (s.getCouponError() == null ? "null" : jsonString(s.getCouponError()));
    }

    private Integer currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Integer) session.getAttribute("userId");
    }

    private void writeJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }

    private String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
