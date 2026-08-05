<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Login — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/auth.css" rel="stylesheet">
</head>
<body class="auth-body">
<div class="auth-card glass-card">
    <h2>Admin Login</h2>
    <p class="text-muted">StyleMart back office</p>

    <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="alert alert-danger"><%= request.getAttribute("errorMessage") %></div>
    <% } %>

    <form action="<%= request.getContextPath() %>/admin/login" method="post">
        <div class="mb-3">
            <label class="form-label">Admin Email</label>
            <input type="email" name="email" class="form-control" required autofocus>
        </div>
        <div class="mb-3">
            <label class="form-label">Password</label>
            <input type="password" name="password" class="form-control" required>
        </div>
        <button type="submit" class="btn btn-gradient w-100">Log In</button>
    </form>
    <p class="text-muted small mt-3 mb-0">Default admin: admin@stylemart.com / Admin@123 (change after first login)</p>
</div>
</body>
</html>
