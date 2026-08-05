<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Contact Us — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>
<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>
<div class="container py-5" style="max-width:800px">
    <h2 class="section-title mb-4">Contact Us</h2>
    <div class="row g-4">
        <div class="col-md-4">
            <i class="fa-solid fa-envelope text-danger mb-2"></i>
            <h6>Email</h6>
            <p class="text-muted small">support@stylemart.com</p>
        </div>
        <div class="col-md-4">
            <i class="fa-solid fa-phone text-danger mb-2"></i>
            <h6>Phone</h6>
            <p class="text-muted small">+91 1800-000-0000</p>
        </div>
        <div class="col-md-4">
            <i class="fa-solid fa-clock text-danger mb-2"></i>
            <h6>Support Hours</h6>
            <p class="text-muted small">Mon–Sat, 9 AM – 7 PM</p>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/views/partials/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
