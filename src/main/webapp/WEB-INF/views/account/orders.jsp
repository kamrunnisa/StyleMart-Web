<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Orders — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<div class="container py-4">
    <h4 class="fw-bold mb-4">My Orders</h4>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>

    <c:choose>
        <c:when test="${empty orders}">
            <div class="text-center py-5">
                <i class="fa-solid fa-box-open fa-3x text-muted mb-3"></i>
                <h5>No orders yet</h5>
                <p class="text-muted">When you place an order, it'll show up here.</p>
                <a href="<%= request.getContextPath() %>/products" class="btn btn-gradient">Start Shopping</a>
            </div>
        </c:when>
        <c:otherwise>
            <c:forEach var="o" items="${orders}">
                <div class="border rounded p-3 mb-3 order-row">
                    <a href="<%= request.getContextPath() %>/account/orders/view?id=${o.id}" class="text-decoration-none text-dark">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <div class="fw-semibold">Order #${o.orderNumber}</div>
                                <div class="text-muted small">
                                    Placed <fmt:formatDate value="${o.placedAt}" pattern="dd MMM yyyy, hh:mm a"/>
                                </div>
                            </div>
                            <div class="text-end">
                                <span class="status-badge status-${o.status}">${o.status}</span>
                                <div class="fw-bold mt-1">&#8377;<fmt:formatNumber value="${o.totalAmount}" maxFractionDigits="0"/></div>
                            </div>
                        </div>
                    </a>
                    <div class="d-flex gap-2 mt-2 pt-2 border-top">
                        <c:if test="${o.status ne 'cancelled' and o.status ne 'returned'}">
                            <a href="<%= request.getContextPath() %>/account/orders/track?id=${o.id}" class="btn btn-sm btn-outline-secondary">
                                <i class="fa-solid fa-truck"></i> Track Order
                            </a>
                        </c:if>
                        <c:if test="${o.status eq 'returned'}">
                            <a href="<%= request.getContextPath() %>/account/orders/return-status?id=${o.id}" class="btn btn-sm btn-outline-secondary">
                                <i class="fa-solid fa-rotate-left"></i> Return Status
                            </a>
                        </c:if>
                        <form action="<%= request.getContextPath() %>/account/orders/buy-again" method="post" class="d-inline">
                            <input type="hidden" name="id" value="${o.id}">
                            <button type="submit" class="btn btn-sm btn-outline-primary"><i class="fa-solid fa-rotate"></i> Buy Again</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
