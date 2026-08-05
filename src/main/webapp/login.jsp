<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Log In — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/auth.css" rel="stylesheet">
</head>
<body class="auth-body">
<div class="auth-card glass-card" data-aos="fade-up">
    <h2>Welcome back</h2>
    <p class="text-muted">Log in to continue to StyleMart</p>

    <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="alert alert-danger"><%= request.getAttribute("errorMessage") %></div>
    <% } %>
    <% if ("1".equals(request.getParameter("verified"))) { %>
        <div class="alert alert-success">Email verified successfully. You can now log in.</div>
    <% } %>

    <form action="<%= request.getContextPath() %>/login" method="post">
        <div class="mb-3">
            <label class="form-label">Email</label>
            <input type="email" name="email" class="form-control" required>
        </div>
        <div class="mb-3">
            <label class="form-label">Password</label>
            <div class="password-field">
                <input type="password" name="password" id="loginPassword" class="form-control" required>
                <i class="fa-regular fa-eye toggle-password" data-target="loginPassword"></i>
            </div>
        </div>
        <div class="d-flex justify-content-between mb-3">
            <div class="form-check">
                <input class="form-check-input" type="checkbox" name="rememberMe" id="rememberMe">
                <label class="form-check-label" for="rememberMe">Remember me</label>
            </div>
            <a href="<%= request.getContextPath() %>/forgot-password.jsp" class="small">Forgot password?</a>
        </div>
        <button type="submit" class="btn btn-gradient w-100">Log In</button>
    </form>

    <p class="text-center mt-3 small">New to StyleMart? <a href="<%= request.getContextPath() %>/register.jsp">Create an account</a></p>
</div>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
