<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Return Status — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
    <style>
        .timeline { position: relative; padding-left: 32px; }
        .timeline::before {
            content: ""; position: absolute; left: 9px; top: 4px; bottom: 4px; width: 2px; background: #e5e5e5;
        }
        .timeline-item { position: relative; padding-bottom: 28px; }
        .timeline-item:last-child { padding-bottom: 0; }
        .timeline-dot {
            position: absolute; left: -32px; top: 2px; width: 20px; height: 20px; border-radius: 50%;
            background: #e5e5e5; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 0.7rem;
        }
        .timeline-item.done .timeline-dot { background: linear-gradient(135deg, var(--sm-primary), var(--sm-accent)); }
        .timeline-item.done .timeline-label { color: var(--sm-dark); font-weight: 600; }
        .timeline-label { color: var(--sm-muted); }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<div class="container py-4" style="max-width: 720px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="fw-bold mb-0">Return &amp; Refund — Order #${order.orderNumber}</h4>
            <div class="text-muted small">Reason: ${ret.reason}<c:if test="${not empty ret.comment}"> &middot; ${ret.comment}</c:if></div>
        </div>
        <a href="<%= request.getContextPath() %>/account/orders/view?id=${order.id}" class="small"><i class="fa-solid fa-arrow-left"></i> Back to order</a>
    </div>

    <c:choose>
        <c:when test="${ret.status eq 'rejected'}">
            <div class="alert alert-danger">This return request was rejected.</div>
        </c:when>
        <c:otherwise>
            <c:choose>
                <c:when test="${ret.status eq 'requested'}"><c:set var="idx" value="0"/></c:when>
                <c:when test="${ret.status eq 'pickup_scheduled'}"><c:set var="idx" value="1"/></c:when>
                <c:when test="${ret.status eq 'picked_up'}"><c:set var="idx" value="2"/></c:when>
                <c:when test="${ret.status eq 'refund_initiated'}"><c:set var="idx" value="3"/></c:when>
                <c:when test="${ret.status eq 'refund_completed'}"><c:set var="idx" value="4"/></c:when>
                <c:otherwise><c:set var="idx" value="0"/></c:otherwise>
            </c:choose>

            <div class="border rounded p-4 mb-3">
                <div class="timeline">
                    <c:forEach var="label" items="${['Return Requested','Pickup Scheduled','Picked Up','Refund Initiated','Refund Completed']}" varStatus="st">
                        <div class="timeline-item ${st.index le idx ? 'done' : ''}">
                            <div class="timeline-dot"><i class="fa-solid fa-check"></i></div>
                            <div class="timeline-label">${label}</div>
                            <c:if test="${st.index eq 0 and st.index le idx}">
                                <div class="text-muted small"><fmt:formatDate value="${ret.requestedAt}" pattern="dd MMM yyyy, hh:mm a"/></div>
                            </c:if>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <c:if test="${not empty ret.refund}">
                <div class="border rounded p-3 mb-3">
                    <h6 class="fw-bold mb-2">Refund details</h6>
                    <div class="d-flex justify-content-between mb-1">
                        <span class="text-muted small">Amount</span>
                        <span class="fw-semibold small">&#8377;<fmt:formatNumber value="${ret.refund.amount}" maxFractionDigits="2"/></span>
                    </div>
                    <div class="d-flex justify-content-between mb-1">
                        <span class="text-muted small">Method</span>
                        <span class="fw-semibold small">${ret.refund.method}</span>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span class="text-muted small">Status</span>
                        <span class="fw-semibold small text-capitalize">${ret.refund.status}</span>
                    </div>
                </div>
            </c:if>

            <c:if test="${ret.status ne 'refund_completed'}">
                <div class="border rounded p-3 bg-light-subtle">
                    <div class="small fw-semibold mb-2"><i class="fa-solid fa-flask"></i> Demo control</div>
                    <div class="text-muted small mb-2">There's no live logistics/finance backend behind this yet, so step the return forward manually to see the timeline update.</div>
                    <form action="<%= request.getContextPath() %>/account/orders/return/advance" method="post">
                        <input type="hidden" name="id" value="${order.id}">
                        <button type="submit" class="btn btn-gradient btn-sm">Simulate next update</button>
                    </form>
                </div>
            </c:if>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
