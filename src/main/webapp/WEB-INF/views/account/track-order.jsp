<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Track Order #${order.orderNumber} — StyleMart</title>
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
            <h4 class="fw-bold mb-0">Track Order #${order.orderNumber}</h4>
            <div class="text-muted small">Current status: <span class="status-badge status-${order.status}">${order.status}</span></div>
        </div>
        <a href="<%= request.getContextPath() %>/account/orders/view?id=${order.id}" class="small"><i class="fa-solid fa-arrow-left"></i> Back to order</a>
    </div>

    <c:choose>
        <c:when test="${order.status eq 'cancelled' or order.status eq 'returned'}">
            <div class="alert alert-secondary">
                This order's tracking timeline stopped because the order was
                <c:choose><c:when test="${order.status eq 'cancelled'}">cancelled</c:when><c:otherwise>returned</c:otherwise></c:choose>.
            </div>
        </c:when>
        <c:otherwise>
            <div class="border rounded p-4 mb-3">
                <div class="timeline">
                    <c:forEach var="stage" items="${stages}" varStatus="st">
                        <c:set var="reached" value="false"/>
                        <c:set var="reachedAt" value="${null}"/>
                        <c:forEach var="ev" items="${events}">
                            <c:if test="${ev.stage eq stage}">
                                <c:set var="reached" value="true"/>
                                <c:set var="reachedAt" value="${ev.createdAt}"/>
                            </c:if>
                        </c:forEach>
                        <div class="timeline-item ${reached ? 'done' : ''}">
                            <div class="timeline-dot"><i class="fa-solid fa-check"></i></div>
                            <div class="timeline-label text-capitalize">${fn:replace(stage, '_', ' ')}</div>
                            <c:if test="${reached}">
                                <div class="text-muted small"><fmt:formatDate value="${reachedAt}" pattern="dd MMM yyyy, hh:mm a"/></div>
                            </c:if>
                        </div>
                    </c:forEach>
                </div>
            </div>

            <div class="border rounded p-3 bg-light-subtle">
                <div class="small fw-semibold mb-2"><i class="fa-solid fa-flask"></i> Demo control</div>
                <div class="text-muted small mb-2">There's no live courier feed behind this yet, so step the order forward manually to see the timeline update.</div>
                <form action="<%= request.getContextPath() %>/account/orders/track/advance" method="post">
                    <input type="hidden" name="id" value="${order.id}">
                    <button type="submit" class="btn btn-gradient btn-sm" ${fn:length(events) >= fn:length(stages) ? 'disabled' : ''}>
                        Simulate next update
                    </button>
                </form>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
