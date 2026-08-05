<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${product.name} — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>

<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<div class="container py-4">
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb small">
            <li class="breadcrumb-item"><a href="<%= request.getContextPath() %>/index.jsp">Home</a></li>
            <c:if test="${not empty category}">
                <li class="breadcrumb-item"><a href="<%= request.getContextPath() %>/products?category=${category.slug}">${category.name}</a></li>
            </c:if>
            <li class="breadcrumb-item active">${product.name}</li>
        </ol>
    </nav>

    <div class="row g-5">
        <!-- Gallery -->
        <div class="col-lg-5">
            <div class="pd-gallery d-flex gap-3">
                <div class="pd-thumbs d-flex flex-column gap-2">
                    <img class="pd-thumb active"
                         src="<%= request.getContextPath() %>/assets/img/products/${not empty product.thumbnail ? product.thumbnail : 'placeholder.jpg'}"
                         onclick="selectThumb(this)">
                    <c:forEach var="img" items="${images}">
                        <img class="pd-thumb"
                             src="<%= request.getContextPath() %>/assets/img/products/${img}"
                             onclick="selectThumb(this)">
                    </c:forEach>
                </div>
                <div class="pd-main-wrap flex-grow-1 position-relative">
                    <img id="pdMainImage" class="pd-main-image"
                         src="<%= request.getContextPath() %>/assets/img/products/${not empty product.thumbnail ? product.thumbnail : 'placeholder.jpg'}"
                         alt="${product.name}">
                    <button type="button" class="pd-nav-btn pd-nav-prev" onclick="pdNav(-1)" aria-label="Previous image">
                        <i class="fa-solid fa-chevron-left"></i>
                    </button>
                    <button type="button" class="pd-nav-btn pd-nav-next" onclick="pdNav(1)" aria-label="Next image">
                        <i class="fa-solid fa-chevron-right"></i>
                    </button>
                    <button type="button" class="pd-zoom-btn" onclick="pdOpenZoom()" aria-label="Zoom image">
                        <i class="fa-solid fa-magnifying-glass-plus"></i>
                    </button>
                </div>
            </div>
        </div>

        <!-- Details -->
        <div class="col-lg-7">
            <div class="text-muted small">${product.brand}</div>
            <h2 class="fw-bold">${product.name}</h2>

            <c:if test="${product.totalReviews > 0}">
                <div class="mb-2">
                    <span class="badge bg-success"><fmt:formatNumber value="${product.avgRating}" maxFractionDigits="1"/> <i class="fa-solid fa-star"></i></span>
                    <span class="text-muted small ms-1">${product.totalReviews} Ratings</span>
                </div>
            </c:if>

            <div class="pd-price mb-3">
                <span class="final fs-3 fw-bold">&#8377;<fmt:formatNumber value="${product.finalPrice}" maxFractionDigits="0"/></span>
                <c:if test="${product.discountPercent > 0}">
                    <span class="original text-muted text-decoration-line-through ms-2">&#8377;<fmt:formatNumber value="${product.price}" maxFractionDigits="0"/></span>
                    <span class="discount text-danger ms-2"><fmt:formatNumber value="${product.discountPercent}" maxFractionDigits="0"/>% OFF</span>
                </c:if>
                <div class="text-muted small mt-1">Inclusive of all taxes</div>
            </div>

            <c:if test="${not empty product.sizes}">
                <div class="mb-3">
                    <div class="fw-semibold small mb-2">Select Size</div>
                    <div class="size-options d-flex flex-wrap gap-2">
                        <c:forEach var="sz" items="${product.sizes.split(',')}" varStatus="szStatus">
                            <label class="size-chip">
                                <input type="radio" name="pdSize" value="${sz}" class="d-none" ${szStatus.first ? 'checked' : ''}>
                                <span>${sz}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty product.colors}">
                <div class="mb-3">
                    <div class="fw-semibold small mb-2">Select Color</div>
                    <div class="color-options d-flex flex-wrap gap-2">
                        <c:forEach var="cl" items="${product.colors.split(',')}" varStatus="clStatus">
                            <label class="color-chip">
                                <input type="radio" name="pdColor" value="${cl}" class="d-none" ${clStatus.first ? 'checked' : ''}>
                                <span>${cl}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>
            </c:if>

            <div class="mb-3">
                <c:choose>
                    <c:when test="${product.stock > 0}">
                        <span class="badge bg-light text-success border border-success">In Stock</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge bg-light text-danger border border-danger">Out of Stock</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="d-flex gap-3 mb-4">
                <button class="btn btn-gradient btn-lg flex-grow-1" id="addToCartBtn"
                        data-product-id="${product.id}" ${product.stock <= 0 ? 'disabled' : ''}>
                    <i class="fa-solid fa-bag-shopping me-2"></i>Add to Cart
                </button>
                <button class="btn btn-outline-secondary btn-lg" id="wishlistBtn" title="Add to Wishlist"
                        data-product-id="${product.id}" data-saved="${inWishlist}">
                    <i class="fa-${inWishlist ? 'solid' : 'regular'} fa-heart" id="wishlistIcon"
                       style="${inWishlist ? 'color:#e63946' : ''}"></i>
                </button>
            </div>

            <div class="pd-description">
                <h6 class="fw-bold">Product Details</h6>
                <p class="text-muted">${product.description}</p>
            </div>
        </div>
    </div>

    <!-- Zoom Lightbox -->
    <div id="pdZoomOverlay" class="pd-zoom-overlay" onclick="pdCloseZoom(event)">
        <button type="button" class="pd-zoom-close" onclick="pdCloseZoom(event)" aria-label="Close">
            <i class="fa-solid fa-xmark"></i>
        </button>
        <img id="pdZoomImage" src="" alt="${product.name}">
    </div>

    <!-- Related Products -->
    <c:if test="${not empty related}">
        <section class="py-5">
            <h4 class="section-title">You May Also Like</h4>
            <div class="row g-4 product-grid">
                <c:forEach var="p" items="${related}">
                    <div class="col-6 col-md-3">
                        <a href="<%= request.getContextPath() %>/product?id=${p.id}" class="text-decoration-none text-dark">
                            <div class="product-card">
                                <img src="<%= request.getContextPath() %>/assets/img/products/${not empty p.thumbnail ? p.thumbnail : 'placeholder.jpg'}" alt="${p.name}">
                                <div class="info">
                                    <div class="brand">${p.brand}</div>
                                    <div class="name">${p.name}</div>
                                    <div class="price">
                                        <span class="final">&#8377;<fmt:formatNumber value="${p.finalPrice}" maxFractionDigits="0"/></span>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                </c:forEach>
            </div>
        </section>
    </c:if>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
<script>
    const ctx = '<%= request.getContextPath() %>';

    function selectThumb(el) {
        document.getElementById('pdMainImage').src = el.src;
        document.querySelectorAll('.pd-thumb').forEach(function (t) { t.classList.remove('active'); });
        el.classList.add('active');
    }

    function pdNav(direction) {
        const thumbs = Array.from(document.querySelectorAll('.pd-thumb'));
        if (thumbs.length === 0) return;
        let index = thumbs.findIndex(function (t) { return t.classList.contains('active'); });
        if (index === -1) index = 0;
        index = (index + direction + thumbs.length) % thumbs.length;
        selectThumb(thumbs[index]);
        thumbs[index].scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    }

    function pdOpenZoom() {
        document.getElementById('pdZoomImage').src = document.getElementById('pdMainImage').src;
        document.getElementById('pdZoomOverlay').classList.add('active');
    }

    function pdCloseZoom(e) {
        if (e.target.id === 'pdZoomImage') return;
        document.getElementById('pdZoomOverlay').classList.remove('active');
    }

    function getSelected(name) {
        const el = document.querySelector('input[name="' + name + '"]:checked');
        return el ? el.value : '';
    }

    document.getElementById('addToCartBtn').addEventListener('click', function () {
        const btn = this;
        const productId = btn.getAttribute('data-product-id');
        const body = new URLSearchParams({
            productId: productId,
            size: getSelected('pdSize'),
            color: getSelected('pdColor'),
            quantity: 1
        });

        btn.disabled = true;
        fetch(ctx + '/cart/add', { method: 'POST', body: body })
            .then(function (res) {
                if (res.status === 401) {
                    window.location.href = ctx + '/login.jsp?redirect=product?id=' + productId;
                    return null;
                }
                return res.json();
            })
            .then(function (data) {
                btn.disabled = false;
                if (data && data.added) {
                    btn.innerHTML = '<i class="fa-solid fa-check me-2"></i>Added to Cart';
                    setTimeout(function () {
                        btn.innerHTML = '<i class="fa-solid fa-bag-shopping me-2"></i>Add to Cart';
                    }, 1500);
                }
            })
            .catch(function () {
                btn.disabled = false;
                alert('Could not add to cart. Please try again.');
            });
    });

    document.getElementById('wishlistBtn').addEventListener('click', function () {
        const btn = this;
        const icon = document.getElementById('wishlistIcon');
        const productId = btn.getAttribute('data-product-id');

        fetch(ctx + '/wishlist/toggle', {
            method: 'POST',
            body: new URLSearchParams({ productId: productId })
        })
            .then(function (res) {
                if (res.status === 401) {
                    window.location.href = ctx + '/login.jsp?redirect=product?id=' + productId;
                    return null;
                }
                return res.json();
            })
            .then(function (data) {
                if (!data) return;
                if (data.saved) {
                    icon.classList.remove('fa-regular');
                    icon.classList.add('fa-solid');
                    icon.style.color = '#e63946';
                } else {
                    icon.classList.remove('fa-solid');
                    icon.classList.add('fa-regular');
                    icon.style.color = '';
                }
            })
            .catch(function () {
                alert('Could not update your wishlist. Please try again.');
            });
    });
</script>
</body>
</html>
