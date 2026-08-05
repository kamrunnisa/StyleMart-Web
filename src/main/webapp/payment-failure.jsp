<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Failed — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<div class="container py-5" style="max-width: 640px;">
    <div class="border rounded p-4 p-md-5 text-center">
        <div class="pay-fail-circle"><i class="fa-solid fa-xmark"></i></div>
        <h4 class="fw-bold mb-1">Payment failed</h4>
        <p class="text-muted mb-4">
            <c:choose>
                <c:when test="${not empty payment.failureReason}">${payment.failureReason}.</c:when>
                <c:otherwise>We couldn't process your payment.</c:otherwise>
            </c:choose>
            Your order #${order.orderNumber} is saved — you can retry or switch payment methods any time.
        </p>

        <div class="text-start border rounded p-3 mb-4">
            <div class="d-flex justify-content-between py-1">
                <span class="text-muted small">Order ID</span>
                <span class="fw-semibold small">#${order.orderNumber}</span>
            </div>
            <div class="d-flex justify-content-between py-1">
                <span class="text-muted small">Attempted method</span>
                <span class="fw-semibold small">${payment.provider}</span>
            </div>
            <div class="d-flex justify-content-between py-1">
                <span class="text-muted small">Amount due</span>
                <span class="fw-semibold small">&#8377;<fmt:formatNumber value="${order.totalAmount}" maxFractionDigits="2"/></span>
            </div>
        </div>

        <div class="d-grid gap-2 d-md-flex justify-content-md-center">
            <form action="<%= request.getContextPath() %>/payment/retry" method="post" class="d-inline">
                <input type="hidden" name="orderId" value="${order.id}">
                <button type="submit" class="btn btn-gradient px-4"><i class="fa-solid fa-rotate-right"></i> Retry Payment</button>
            </form>
            <a href="<%= request.getContextPath() %>/payment?orderId=${order.id}" class="btn btn-outline-secondary px-4">
                Change Payment Method
            </a>
            <a href="<%= request.getContextPath() %>/checkout" class="btn btn-outline-secondary px-4">
                Return to Checkout
            </a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
