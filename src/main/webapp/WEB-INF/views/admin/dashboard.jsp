<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard — StyleMart</title>
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

<div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h4 class="fw-bold mb-0">Product Catalog</h4>
        <a href="<%= request.getContextPath() %>/admin/products/new" class="btn btn-gradient">
            <i class="fa-solid fa-plus"></i> Add New Product
        </a>
    </div>

    <c:if test="${not empty param.success}">
        <div class="alert alert-success"><c:out value="${param.success}"/></div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="alert alert-danger"><c:out value="${param.error}"/></div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
    </c:if>

    <div class="table-responsive">
        <table class="table align-middle">
            <thead>
                <tr>
                    <th>Image</th>
                    <th>Product</th>
                    <th>Brand</th>
                    <th>Price</th>
                    <th>Stock</th>
                    <th>Status</th>
                    <th></th>
                    <th style="min-width:160px">Gallery Photos</th>
                    <th style="min-width:280px">Upload Image</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${products}">
                    <tr>
                        <td>
                            <img src="<%= request.getContextPath() %>/assets/img/products/${not empty p.thumbnail ? p.thumbnail : 'placeholder.jpg'}"
                                 alt="${p.name}" style="width:56px;height:70px;object-fit:cover;border-radius:6px;">
                        </td>
                        <td>${p.name}<div class="text-muted small">ID #${p.id}</div></td>
                        <td>${p.brand}</td>
                        <td>&#8377;<fmt:formatNumber value="${p.finalPrice}" maxFractionDigits="0"/></td>
                        <td>${p.stock}</td>
                        <td>
                            <span class="badge ${p.status == 'active' ? 'bg-success' : 'bg-secondary'}">${p.status}</span>
                        </td>
                        <td>
                            <a href="<%= request.getContextPath() %>/admin/products/edit?id=${p.id}"
                               class="btn btn-sm btn-outline-primary text-nowrap">
                                <i class="fa-solid fa-pen"></i> Edit
                            </a>
                        </td>
                        <td>
                            <div class="d-flex flex-wrap gap-1">
                                <c:forEach var="img" items="${galleryByProduct[p.id]}">
                                    <div class="position-relative">
                                        <img src="<%= request.getContextPath() %>/assets/img/products/${img.imageUrl}"
                                             style="width:36px;height:46px;object-fit:cover;border-radius:4px;">
                                        <form action="<%= request.getContextPath() %>/admin/products/delete-image"
                                              method="post" class="position-absolute top-0 end-0">
                                            <input type="hidden" name="imageId" value="${img.id}">
                                            <input type="hidden" name="productId" value="${p.id}">
                                            <button type="submit" class="btn btn-sm btn-danger p-0"
                                                    style="width:16px;height:16px;line-height:1;font-size:10px;border-radius:50%;"
                                                    title="Remove photo">&times;</button>
                                        </form>
                                    </div>
                                </c:forEach>
                                <c:if test="${empty galleryByProduct[p.id]}">
                                    <span class="text-muted small">None yet</span>
                                </c:if>
                            </div>
                        </td>
                        <td>
                            <form action="<%= request.getContextPath() %>/admin/products/upload-image"
                                  method="post" enctype="multipart/form-data" class="d-flex flex-column gap-1">
                                <input type="hidden" name="productId" value="${p.id}">
                                <input type="file" name="productImage" class="form-control form-control-sm"
                                       accept=".jpg,.jpeg,.png,.webp" required>
                                <div class="d-flex gap-1">
                                    <button type="submit" name="mode" value="thumbnail" class="btn btn-gradient btn-sm text-nowrap">Set as Main</button>
                                    <button type="submit" name="mode" value="gallery" class="btn btn-outline-secondary btn-sm text-nowrap">Add Extra Photo</button>
                                </div>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <p class="text-muted small">Accepted formats: JPG, JPEG, PNG, WEBP. Max size: 5 MB.</p>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
