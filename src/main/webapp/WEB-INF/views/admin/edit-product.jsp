<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Product — StyleMart Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark" style="background:var(--sm-dark, #1a1a2e);">
    <div class="container">
        <span class="navbar-brand fw-bold">StyleMart Admin</span>
        <div>
            <span class="text-light small me-3">Signed in as ${sessionScope.adminName}</span>
            <a href="<%= request.getContextPath() %>/admin/logout" class="btn btn-outline-light btn-sm">Log Out</a>
        </div>
    </div>
</nav>

<div class="container py-4" style="max-width: 720px;">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h4 class="fw-bold mb-0">Edit Product <span class="text-muted">#${product.id}</span></h4>
        <a href="<%= request.getContextPath() %>/admin/dashboard" class="small"><i class="fa-solid fa-arrow-left"></i> Back to catalog</a>
    </div>

    <c:if test="${not empty param.error}">
        <div class="alert alert-danger"><c:out value="${param.error}"/></div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
    </c:if>

    <c:set var="checkedSizes" value=",${product.sizes},"/>

    <form action="<%= request.getContextPath() %>/admin/products/update" method="post" class="border rounded p-4">
        <input type="hidden" name="id" value="${product.id}">

        <div class="mb-3">
            <label class="form-label small fw-semibold">Product Name *</label>
            <input type="text" name="name" class="form-control" required maxlength="200" value="${product.name}">
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-6">
                <label class="form-label small fw-semibold">Brand *</label>
                <input type="text" name="brand" class="form-control" required maxlength="100" value="${product.brand}">
            </div>
            <div class="col-md-6">
                <label class="form-label small fw-semibold">Category *</label>
                <select name="categoryId" class="form-select" required>
                    <option value="">Choose a category</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.id}" ${cat.id == product.categoryId ? 'selected' : ''}>${cat.name}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="mb-3">
            <label class="form-label small fw-semibold">Description</label>
            <textarea name="description" class="form-control" rows="3" maxlength="2000">${product.description}</textarea>
        </div>

        <div class="row g-3 mb-3">
            <div class="col-md-3">
                <label class="form-label small fw-semibold">Price (&#8377;) *</label>
                <input type="number" name="price" class="form-control" min="0" step="0.01" required value="${product.price}">
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-semibold">Discount %</label>
                <input type="number" name="discountPercent" class="form-control" min="0" max="100" step="0.01" value="${product.discountPercent}">
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-semibold">Stock *</label>
                <input type="number" name="stock" class="form-control" min="0" step="1" required value="${product.stock}">
            </div>
            <div class="col-md-3">
                <label class="form-label small fw-semibold">Status</label>
                <select name="status" class="form-select">
                    <option value="active" ${product.status == 'active' ? 'selected' : ''}>Active</option>
                    <option value="inactive" ${product.status == 'inactive' ? 'selected' : ''}>Inactive</option>
                </select>
            </div>
        </div>

        <div class="mb-3">
            <label class="form-label small fw-semibold">Available Sizes</label>
            <div class="d-flex flex-wrap gap-3">
                <c:forEach var="s" items="${['XS','S','M','L','XL','XXL']}">
                    <c:set var="needle" value=",${s},"/>
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="sizes" value="${s}" id="size-${s}"
                               ${fn:contains(checkedSizes, needle) ? 'checked' : ''}>
                        <label class="form-check-label small" for="size-${s}">${s}</label>
                    </div>
                </c:forEach>
            </div>
        </div>

        <div class="mb-3">
            <label class="form-label small fw-semibold">Colors</label>
            <input type="text" name="colors" class="form-control" placeholder="e.g. Red, Black, Navy Blue" value="${product.colors}">
            <div class="form-text">Comma-separated.</div>
        </div>

        <div class="mb-3">
            <div class="form-text">
                To change the main photo or gallery photos, use the catalog page's
                <strong>Upload Image</strong> column instead.
            </div>
        </div>

        <div class="mb-4">
            <label class="form-label small fw-semibold d-block">Show this product in</label>
            <div class="d-flex flex-wrap gap-3">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="isTrending" id="isTrending" ${product.trending ? 'checked' : ''}>
                    <label class="form-check-label small" for="isTrending">Trending</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="isNewArrival" id="isNewArrival" ${product.newArrival ? 'checked' : ''}>
                    <label class="form-check-label small" for="isNewArrival">New Arrivals</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="isBestSeller" id="isBestSeller" ${product.bestSeller ? 'checked' : ''}>
                    <label class="form-check-label small" for="isBestSeller">Best Sellers</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="isFeatured" id="isFeatured" ${product.featured ? 'checked' : ''}>
                    <label class="form-check-label small" for="isFeatured">Featured</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="isFlashSale" id="isFlashSale" ${product.flashSale ? 'checked' : ''}>
                    <label class="form-check-label small" for="isFlashSale">Flash Sale</label>
                </div>
            </div>
        </div>

        <button type="submit" class="btn btn-gradient px-4">Save Changes</button>
        <a href="<%= request.getContextPath() %>/admin/dashboard" class="btn btn-outline-secondary px-4">Cancel</a>
    </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
