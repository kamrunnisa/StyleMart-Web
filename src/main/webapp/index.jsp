<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StyleMart — Fashion, Delivered</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/aos@2.3.4/dist/aos.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/assets/css/style.css" rel="stylesheet">
</head>
<body>

<%@ include file="/WEB-INF/views/partials/navbar.jspf" %>

<!-- Hero Banner Slider -->
<section class="hero-slider">
    <div id="heroCarousel" class="carousel slide" data-bs-ride="carousel">
        <div class="carousel-indicators">
            <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="0" class="active" aria-current="true"></button>
            <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="1"></button>
        </div>
        <div class="carousel-inner">
            <div class="carousel-item active">
                <div class="hero-slide hero-slide-1">
                    <span class="hero-badge">UP TO<br>60% OFF</span>
                    <div class="hero-content" data-aos="fade-up">
                        <h1>End of Season Sale</h1>
                        <p>Up to 60% off on top brands</p>
                        <a href="<%= request.getContextPath() %>/products" class="btn btn-gradient">Shop Now</a>
                    </div>
                </div>
            </div>
            <div class="carousel-item">
                <div class="hero-slide hero-slide-2">
                    <span class="hero-badge">NEW<br>DROP</span>
                    <div class="hero-content" data-aos="fade-up">
                        <h1>New Arrivals</h1>
                        <p>Fresh styles, just landed</p>
                        <a href="<%= request.getContextPath() %>/products" class="btn btn-gradient">Explore</a>
                    </div>
                </div>
            </div>
        </div>
        <button class="carousel-control-prev" type="button" data-bs-target="#heroCarousel" data-bs-slide="prev">
            <span class="carousel-control-prev-icon"></span>
        </button>
        <button class="carousel-control-next" type="button" data-bs-target="#heroCarousel" data-bs-slide="next">
            <span class="carousel-control-next-icon"></span>
        </button>
    </div>
</section>

<!-- Categories -->
<section class="container py-5">
    <h2 class="section-title" data-aos="fade-up">Shop by Category</h2>
    <div class="row g-4 category-grid">
        <div class="col-6 col-md-3">
            <a href="<%= request.getContextPath() %>/products?category=men" class="category-card glass-card" data-aos="zoom-in">
                <i class="fa-solid fa-shirt"></i>
                <span>Men</span>
            </a>
        </div>
        <div class="col-6 col-md-3">
            <a href="<%= request.getContextPath() %>/products?category=women" class="category-card glass-card" data-aos="zoom-in" data-aos-delay="100">
                <i class="fa-solid fa-person-dress"></i>
                <span>Women</span>
            </a>
        </div>
        <div class="col-6 col-md-3">
            <a href="<%= request.getContextPath() %>/products?category=kids" class="category-card glass-card" data-aos="zoom-in" data-aos-delay="200">
                <i class="fa-solid fa-child"></i>
                <span>Kids</span>
            </a>
        </div>
        <div class="col-6 col-md-3">
            <a href="<%= request.getContextPath() %>/products?category=footwear" class="category-card glass-card" data-aos="zoom-in" data-aos-delay="300">
                <i class="fa-solid fa-shoe-prints"></i>
                <span>Footwear</span>
            </a>
        </div>
    </div>
</section>

<!-- Trending Products (populated via AJAX -- see assets/js/main.js) -->
<section class="container py-5">
    <h2 class="section-title" data-aos="fade-up">Trending Now</h2>
    <div id="trendingProducts" class="row g-4 product-grid">
        <!-- Skeleton loaders shown until AJAX resolves -->
        <c:forEach begin="1" end="4">
            <div class="col-6 col-md-3">
                <div class="skeleton-card"></div>
            </div>
        </c:forEach>
    </div>
</section>

<!-- Newsletter -->
<section class="newsletter-section">
    <div class="container text-center py-5" data-aos="fade-up">
        <h3>Stay in Style</h3>
        <p>Subscribe for early access to sales and new drops</p>
        <form class="newsletter-form d-flex justify-content-center gap-2">
            <input type="email" class="form-control" placeholder="Enter your email" style="max-width:320px" required>
            <button type="submit" class="btn btn-gradient">Subscribe</button>
        </form>
    </div>
</section>

<%@ include file="/WEB-INF/views/partials/footer.jspf" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/aos@2.3.4/dist/aos.js"></script>
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
</body>
</html>
