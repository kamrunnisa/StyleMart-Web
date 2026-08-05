package com.stylemart.controller;

import com.stylemart.dao.AddressDAO;
import com.stylemart.dao.CartDAO;
import com.stylemart.dao.CouponDAO;
import com.stylemart.dao.OrderDAO;
import com.stylemart.model.Address;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Not under /account/* (checkout is reachable straight from the cart), so
 * -- like CartServlet -- it does its own login check rather than relying on
 * AuthFilter.
 *   GET  /checkout        -> address picker + final order summary
 *   POST /checkout/place  -> validates everything server-side and creates the order
 */
@WebServlet({"/checkout", "/checkout/place"})
public class CheckoutServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CheckoutServlet.class.getName());
    private final CartDAO cartDAO = new CartDAO();
    private final AddressDAO addressDAO = new AddressDAO();
    private final CouponDAO couponDAO = new CouponDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer userId = currentUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?redirect=checkout");
            return;
        }

        // Safe defaults up front so a mid-load SQLException can never leave
        // checkout.jsp rendering against null attributes (that was crashing
        // fmt:formatNumber and turning a recoverable DB hiccup into a raw 500).
        request.setAttribute("cartItems", java.util.Collections.emptyList());
        request.setAttribute("addresses", java.util.Collections.emptyList());
        request.setAttribute("summary", new OrderSummary());

        try {
            List<CartItem> items = cartDAO.getByUser(userId);
            if (items.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
            List<Address> addresses = addressDAO.getByUser(userId);

            BigDecimal subtotal = items.stream()
                    .map(CartItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            OrderSummary summary = buildSummary(request, subtotal);

            request.setAttribute("cartItems", items);
            request.setAttribute("addresses", addresses);
            request.setAttribute("summary", summary);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load checkout for userId=" + userId, e);
            request.setAttribute("errorMessage", "Unable to load checkout right now. Please try again in a moment.");
        }
        request.getRequestDispatcher("/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer userId = currentUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?redirect=checkout");
            return;
        }
        if (!"/checkout/place".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int addressId = parseInt(request.getParameter("addressId"), -1);
        String paymentMethod = "online".equals(request.getParameter("paymentMethod")) ? "online" : "cod";

        try {
            List<CartItem> items = cartDAO.getByUser(userId);
            if (items.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }

            Address address = addressId > 0 ? addressDAO.getByIdForUser(addressId, userId) : null;
            if (address == null) {
                request.setAttribute("errorMessage", "Please choose a valid delivery address.");
                doGet(request, response);
                return;
            }

            BigDecimal subtotal = items.stream()
                    .map(CartItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            OrderSummary summary = buildSummary(request, subtotal);
            Integer couponId = null;
            if (summary.getCouponCode() != null) {
                Coupon coupon = couponDAO.findByCode(summary.getCouponCode());
                if (coupon != null) couponId = coupon.getId();
            }

            int orderId = orderDAO.placeOrder(userId, address.getId(), items, summary, couponId, paymentMethod);
            HttpSession session = request.getSession(false);
            if (session != null) session.removeAttribute("cartCouponCode");

            if ("online".equals(paymentMethod)) {
                // Order is created (stock reserved, like COD) but payment_status is still
                // 'pending' -- send the customer to the gateway to actually pay.
                response.sendRedirect(request.getContextPath() + "/payment?orderId=" + orderId);
            } else {
                response.sendRedirect(request.getContextPath() + "/account/orders/view?id=" + orderId + "&placed=1");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Order placement failed for userId=" + userId, e);
            boolean stockIssue = e.getCause() instanceof IllegalStateException;
            request.setAttribute("errorMessage", stockIssue
                    ? e.getMessage()
                    : "We couldn't place your order right now. Please try again.");
            doGet(request, response);
        }
    }

    private OrderSummary buildSummary(HttpServletRequest request, BigDecimal subtotal) throws SQLException {
        HttpSession session = request.getSession(false);
        String code = session == null ? null : (String) session.getAttribute("cartCouponCode");
        Coupon coupon = code == null ? null : couponDAO.findByCode(code);
        return PricingUtil.computeSummary(subtotal, coupon);
    }

    private Integer currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Integer) session.getAttribute("userId");
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
