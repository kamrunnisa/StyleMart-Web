<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Returns Policy — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>
<div class="container py-5" style="max-width:800px">
    <h2 class="section-title mb-4">Returns &amp; Refunds Policy</h2>
    <p class="text-muted">Most items are eligible for return within 7 days of delivery, provided tags are intact and
        the item is unused. Refunds are issued to the original payment method, or as store credit for
        Cash-on-Delivery orders.</p>
    <p class="text-muted small">This is placeholder policy text for a development project and is not a legal
        document. Order-level return requests will be available from the Orders module once it ships.</p>
</div>
<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
