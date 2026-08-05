<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<div class="container py-4">
    <h4 class="fw-bold mb-4">Checkout</h4>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
    </c:if>

    <form action="<%= request.getContextPath() %>/checkout/place" method="post" id="checkoutForm">
        <div class="row g-4">
            <div class="col-lg-7">
                <div class="border rounded p-3 mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h6 class="fw-bold mb-0">Deliver to</h6>
                        <a href="<%= request.getContextPath() %>/account/addresses?redirect=checkout" class="small">
                            <i class="fa-solid fa-plus"></i> Add new address
                        </a>
                    </div>

                    <c:choose>
                        <c:when test="${empty addresses}">
                            <p class="text-muted mb-2">You don't have any saved addresses yet.</p>
                            <a href="<%= request.getContextPath() %>/account/addresses?redirect=checkout" class="btn btn-gradient btn-sm">Add an address</a>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="addr" items="${addresses}" varStatus="st">
                                <label class="address-choice d-flex gap-2 p-2 mb-2 border rounded ${addr['default'] ? 'border-primary' : ''}">
                                    <input type="radio" name="addressId" value="${addr.id}" class="mt-1" ${addr['default'] ? 'checked' : (st.first ? 'checked' : '')}>
                                    <div>
                                        <div class="fw-semibold">
                                            ${addr.fullName}
                                            <span class="badge bg-light text-dark border ms-1">${addr.label}</span>
                                            <c:if test="${addr['default']}"><span class="badge bg-primary ms-1">Default</span></c:if>
                                        </div>
                                        <div class="text-muted small">
                                            ${addr.addressLine1}<c:if test="${not empty addr.addressLine2}">, ${addr.addressLine2}</c:if>,
                                            ${addr.city}, ${addr.state} - ${addr.pincode}
                                        </div>
                                        <div class="text-muted small">Phone: ${addr.phone}</div>
                                    </div>
                                </label>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="border rounded p-3">
                    <h6 class="fw-bold mb-3">Payment method</h6>
                    <div class="form-check mb-2">
                        <input class="form-check-input" type="radio" name="paymentMethod" id="payCod" value="cod" checked>
                        <label class="form-check-label" for="payCod">Cash on Delivery</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="paymentMethod" id="payOnline" value="online">
                        <label class="form-check-label" for="payOnline">Pay Online (Card / UPI / Wallet)</label>
                    </div>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="border rounded p-3 mb-3">
                    <h6 class="fw-bold mb-3">Order Items</h6>
                    <c:forEach var="item" items="${cartItems}">
                        <div class="d-flex gap-2 align-items-center mb-2">
                            <img src="<%= request.getContextPath() %>/assets/img/products/${not empty item.thumbnail ? item.thumbnail : 'placeholder.jpg'}"
                                 alt="${item.name}" style="width:48px;height:60px;object-fit:cover;border-radius:6px;">
                            <div class="flex-grow-1 small">
                                <div class="fw-semibold">${item.name}</div>
                                <div class="text-muted">
                                    Qty: ${item.quantity}
                                    <c:if test="${not empty item.size}"> &middot; Size: ${item.size}</c:if>
                                </div>
                            </div>
                            <div class="fw-semibold small">&#8377;<fmt:formatNumber value="${item.lineTotal}" maxFractionDigits="0"/></div>
                        </div>
                    </c:forEach>
                </div>

                <div class="border rounded p-3">
                    <h6 class="fw-bold mb-3">Order Summary</h6>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Subtotal</span>
                        <span>&#8377;<fmt:formatNumber value="${summary.subtotal}" maxFractionDigits="0"/></span>
                    </div>
                    <c:if test="${summary.discount > 0}">
                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted">Coupon discount (${summary.couponCode})</span>
                            <span class="text-success">-&#8377;<fmt:formatNumber value="${summary.discount}" maxFractionDigits="0"/></span>
                        </div>
                    </c:if>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">GST (5%)</span>
                        <span>&#8377;<fmt:formatNumber value="${summary.tax}" maxFractionDigits="0"/></span>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Delivery</span>
                        <span>
                            <c:choose>
                                <c:when test="${summary.deliveryCharge > 0}">&#8377;<fmt:formatNumber value="${summary.deliveryCharge}" maxFractionDigits="0"/></c:when>
                                <c:otherwise>FREE</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <hr>
                    <div class="d-flex justify-content-between mb-3">
                        <span class="fw-bold">Total</span>
                        <span class="fw-bold fs-5">&#8377;<fmt:formatNumber value="${summary.total}" maxFractionDigits="0"/></span>
                    </div>
                    <button type="submit" class="btn btn-gradient w-100" ${empty addresses ? 'disabled' : ''}>
                        Place Order
                    </button>
                </div>
            </div>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
