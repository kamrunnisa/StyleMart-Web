<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/auth.css" rel="stylesheet">
</head>
<body class="auth-body">
<div class="auth-card glass-card" data-aos="fade-up">
    <h2>Create your account</h2>
    <p class="text-muted">Join StyleMart for exclusive drops and offers</p>

    <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="alert alert-danger"><%= request.getAttribute("errorMessage") %></div>
    <% } %>

    <form action="<%= request.getContextPath() %>/register" method="post">
        <div class="mb-3">
            <label class="form-label">Full Name</label>
            <input type="text" name="fullName" class="form-control" required>
        </div>
        <div class="mb-3">
            <label class="form-label">Email</label>
            <input type="email" name="email" class="form-control" required>
        </div>
        <div class="mb-3">
            <label class="form-label">Phone</label>
            <input type="tel" name="phone" class="form-control" inputmode="numeric"
                   pattern="[0-9]{10}" maxlength="10" placeholder="10-digit mobile number"
                   title="Enter a 10-digit phone number"
                   oninput="this.value = this.value.replace(/[^0-9]/g, '').slice(0, 10)">
        </div>
        <div class="mb-3">
            <label class="form-label">Password</label>
            <div class="password-field">
                <input type="password" name="password" id="registerPassword" class="form-control" minlength="6" required>
                <i class="fa-regular fa-eye toggle-password" data-target="registerPassword"></i>
            </div>
        </div>
        <button type="submit" class="btn btn-gradient w-100">Create Account</button>
    </form>

    <p class="text-center mt-3 small">Already have an account? <a href="<%= request.getContextPath() %>/login.jsp">Log in</a></p>
</div>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
