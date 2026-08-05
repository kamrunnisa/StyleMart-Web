<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify Email — StyleMart</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/auth.css" rel="stylesheet">
</head>
<body class="auth-body">
<div class="auth-card glass-card" data-aos="fade-up">
    <h2>Verify your email</h2>
    <p class="text-muted">
        We sent a 6-digit code to
        <strong><c:out value="${param.email}"/></strong>
        &mdash; it expires in 5 minutes.
    </p>

    <c:if test="${not empty param.error}">
        <div class="alert alert-danger"><c:out value="${param.error}"/></div>
    </c:if>
    <c:if test="${not empty param.sent}">
        <div class="alert alert-success"><c:out value="${param.sent}"/></div>
    </c:if>

    <form action="<%= request.getContextPath() %>/verify-otp" method="post">
        <input type="hidden" name="email" value="${param.email}">
        <div class="mb-3">
            <label class="form-label">OTP Code</label>
            <input type="text" name="otp" class="form-control text-center" inputmode="numeric"
                   pattern="[0-9]{6}" maxlength="6" placeholder="000000" required autofocus>
        </div>
        <button type="submit" class="btn btn-gradient w-100">Verify</button>
    </form>

    <form action="<%= request.getContextPath() %>/resend-otp" method="post" id="resendForm" class="mt-3 text-center">
        <input type="hidden" name="email" value="${param.email}">
        <button type="submit" class="btn btn-link btn-sm" id="resendBtn">Resend Code</button>
        <span class="text-muted small" id="resendTimer"></span>
    </form>
</div>

<script>
    // Client-side countdown is UX only -- the real 60s cooldown is enforced
    // server-side in AuthService.resendOtp() via the otp_sent_at column, so
    // this can't be bypassed by just re-POSTing the resend form.
    (function () {
        var COOLDOWN = 60;
        var btn = document.getElementById('resendBtn');
        var timer = document.getElementById('resendTimer');
        var remaining = COOLDOWN;

        function tick() {
            if (remaining <= 0) {
                btn.disabled = false;
                timer.textContent = '';
                return;
            }
            btn.disabled = true;
            timer.textContent = ' (available in ' + remaining + 's)';
            remaining--;
            setTimeout(tick, 1000);
        }
        tick();
    })();
</script>
</body>
</html>
