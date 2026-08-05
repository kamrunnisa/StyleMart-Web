<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>About Us — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>
<div class="container py-5" style="max-width:800px">
    <h2 class="section-title mb-4">About StyleMart</h2>
    <p class="text-muted">StyleMart is a fashion destination bringing together apparel, footwear, and accessories from a
        wide range of brands into one convenient storefront — built to make discovering, comparing, and buying
        styles as effortless as browsing your favorite catalog.</p>
    <p class="text-muted">We're a small, focused team obsessed with clean design, fast pages, and a checkout that
        doesn't get in your way. This site is under active development — new features are shipping continuously.</p>
</div>
<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
