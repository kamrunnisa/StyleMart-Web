<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Addresses — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h4 class="fw-bold mb-0">My Addresses</h4>
        <c:if test="${redirect eq 'checkout'}">
            <a href="<%= request.getContextPath() %>/checkout" class="small"><i class="fa-solid fa-arrow-left"></i> Back to checkout</a>
        </c:if>
    </div>

    <c:if test="${not empty errorMessage}"><div class="alert alert-danger">${errorMessage}</div></c:if>
    <c:if test="${not empty sessionScope.flashError}">
        <div class="alert alert-danger">${sessionScope.flashError}</div>
        <c:remove var="flashError" scope="session"/>
    </c:if>

    <div class="row g-4">
        <div class="col-lg-7">
            <c:choose>
                <c:when test="${empty addresses}">
                    <p class="text-muted">No saved addresses yet -- add your first one.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach var="addr" items="${addresses}">
                        <div class="border rounded p-3 mb-3 d-flex justify-content-between align-items-start">
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
                            <div class="d-flex flex-column gap-1 text-end">
                                <a href="<%= request.getContextPath() %>/account/addresses?edit=${addr.id}${not empty redirect ? '&redirect=' : ''}${redirect}"
                                   class="small">Edit</a>
                                <c:if test="${!addr['default']}">
                                    <form action="<%= request.getContextPath() %>/account/addresses/default" method="post">
                                        <input type="hidden" name="id" value="${addr.id}">
                                        <input type="hidden" name="redirect" value="${redirect}">
                                        <button type="submit" class="btn btn-link btn-sm p-0 small">Set default</button>
                                    </form>
                                </c:if>
                                <form action="<%= request.getContextPath() %>/account/addresses/delete" method="post"
                                      onsubmit="return confirm('Delete this address?');">
                                    <input type="hidden" name="id" value="${addr.id}">
                                    <input type="hidden" name="redirect" value="${redirect}">
                                    <button type="submit" class="btn btn-link btn-sm p-0 small text-danger">Delete</button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="col-lg-5">
            <div class="border rounded p-3">
                <h6 class="fw-bold mb-3">${not empty editingAddress ? 'Edit Address' : 'Add New Address'}</h6>
                <form action="<%= request.getContextPath() %>/account/addresses/save" method="post">
                    <c:if test="${not empty editingAddress}">
                        <input type="hidden" name="id" value="${editingAddress.id}">
                    </c:if>
                    <input type="hidden" name="redirect" value="${redirect}">

                    <div class="mb-2">
                        <label class="form-label small">Label</label>
                        <select name="label" class="form-select form-select-sm">
                            <c:forEach var="opt" items="${['Home','Work','Other']}">
                                <option value="${opt}" ${editingAddress.label eq opt ? 'selected' : ''}>${opt}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="mb-2">
                        <label class="form-label small">Full Name</label>
                        <input type="text" name="fullName" class="form-control form-control-sm" required
                               value="${editingAddress.fullName}">
                    </div>
                    <div class="mb-2">
                        <label class="form-label small">Phone</label>
                        <input type="tel" name="phone" class="form-control form-control-sm" required
                               value="${editingAddress.phone}">
                    </div>
                    <div class="mb-2">
                        <label class="form-label small">Address Line 1</label>
                        <input type="text" name="addressLine1" class="form-control form-control-sm" required
                               value="${editingAddress.addressLine1}">
                    </div>
                    <div class="mb-2">
                        <label class="form-label small">Address Line 2 (optional)</label>
                        <input type="text" name="addressLine2" class="form-control form-control-sm"
                               value="${editingAddress.addressLine2}">
                    </div>
                    <div class="row g-2 mb-2">
                        <div class="col-6">
                            <label class="form-label small">City</label>
                            <input type="text" name="city" class="form-control form-control-sm" required
                                   value="${editingAddress.city}">
                        </div>
                        <div class="col-6">
                            <label class="form-label small">State</label>
                            <input type="text" name="state" class="form-control form-control-sm" required
                                   value="${editingAddress.state}">
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small">Pincode</label>
                        <input type="text" name="pincode" class="form-control form-control-sm" required
                               value="${editingAddress.pincode}">
                    </div>
                    <div class="form-check mb-3">
                        <input class="form-check-input" type="checkbox" name="isDefault" id="isDefault"
                               ${editingAddress['default'] ? 'checked' : ''}>
                        <label class="form-check-label small" for="isDefault">Set as default address</label>
                    </div>
                    <button type="submit" class="btn btn-gradient w-100">
                        ${not empty editingAddress ? 'Save Changes' : 'Add Address'}
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
