# StyleMart-Web — Fashion E-Commerce Website (Java / JSP / Servlet)

## Phase 1 — what's included

```
StyleMart-Web/
├── pom.xml                              # Maven config: servlet/JSP/JSTL, MySQL driver, jBCrypt, Gson
├── database/
│   └── stylemart_web.sql                # 13 tables + FKs + constraints + sample data
├── src/main/resources/
│   └── db.properties                    # JDBC connection settings (XAMPP defaults)
└── src/main/
    ├── java/com/stylemart/
    │   ├── util/DBConnection.java       # JDBC connection helper
    │   ├── util/PasswordUtil.java       # BCrypt hashing
    │   ├── model/User.java, Product.java
    │   ├── dao/UserDAO.java, ProductDAO.java   # prepared statements only
    │   ├── service/AuthService.java     # business logic layer
    │   └── controller/
    │       ├── AuthFilter.java          # session guard for /account/*, /admin/*
    │       ├── LoginServlet.java
    │       ├── RegisterServlet.java
    │       ├── VerifyOtpServlet.java
    │       └── TrendingProductsServlet.java   # JSON API consumed by main.js
    └── webapp/
        ├── WEB-INF/web.xml
        ├── WEB-INF/views/partials/navbar.jspf, footer.jspf
        ├── WEB-INF/views/error/404.jsp, 500.jsp
        ├── index.jsp, login.jsp, register.jsp, verify-otp.jsp
        └── assets/css/style.css, auth.css
        └── assets/js/main.js
```

## Phase 4 — what's included

```
src/main/
├── java/com/stylemart/
│   ├── model/Address.java, Coupon.java, Order.java, OrderItem.java, OrderSummary.java
│   ├── dao/AddressDAO.java     # CRUD + default-address handling (one default per user, enforced)
│   ├── dao/CouponDAO.java      # lookup by code/id
│   ├── dao/OrderDAO.java       # transactional placement (stock re-check + decrement, cart clear,
│   │                           #   payments row) + history/detail/cancel/return
│   ├── util/PricingUtil.java   # single source of truth for subtotal → discount → GST → delivery → total
│   └── controller/
│       ├── CartServlet.java        # extended: /cart/coupon/apply, /cart/coupon/remove
│       ├── CheckoutServlet.java    # /checkout, /checkout/place
│       ├── AddressServlet.java     # /account/addresses (+ /save, /delete, /default)
│       └── OrderServlet.java       # /account/orders (+ /view, /cancel, /return) — replaces the Phase 3 placeholder
└── webapp/
    ├── cart.jsp             # updated: coupon box, live AJAX summary (subtotal/discount/GST/delivery/total)
    ├── checkout.jsp         # address picker, payment method, final order summary, place order
    └── WEB-INF/views/account/
        ├── addresses.jsp    # address book list + add/edit form
        ├── orders.jsp       # order history
        └── order-detail.jsp # order detail, visual tracking timeline, cancel/return actions
```

**Pricing model** (`PricingUtil`, simplified for this project — real GST/shipping would come from tax slabs and a carrier API):
- GST: flat 5% on `(subtotal − coupon discount)`
- Delivery: ₹79, free at/above ₹999 subtotal
- Coupons: `flat` or `percent` (with optional max-discount cap), gated by `min_order_value` and validity dates; re-validated against the live cart on every read so a coupon silently drops if the cart no longer qualifies

**Order lifecycle**: `placed → accepted → shipped → delivered`, with `cancelled` (only while `placed`/`accepted`, restocks items) and `returned` (only once `delivered`) as terminal side-branches. Placement is one DB transaction — order + order_items + payments + stock decrement + cart clear all succeed or all roll back, with a row-level `FOR UPDATE` stock check to prevent overselling on concurrent checkouts.

## Not yet wired (fair to flag)

- Online payment is simulated (`payment_status` flips straight to `paid`) — no real gateway integration
- Order status transitions `accepted → shipped → delivered` are DB-only for now; they'll get admin-panel controls in Phase 5
- `/account/profile` is still the Phase 3 placeholder — profile editing wasn't in the Phase 4 scope



1. **Database**: start MySQL in XAMPP, open phpMyAdmin, import `database/stylemart_web.sql`.
2. **Build**: `mvn clean package` — produces `target/stylemart-web.war`.
3. **Deploy**: drop the WAR into Tomcat 10's `webapps/` folder (Tomcat 10 uses the Jakarta `jakarta.servlet.*` namespace, which is why `web.xml` and the dependencies target that, not the older `javax.servlet.*`).
4. Visit `http://localhost:8080/stylemart-web/` — home page loads, trending products populate via AJAX from `/api/products/trending` once the DB is seeded.
5. Before anything beyond local testing: change nothing is hardcoded as a secret here, but wire `AuthService.register()` to a real SMTP mailer (JavaMail) instead of `System.out.println` for the OTP.

## Design notes

- **DAO pattern**: all SQL lives in `dao/`, always via `PreparedStatement` — no string-concatenated queries anywhere (SQL-injection protection).
- **Service layer**: `AuthService` sits between servlets and DAOs, holds validation/business rules, throws a typed `AuthException` servlets translate into user-facing messages.
- **Session auth**: `AuthFilter` blocks `/account/*` and `/admin/*` for anyone without `session.userId`; admin routes additionally require `role=admin`.
- **XSS**: JSPs currently only echo server-controlled or already-validated values; once product/review content (user-generated text) is rendered, use JSTL `<c:out>` or `fn:escapeXml` rather than raw `<%= %>` scriptlets.

## Roadmap (matches the phases you specified)

- ✅ **Phase 1** — folder structure, database, Maven setup, JDBC, base JSP/CSS/JS, servlet config
- ✅ **Phase 2** — Authentication module
- ✅ **Phase 3** — Home page data wiring, full product listing + detail pages, live search, filters
- ✅ **Phase 4** — Wishlist, cart (add/update/remove, coupon, GST, delivery), checkout, address management, order placement/history/tracking/cancel/return *(this delivery)*
- **Phase 5** — Admin panel: dashboard, product/category/order/user management, coupons/offers, reports
- **Phase 6** — Final UI polish, dark mode toggle wiring, testing, deployment guide

Tell me which phase to build next.
