<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your Wishlist — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<div class="container py-4">
    <h4 class="fw-bold mb-4">Your Wishlist</h4>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
    </c:if>

    <c:choose>
        <c:when test="${empty wishlistItems}">
            <div class="text-center py-5">
                <i class="fa-regular fa-heart fa-3x text-muted mb-3"></i>
                <h5>Your wishlist is empty</h5>
                <p class="text-muted">Save products you like so you can find them again easily.</p>
                <a href="<%= request.getContextPath() %>/products" class="btn btn-gradient">Continue Shopping</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-4 product-grid">
                <c:forEach var="p" items="${wishlistItems}">
                    <div class="col-6 col-md-3" data-product-id="${p.id}">
                        <div class="product-card position-relative">
                            <button class="btn btn-sm btn-light position-absolute top-0 end-0 m-2 remove-wish-btn"
                                    title="Remove from wishlist" style="z-index:2;border-radius:50%;">
                                <i class="fa-solid fa-xmark"></i>
                            </button>
                            <a href="<%= request.getContextPath() %>/product?id=${p.id}" class="text-decoration-none text-dark">
                                <img src="<%= request.getContextPath() %>/assets/img/products/${not empty p.thumbnail ? p.thumbnail : 'placeholder.jpg'}" alt="${p.name}">
                                <div class="info">
                                    <div class="brand">${p.brand}</div>
                                    <div class="name">${p.name}</div>
                                    <div class="price">
                                        <span class="final">&#8377;<fmt:formatNumber value="${p.finalPrice}" maxFractionDigits="0"/></span>
                                    </div>
                                </div>
                            </a>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
<script>
    const ctx = '<%= request.getContextPath() %>';
    document.querySelectorAll('.remove-wish-btn').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const card = btn.closest('[data-product-id]');
            const productId = card.getAttribute('data-product-id');
            fetch(ctx + '/wishlist/toggle', {
                method: 'POST',
                body: new URLSearchParams({ productId: productId })
            }).then(function (res) { return res.json(); })
              .then(function (data) {
                  if (data && data.saved === false) {
                      card.remove();
                      if (!document.querySelector('[data-product-id]')) {
                          location.reload();
                      }
                  }
              });
        });
    });
</script>
</body>
</html>
