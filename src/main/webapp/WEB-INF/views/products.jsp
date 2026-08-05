<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>
        <c:choose>
            <c:when test="${not empty activeCategory}">${activeCategory.name} — StyleMart</c:when>
            <c:when test="${not empty filter.keyword}">Search: ${filter.keyword} — StyleMart</c:when>
            <c:otherwise>Shop — StyleMart</c:otherwise>
        </c:choose>
    </title>
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
            <c:if test="${not empty activeCategory}">
                <li class="breadcrumb-item active">${activeCategory.name}</li>
            </c:if>
            <c:if test="${empty activeCategory and not empty filter.keyword}">
                <li class="breadcrumb-item active">Search results for "${filter.keyword}"</li>
            </c:if>
            <c:if test="${empty activeCategory and empty filter.keyword}">
                <li class="breadcrumb-item active">All Products</li>
            </c:if>
        </ol>
    </nav>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
    </c:if>

    <c:if test="${empty errorMessage}">
    <div class="row g-4">

        <!-- Filter Sidebar -->
        <div class="col-lg-3">
            <div class="filter-panel glass-card p-3">
                <h6 class="fw-bold mb-3">Filters</h6>

                <form method="get" action="<%= request.getContextPath() %>/products" id="filterForm">
                    <c:if test="${not empty param.category}"><input type="hidden" name="category" value="${param.category}"></c:if>
                    <c:if test="${not empty param.search}"><input type="hidden" name="search" value="${param.search}"></c:if>
                    <c:if test="${not empty param.flag}"><input type="hidden" name="flag" value="${param.flag}"></c:if>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Category</label>
                        <select name="category" class="form-select form-select-sm" onchange="document.getElementById('filterForm').submit()">
                            <option value="">All Categories</option>
                            <c:forEach var="cat" items="${categories}">
                                <c:if test="${empty cat.parentId}">
                                    <option value="${cat.slug}" ${activeCategory.slug == cat.slug ? 'selected' : ''}>${cat.name}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Brand</label>
                        <select name="brand" class="form-select form-select-sm" onchange="document.getElementById('filterForm').submit()">
                            <option value="">All Brands</option>
                            <c:forEach var="b" items="${brands}">
                                <option value="${b}" ${filter.brand == b ? 'selected' : ''}>${b}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Size</label>
                        <select name="size" class="form-select form-select-sm" onchange="document.getElementById('filterForm').submit()">
                            <option value="">Any Size</option>
                            <option value="XS" ${filter.size == 'XS' ? 'selected' : ''}>XS</option>
                            <option value="S" ${filter.size == 'S' ? 'selected' : ''}>S</option>
                            <option value="M" ${filter.size == 'M' ? 'selected' : ''}>M</option>
                            <option value="L" ${filter.size == 'L' ? 'selected' : ''}>L</option>
                            <option value="XL" ${filter.size == 'XL' ? 'selected' : ''}>XL</option>
                            <option value="6" ${filter.size == '6' ? 'selected' : ''}>6</option>
                            <option value="7" ${filter.size == '7' ? 'selected' : ''}>7</option>
                            <option value="8" ${filter.size == '8' ? 'selected' : ''}>8</option>
                            <option value="9" ${filter.size == '9' ? 'selected' : ''}>9</option>
                            <option value="10" ${filter.size == '10' ? 'selected' : ''}>10</option>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Price Range (₹)</label>
                        <div class="d-flex gap-2">
                            <input type="number" name="minPrice" class="form-control form-control-sm" placeholder="Min" value="${filter.minPrice}">
                            <input type="number" name="maxPrice" class="form-control form-control-sm" placeholder="Max" value="${filter.maxPrice}">
                        </div>
                    </div>

                    <button type="submit" class="btn btn-gradient btn-sm w-100">Apply Filters</button>
                    <a href="<%= request.getContextPath() %>/products" class="btn btn-outline-secondary btn-sm w-100 mt-2">Clear All</a>
                </form>
            </div>
        </div>

        <!-- Product Grid -->
        <div class="col-lg-9">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <span class="text-muted small">${results.totalCount} products found</span>

                <form method="get" action="<%= request.getContextPath() %>/products" class="d-flex align-items-center gap-2">
                    <c:if test="${not empty param.category}"><input type="hidden" name="category" value="${param.category}"></c:if>
                    <c:if test="${not empty param.search}"><input type="hidden" name="search" value="${param.search}"></c:if>
                    <c:if test="${not empty param.flag}"><input type="hidden" name="flag" value="${param.flag}"></c:if>
                    <c:if test="${not empty param.brand}"><input type="hidden" name="brand" value="${param.brand}"></c:if>
                    <c:if test="${not empty param.size}"><input type="hidden" name="size" value="${param.size}"></c:if>
                    <label class="small text-muted mb-0">Sort:</label>
                    <select name="sort" class="form-select form-select-sm" style="width:auto" onchange="this.form.submit()">
                        <option value="popularity" ${filter.sort == 'popularity' || empty filter.sort ? 'selected' : ''}>Popularity</option>
                        <option value="newest" ${filter.sort == 'newest' ? 'selected' : ''}>Newest First</option>
                        <option value="price_low" ${filter.sort == 'price_low' ? 'selected' : ''}>Price: Low to High</option>
                        <option value="price_high" ${filter.sort == 'price_high' ? 'selected' : ''}>Price: High to Low</option>
                        <option value="rating" ${filter.sort == 'rating' ? 'selected' : ''}>Customer Rating</option>
                    </select>
                </form>
            </div>

            <c:choose>
                <c:when test="${empty results.products}">
                    <div class="text-center py-5">
                        <i class="fa-solid fa-box-open fa-3x text-muted mb-3"></i>
                        <p class="text-muted">No products matched your filters. Try widening your search.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="row g-4 product-grid">
                        <c:forEach var="p" items="${results.products}">
                            <div class="col-6 col-md-4">
                                <a href="<%= request.getContextPath() %>/product?id=${p.id}" class="text-decoration-none text-dark">
                                    <div class="product-card">
                                        <img src="<%= request.getContextPath() %>/assets/img/products/${not empty p.thumbnail ? p.thumbnail : 'placeholder.jpg'}" alt="${p.name}">
                                        <div class="info">
                                            <div class="brand">${p.brand}</div>
                                            <div class="name">${p.name}</div>
                                            <div class="price">
                                                <span class="final">&#8377;<fmt:formatNumber value="${p.finalPrice}" maxFractionDigits="0"/></span>
                                                <c:if test="${p.discountPercent > 0}">
                                                    <span class="original">&#8377;<fmt:formatNumber value="${p.price}" maxFractionDigits="0"/></span>
                                                    <span class="discount"><fmt:formatNumber value="${p.discountPercent}" maxFractionDigits="0"/>% off</span>
                                                </c:if>
                                            </div>
                                            <c:if test="${p.totalReviews > 0}">
                                                <div class="rating small text-muted">
                                                    <i class="fa-solid fa-star text-warning"></i> <fmt:formatNumber value="${p.avgRating}" maxFractionDigits="1"/> (${p.totalReviews})
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>
                                </a>
                            </div>
                        </c:forEach>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${results.totalPages > 1}">
                        <nav class="mt-4">
                            <ul class="pagination justify-content-center">
                                <li class="page-item ${results.hasPrev ? '' : 'disabled'}">
                                    <c:url var="prevUrl" value="/products">
                                        <c:param name="category" value="${param.category}"/>
                                        <c:param name="search" value="${param.search}"/>
                                        <c:param name="flag" value="${param.flag}"/>
                                        <c:param name="brand" value="${param.brand}"/>
                                        <c:param name="size" value="${param.size}"/>
                                        <c:param name="sort" value="${param.sort}"/>
                                        <c:param name="minPrice" value="${param.minPrice}"/>
                                        <c:param name="maxPrice" value="${param.maxPrice}"/>
                                        <c:param name="page" value="${results.page - 1}"/>
                                    </c:url>
                                    <a class="page-link" href="${prevUrl}">Previous</a>
                                </li>
                                <c:forEach begin="1" end="${results.totalPages}" var="pg">
                                    <li class="page-item ${pg == results.page ? 'active' : ''}">
                                        <c:url var="pageUrl" value="/products">
                                            <c:param name="category" value="${param.category}"/>
                                            <c:param name="search" value="${param.search}"/>
                                            <c:param name="flag" value="${param.flag}"/>
                                            <c:param name="brand" value="${param.brand}"/>
                                            <c:param name="size" value="${param.size}"/>
                                            <c:param name="sort" value="${param.sort}"/>
                                            <c:param name="minPrice" value="${param.minPrice}"/>
                                            <c:param name="maxPrice" value="${param.maxPrice}"/>
                                            <c:param name="page" value="${pg}"/>
                                        </c:url>
                                        <a class="page-link" href="${pageUrl}">${pg}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${results.hasNext ? '' : 'disabled'}">
                                    <c:url var="nextUrl" value="/products">
                                        <c:param name="category" value="${param.category}"/>
                                        <c:param name="search" value="${param.search}"/>
                                        <c:param name="flag" value="${param.flag}"/>
                                        <c:param name="brand" value="${param.brand}"/>
                                        <c:param name="size" value="${param.size}"/>
                                        <c:param name="sort" value="${param.sort}"/>
                                        <c:param name="minPrice" value="${param.minPrice}"/>
                                        <c:param name="maxPrice" value="${param.maxPrice}"/>
                                        <c:param name="page" value="${results.page + 1}"/>
                                    </c:url>
                                    <a class="page-link" href="${nextUrl}">Next</a>
                                </li>
                            </ul>
                        </nav>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    </c:if>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
