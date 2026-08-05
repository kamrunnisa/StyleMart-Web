<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Something Went Wrong — StyleMart</title>
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body style="display:flex;align-items:center;justify-content:center;height:100vh;flex-direction:column;">
    <h1 class="text-gradient" style="font-size:4rem;">500</h1>
    <p>Something went wrong on our end. Please try again shortly.</p>
    <a href="<%= request.getContextPath() %>/index.jsp" class="btn btn-gradient mt-3">Back to Home</a>
</body>
</html>
