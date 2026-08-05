package com.stylemart.controller;

import com.stylemart.dao.OrderDAO;
import com.stylemart.dao.PaymentDAO;
import com.stylemart.model.Order;
import com.stylemart.model.Payment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Demo payment gateway for orders placed with paymentMethod=online. No real
 * money/integration -- server-side validation is real, but the actual
 * success/failure outcome is chosen on the form (this is a demo gateway, not
 * a live one) so both the success and failure pages are easy to exercise.
 *
 *   GET  /payment?orderId=X    -> method selection + payment form
 *   POST /payment/process      -> validates + "processes" the chosen method
 *   GET  /payment/success      -> success page (also the idempotent landing
 *                                  spot if you refresh/revisit after paying)
 *   GET  /payment/failure      -> failure page (retry / change method / back)
 *   POST /payment/retry        -> resets a failed payment to pending, back to /payment
 */
@WebServlet({"/payment", "/payment/process", "/payment/success", "/payment/failure", "/payment/retry"})
public class PaymentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PaymentServlet.class.getName());
    private static final Pattern UPI_PATTERN = Pattern.compile("^[\\w.\\-]{2,}@[A-Za-z]{2,}$");

    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = currentUserId(request);
        String path = request.getServletPath();
        int orderId = parseInt(request.getParameter("orderId"), -1);

        try {
            Order order = orderId > 0 ? orderDAO.getById(orderId, userId) : null;
            if (order == null || !"online".equals(order.getPaymentMethod())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Payment payment = paymentDAO.getByOrderId(orderId);
            if (payment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if ("/payment/success".equals(path)) {
                if (!payment.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/payment?orderId=" + orderId);
                    return;
                }
                request.setAttribute("order", order);
                request.setAttribute("payment", payment);
                request.getRequestDispatcher("/payment-success.jsp").forward(request, response);
                return;
            }

            if ("/payment/failure".equals(path)) {
                if (!payment.isFailed()) {
                    response.sendRedirect(request.getContextPath() + "/payment?orderId=" + orderId);
                    return;
                }
                request.setAttribute("order", order);
                request.setAttribute("payment", payment);
                request.getRequestDispatcher("/payment-failure.jsp").forward(request, response);
                return;
            }

            if ("/payment".equals(path)) {
                // Duplicate-payment prevention: already paid? skip straight to success.
                if (payment.isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/payment/success?orderId=" + orderId);
                    return;
                }
                request.setAttribute("order", order);
                request.setAttribute("payment", payment);
                request.getRequestDispatcher("/payment.jsp").forward(request, response);
                return;
            }

            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Payment page load failed for orderId=" + orderId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = currentUserId(request);
        String path = request.getServletPath();
        int orderId = parseInt(request.getParameter("orderId"), -1);
        String ctx = request.getContextPath();

        try {
            Order order = orderId > 0 ? orderDAO.getById(orderId, userId) : null;
            if (order == null || !"online".equals(order.getPaymentMethod())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if ("/payment/retry".equals(path)) {
                paymentDAO.resetToPending(orderId);
                response.sendRedirect(ctx + "/payment?orderId=" + orderId);
                return;
            }

            if (!"/payment/process".equals(path)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Duplicate-payment prevention: if it's already paid, don't process again.
            Payment payment = paymentDAO.getByOrderId(orderId);
            if (payment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (payment.isSuccess()) {
                response.sendRedirect(ctx + "/payment/success?orderId=" + orderId);
                return;
            }

            String method = trim(request.getParameter("method"));
            String errors = validate(method, request);

            if (errors != null) {
                request.setAttribute("order", order);
                request.setAttribute("payment", payment);
                request.setAttribute("errorMessage", errors);
                request.setAttribute("selectedMethod", method);
                request.getRequestDispatcher("/payment.jsp").forward(request, response);
                return;
            }

            String provider = describeProvider(method, request);
            // Demo gateway: the actual success/failure outcome is chosen on the form
            // (this project has no real payment integration) so both the success and
            // failure paths are easy to demo. Everything else about the flow -- field
            // validation, DB updates, redirects -- behaves like the real thing.
            boolean simulateSuccess = !"failure".equals(request.getParameter("simulateOutcome"));

            if (simulateSuccess) {
                String transactionId = "TXN" + System.currentTimeMillis()
                        + ThreadLocalRandom.current().nextInt(100, 999);
                paymentDAO.markSuccess(orderId, provider, transactionId);
                HttpSession session = request.getSession(false);
                if (session != null) session.setAttribute("lastPaymentOk_" + orderId, true);
                response.sendRedirect(ctx + "/payment/success?orderId=" + orderId);
            } else {
                paymentDAO.markFailed(orderId, provider, "Declined by demo gateway");
                response.sendRedirect(ctx + "/payment/failure?orderId=" + orderId);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Payment processing failed for orderId=" + orderId, e);
            response.sendRedirect(ctx + "/payment/failure?orderId=" + orderId);
        }
    }

    /** Returns a user-facing error string if invalid, or null if the submission is clean. */
    private String validate(String method, HttpServletRequest request) {
        if (method == null) return "Please choose a payment method.";
        switch (method) {
            case "card": {
                String number = digitsOnly(trim(request.getParameter("cardNumber")));
                String expiry = trim(request.getParameter("cardExpiry"));   // MM/YY
                String cvv = trim(request.getParameter("cardCvv"));
                String name = trim(request.getParameter("cardName"));

                if (isBlank(name)) return "Enter the name on the card.";
                if (number.length() < 13 || number.length() > 19 || !luhnValid(number)) {
                    return "Enter a valid card number.";
                }
                if (expiry == null || !expiry.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
                    return "Enter the card expiry as MM/YY.";
                }
                if (isExpired(expiry)) return "This card has expired.";
                if (cvv == null || !cvv.matches("^\\d{3,4}$")) return "Enter a valid CVV.";
                return null;
            }
            case "upi": {
                String upiId = trim(request.getParameter("upiId"));
                if (upiId == null || !UPI_PATTERN.matcher(upiId).matches()) {
                    return "Enter a valid UPI ID, e.g. name@bank.";
                }
                return null;
            }
            case "netbanking": {
                String bank = trim(request.getParameter("bank"));
                if (isBlank(bank)) return "Choose your bank.";
                return null;
            }
            case "wallet": {
                String provider = trim(request.getParameter("walletProvider"));
                String phone = digitsOnly(trim(request.getParameter("walletPhone")));
                if (isBlank(provider)) return "Choose a wallet.";
                if (phone.length() != 10) return "Enter a valid 10-digit mobile number.";
                return null;
            }
            default:
                return "Please choose a payment method.";
        }
    }

    private String describeProvider(String method, HttpServletRequest request) {
        switch (method) {
            case "card": {
                String number = digitsOnly(trim(request.getParameter("cardNumber")));
                String brand = cardBrand(number);
                String kind = "credit".equals(request.getParameter("cardType")) ? "Credit" : "Debit";
                return brand + " " + kind + " Card";
            }
            case "upi": {
                String app = trim(request.getParameter("upiApp"));
                return "UPI" + (isBlank(app) ? "" : " - " + app);
            }
            case "netbanking":
                return "Net Banking - " + trim(request.getParameter("bank"));
            case "wallet":
                return trim(request.getParameter("walletProvider")) + " Wallet";
            default:
                return "Online";
        }
    }

    private String cardBrand(String digitsOnly) {
        if (digitsOnly.startsWith("4")) return "Visa";
        if (digitsOnly.matches("^5[1-5].*") || digitsOnly.matches("^2[2-7].*")) return "MasterCard";
        if (digitsOnly.matches("^6[05].*") || digitsOnly.matches("^8[12].*")) return "RuPay";
        return "Card";
    }

    private boolean isExpired(String monthYear) {
        String[] parts = monthYear.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]);
        java.time.YearMonth expiry = java.time.YearMonth.of(year, month);
        return expiry.isBefore(java.time.YearMonth.now());
    }

    private boolean luhnValid(String digits) {
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    private String digitsOnly(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
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
}
