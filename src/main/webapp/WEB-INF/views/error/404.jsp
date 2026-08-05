<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Page Not Found — StyleMart</title>
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body style="display:flex;align-items:center;justify-content:center;height:100vh;flex-direction:column;">
    <h1 class="text-gradient" style="font-size:4rem;">404</h1>
    <p>We couldn't find the page you're looking for.</p>
    <a href="<%= request.getContextPath() %>/index.jsp" class="btn btn-gradient mt-3">Back to Home</a>
</body>
</html>
