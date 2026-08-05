<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your Cart — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<div class="container py-4">
    <h4 class="fw-bold mb-4">Your Cart</h4>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
    </c:if>

    <c:choose>
        <c:when test="${empty cartItems}">
            <div class="text-center py-5">
                <i class="fa-solid fa-bag-shopping fa-3x text-muted mb-3"></i>
                <h5>Your cart is empty</h5>
                <p class="text-muted">Looks like you haven't added anything yet.</p>
                <a href="<%= request.getContextPath() %>/products" class="btn btn-gradient">Continue Shopping</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <div class="col-lg-8">
                    <c:forEach var="item" items="${cartItems}">
                        <div class="cart-row d-flex gap-3 align-items-center border-bottom pb-3 mb-3" data-cart-id="${item.id}">
                            <img src="<%= request.getContextPath() %>/assets/img/products/${not empty item.thumbnail ? item.thumbnail : 'placeholder.jpg'}"
                                 alt="${item.name}" style="width:80px;height:100px;object-fit:cover;border-radius:8px;">
                            <div class="flex-grow-1">
                                <div class="text-muted small">${item.brand}</div>
                                <div class="fw-semibold">${item.name}</div>
                                <div class="text-muted small">
                                    <c:if test="${not empty item.size}">Size: ${item.size}</c:if>
                                    <c:if test="${not empty item.color}">&nbsp;&middot;&nbsp;Color: ${item.color}</c:if>
                                </div>
                                <div class="fw-bold mt-1 line-price">&#8377;<fmt:formatNumber value="${item.unitPrice}" maxFractionDigits="0"/></div>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <button class="btn btn-sm btn-outline-secondary qty-btn" data-delta="-1">-</button>
                                <span class="qty-value">${item.quantity}</span>
                                <button class="btn btn-sm btn-outline-secondary qty-btn" data-delta="1">+</button>
                            </div>
                            <button class="btn btn-sm btn-outline-danger remove-btn" title="Remove">
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </div>
                    </c:forEach>
                </div>
                <div class="col-lg-4">
                    <div class="border rounded p-3">
                        <h6 class="fw-bold mb-3">Order Summary</h6>

                        <div class="mb-3">
                            <div class="input-group input-group-sm">
                                <input type="text" id="couponInput" class="form-control" placeholder="Enter coupon code"
                                       value="${summary.couponCode}" ${not empty summary.couponCode ? 'readonly' : ''}>
                                <c:choose>
                                    <c:when test="${not empty summary.couponCode}">
                                        <button class="btn btn-outline-danger" id="couponRemoveBtn" type="button">Remove</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-outline-secondary" id="couponApplyBtn" type="button">Apply</button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div id="couponMsg" class="small mt-1
                                ${not empty summary.couponCode ? 'text-success' : (not empty summary.couponError ? 'text-danger' : 'text-muted')}">
                                <c:choose>
                                    <c:when test="${not empty summary.couponCode}">"${summary.couponCode}" applied</c:when>
                                    <c:when test="${not empty summary.couponError}">${summary.couponError}</c:when>
                                    <c:otherwise>Have a coupon? Enter it above.</c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted">Subtotal</span>
                            <span id="sumSubtotal" class="fw-semibold">&#8377;<fmt:formatNumber value="${summary.subtotal}" maxFractionDigits="0"/></span>
                        </div>
                        <div class="d-flex justify-content-between mb-2" id="discountRow" style="${summary.discount > 0 ? '' : 'display:none;'}">
                            <span class="text-muted">Coupon discount</span>
                            <span id="sumDiscount" class="fw-semibold text-success">-&#8377;<fmt:formatNumber value="${summary.discount}" maxFractionDigits="0"/></span>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted">GST (5%)</span>
                            <span id="sumTax" class="fw-semibold">&#8377;<fmt:formatNumber value="${summary.tax}" maxFractionDigits="0"/></span>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted">Delivery</span>
                            <span id="sumDelivery" class="fw-semibold">
                                <c:choose>
                                    <c:when test="${summary.deliveryCharge > 0}">&#8377;<fmt:formatNumber value="${summary.deliveryCharge}" maxFractionDigits="0"/></c:when>
                                    <c:otherwise>FREE</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <hr>
                        <div class="d-flex justify-content-between mb-3">
                            <span class="fw-bold">Total</span>
                            <span id="sumTotal" class="fw-bold fs-5">&#8377;<fmt:formatNumber value="${summary.total}" maxFractionDigits="0"/></span>
                        </div>
                        <p class="text-muted small mb-3">Free delivery on orders over &#8377;999.</p>
                        <a href="<%= request.getContextPath() %>/checkout" class="btn btn-gradient w-100">
                            <i class="fa-solid fa-bag-shopping me-2"></i>Buy Now
                        </a>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
<script>
    const ctx = '<%= request.getContextPath() %>';

    function applySummary(data) {
        document.getElementById('sumSubtotal').textContent = '\u20B9' + Math.round(data.subtotal);
        document.getElementById('sumTax').textContent = '\u20B9' + Math.round(data.tax);
        document.getElementById('sumDelivery').textContent = data.delivery > 0 ? ('\u20B9' + Math.round(data.delivery)) : 'FREE';
        document.getElementById('sumTotal').textContent = '\u20B9' + Math.round(data.total);
        const discountRow = document.getElementById('discountRow');
        if (data.discount > 0) {
            discountRow.style.display = '';
            document.getElementById('sumDiscount').textContent = '-\u20B9' + Math.round(data.discount);
        } else {
            discountRow.style.display = 'none';
        }
    }

    document.querySelectorAll('.qty-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const row = btn.closest('.cart-row');
            const cartId = row.getAttribute('data-cart-id');
            const qtyEl = row.querySelector('.qty-value');
            const newQty = parseInt(qtyEl.textContent, 10) + parseInt(btn.getAttribute('data-delta'), 10);
            if (newQty < 1) {
                row.querySelector('.remove-btn').click();
                return;
            }
            fetch(ctx + '/cart/update', {
                method: 'POST',
                body: new URLSearchParams({ cartId: cartId, quantity: newQty })
            }).then(function (res) { return res.json(); })
              .then(function (data) {
                  if (data.updated) {
                      qtyEl.textContent = newQty;
                      applySummary(data);
                  }
              });
        });
    });

    document.querySelectorAll('.remove-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const row = btn.closest('.cart-row');
            const cartId = row.getAttribute('data-cart-id');
            fetch(ctx + '/cart/remove', {
                method: 'POST',
                body: new URLSearchParams({ cartId: cartId })
            }).then(function (res) { return res.json(); })
              .then(function (data) {
                  if (data.removed) {
                      row.remove();
                      if (!document.querySelector('.cart-row')) {
                          location.reload();
                      } else {
                          applySummary(data);
                      }
                  }
              });
        });
    });

    const applyBtn = document.getElementById('couponApplyBtn');
    if (applyBtn) {
        applyBtn.addEventListener('click', function () {
            const code = document.getElementById('couponInput').value.trim();
            if (!code) return;
            fetch(ctx + '/cart/coupon/apply', {
                method: 'POST',
                body: new URLSearchParams({ code: code })
            }).then(function (res) { return res.json(); })
              .then(function (data) {
                  const msg = document.getElementById('couponMsg');
                  if (data.applied) {
                      location.reload();
                  } else {
                      msg.className = 'small mt-1 text-danger';
                      msg.textContent = data.error || 'Could not apply that coupon.';
                  }
              });
        });
    }

    const removeBtn = document.getElementById('couponRemoveBtn');
    if (removeBtn) {
        removeBtn.addEventListener('click', function () {
            fetch(ctx + '/cart/coupon/remove', { method: 'POST' })
                .then(function () { location.reload(); });
        });
    }
</script>
</body>
</html>
