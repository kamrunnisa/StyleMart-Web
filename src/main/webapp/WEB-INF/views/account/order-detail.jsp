<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order #${order.orderNumber} — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<div class="container py-4">
    <c:if test="${justPlaced}">
        <div class="alert alert-success">
            <i class="fa-solid fa-circle-check"></i> Your order has been placed successfully!
        </div>
    </c:if>

    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="fw-bold mb-0">Order #${order.orderNumber}</h4>
            <div class="text-muted small">Placed <fmt:formatDate value="${order.placedAt}" pattern="dd MMM yyyy, hh:mm a"/></div>
        </div>
        <a href="<%= request.getContextPath() %>/account/orders" class="small"><i class="fa-solid fa-arrow-left"></i> All orders</a>
    </div>

    <c:choose>
        <c:when test="${order.status eq 'cancelled'}">
            <div class="alert alert-danger">
                This order was cancelled.
                <c:if test="${not empty order.cancelReason}"> Reason: ${order.cancelReason}.</c:if>
            </div>
        </c:when>
        <c:when test="${order.status eq 'returned'}">
            <div class="alert alert-secondary">
                A return was requested for this order.
                <a href="<%= request.getContextPath() %>/account/orders/return-status?id=${order.id}" class="alert-link">View return status &rarr;</a>
            </div>
        </c:when>
        <c:otherwise>
            <c:choose>
                <c:when test="${order.status eq 'placed'}"><c:set var="currentIndex" value="0"/></c:when>
                <c:when test="${order.status eq 'accepted'}"><c:set var="currentIndex" value="1"/></c:when>
                <c:when test="${order.status eq 'shipped'}"><c:set var="currentIndex" value="2"/></c:when>
                <c:when test="${order.status eq 'delivered'}"><c:set var="currentIndex" value="3"/></c:when>
                <c:otherwise><c:set var="currentIndex" value="0"/></c:otherwise>
            </c:choose>
            <div class="order-tracker d-flex justify-content-between mb-2">
                <c:forEach var="label" items="${['Placed','Accepted','Shipped','Delivered']}" varStatus="st">
                    <div class="tracker-step ${st.index le currentIndex ? 'done' : ''}">
                        <div class="tracker-dot"><i class="fa-solid fa-check"></i></div>
                        <div class="tracker-label small">${label}</div>
                    </div>
                </c:forEach>
            </div>
            <div class="text-end mb-4">
                <a href="<%= request.getContextPath() %>/account/orders/track?id=${order.id}" class="small">
                    <i class="fa-solid fa-truck"></i> View detailed tracking &rarr;
                </a>
            </div>
        </c:otherwise>
    </c:choose>

    <div class="row g-4">
        <div class="col-lg-7">
            <div class="border rounded p-3 mb-4">
                <h6 class="fw-bold mb-3">Items</h6>
                <c:forEach var="item" items="${order.items}">
                    <div class="d-flex gap-3 align-items-center border-bottom pb-2 mb-2">
                        <img src="<%= request.getContextPath() %>/assets/img/products/${not empty item.thumbnail ? item.thumbnail : 'placeholder.jpg'}"
                             alt="${item.productName}" style="width:56px;height:70px;object-fit:cover;border-radius:6px;">
                        <div class="flex-grow-1">
                            <div class="fw-semibold small">${item.productName}</div>
                            <div class="text-muted small">
                                Qty: ${item.quantity}
                                <c:if test="${not empty item.size}"> &middot; Size: ${item.size}</c:if>
                                <c:if test="${not empty item.color}"> &middot; Color: ${item.color}</c:if>
                            </div>
                        </div>
                        <div class="fw-semibold small">&#8377;<fmt:formatNumber value="${item.lineTotal}" maxFractionDigits="0"/></div>
                    </div>
                </c:forEach>
            </div>

            <div class="border rounded p-3">
                <h6 class="fw-bold mb-3">Delivery Address</h6>
                <div class="fw-semibold">${order.address.fullName} <span class="badge bg-light text-dark border">${order.address.label}</span></div>
                <div class="text-muted small">
                    ${order.address.addressLine1}<c:if test="${not empty order.address.addressLine2}">, ${order.address.addressLine2}</c:if>,
                    ${order.address.city}, ${order.address.state} - ${order.address.pincode}
                </div>
                <div class="text-muted small">Phone: ${order.address.phone}</div>
            </div>
        </div>

        <div class="col-lg-5">
            <div class="border rounded p-3 mb-3">
                <h6 class="fw-bold mb-3">Payment Summary</h6>
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-muted">Subtotal</span>
                    <span>&#8377;<fmt:formatNumber value="${order.subtotal}" maxFractionDigits="0"/></span>
                </div>
                <c:if test="${order.discountAmount > 0}">
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted">Coupon discount</span>
                        <span class="text-success">-&#8377;<fmt:formatNumber value="${order.discountAmount}" maxFractionDigits="0"/></span>
                    </div>
                </c:if>
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-muted">GST</span>
                    <span>&#8377;<fmt:formatNumber value="${order.taxAmount}" maxFractionDigits="0"/></span>
                </div>
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-muted">Delivery</span>
                    <span>
                        <c:choose>
                            <c:when test="${order.deliveryCharge > 0}">&#8377;<fmt:formatNumber value="${order.deliveryCharge}" maxFractionDigits="0"/></c:when>
                            <c:otherwise>FREE</c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <hr>
                <div class="d-flex justify-content-between mb-2">
                    <span class="fw-bold">Total</span>
                    <span class="fw-bold fs-5">&#8377;<fmt:formatNumber value="${order.totalAmount}" maxFractionDigits="0"/></span>
                </div>
                <div class="text-muted small">
                    Payment: ${order.paymentMethod eq 'cod' ? 'Cash on Delivery' : 'Paid Online'}
                    (${order.paymentStatus})
                </div>
            </div>

            <c:if test="${order.cancellable}">
                <button type="button" class="btn btn-outline-danger w-100 mb-2" data-bs-toggle="modal" data-bs-target="#cancelModal">
                    Cancel Order
                </button>
            </c:if>
            <c:if test="${order.returnable}">
                <button type="button" class="btn btn-outline-secondary w-100 mb-2" data-bs-toggle="modal" data-bs-target="#returnModal">
                    Return Order
                </button>
            </c:if>
            <form action="<%= request.getContextPath() %>/account/orders/buy-again" method="post" class="mb-2">
                <input type="hidden" name="id" value="${order.id}">
                <button type="submit" class="btn btn-outline-primary w-100"><i class="fa-solid fa-rotate"></i> Buy Again</button>
            </form>
            <a href="#" class="btn btn-outline-secondary w-100 disabled" title="Invoice download is coming in a later update">
                <i class="fa-solid fa-file-invoice"></i> Download Invoice
            </a>
        </div>
    </div>
</div>

<!-- Cancel reason modal -->
<div class="modal fade" id="cancelModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <form action="<%= request.getContextPath() %>/account/orders/cancel" method="post" class="modal-content">
            <input type="hidden" name="id" value="${order.id}">
            <div class="modal-header">
                <h6 class="modal-title fw-bold">Cancel this order?</h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <label class="form-label small fw-semibold">Why are you cancelling?</label>
                <select name="reason" class="form-select" required>
                    <option value="">Choose a reason</option>
                    <option>Changed my mind</option>
                    <option>Wrong product ordered</option>
                    <option>Ordered by mistake</option>
                    <option>Found a better price elsewhere</option>
                    <option>Other</option>
                </select>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Keep Order</button>
                <button type="submit" class="btn btn-danger">Confirm Cancellation</button>
            </div>
        </form>
    </div>
</div>

<!-- Return reason modal -->
<div class="modal fade" id="returnModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <form action="<%= request.getContextPath() %>/account/orders/return" method="post" class="modal-content">
            <input type="hidden" name="id" value="${order.id}">
            <div class="modal-header">
                <h6 class="modal-title fw-bold">Request a return</h6>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <label class="form-label small fw-semibold">Reason for return</label>
                <select name="reason" class="form-select mb-2" required>
                    <option value="">Choose a reason</option>
                    <option>Size doesn't fit</option>
                    <option>Product damaged or defective</option>
                    <option>Wrong item delivered</option>
                    <option>Not as described</option>
                    <option>Other</option>
                </select>
                <label class="form-label small fw-semibold">Additional details (optional)</label>
                <textarea name="comment" class="form-control" rows="2" maxlength="255"></textarea>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-gradient">Submit Return Request</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
