# StyleMart — Fashion E-Commerce Web Application

StyleMart is a full-stack fashion e-commerce platform built with **Java, JSP, and Servlets**, backed by a **MySQL** database. It covers the complete shopping flow — from browsing products to placing and tracking an order — plus an admin dashboard for managing the catalog.

---

## Features

### Customer-facing
- **Authentication** — registration with email OTP verification, login, session-based auth, password hashing (BCrypt)
- **Product catalog** — category browsing (Men / Women / Kids / Footwear), search, filters (brand, size, price range), sorting
- **Product details** — image gallery with zoom, size/color selection, ratings
- **Cart** — add/update/remove items, live order summary, coupon codes, GST and delivery calculation
- **Wishlist**
- **Checkout** — address book (multiple saved addresses, default address), payment method selection
- **Orders** — order placement, order history, order tracking timeline, cancel/return requests
- **Payments** — simulated payment flow (success/failure pages)
- **Static pages** — About Us, Contact Us, FAQ, Privacy Policy, Returns Policy

### Admin
- Admin login (separate from customer accounts)
- Product management — add/edit products, upload main image and gallery images, activate/deactivate
- Dashboard overview of the catalog

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java (Jakarta Servlet / JSP) |
| Server | Apache Tomcat 10.1+ |
| Database | MySQL (via XAMPP or standalone) |
| Build tool | Maven |
| Frontend | JSP, HTML, CSS, vanilla JavaScript, Bootstrap 5 |
| Security | BCrypt password hashing, prepared statements (SQL-injection safe), session-based auth filter |

---

## Project Structure

```
StyleMart-Web/
├── pom.xml                          # Maven build configuration
├── database/
│   ├── stylemart_web.sql            # Full schema + sample data
│   └── migrations/                  # Incremental schema updates
├── deploy/
│   └── stylemart-web-context.xml    # Tomcat context config for external image storage
└── src/main/
    ├── resources/
    │   ├── db.properties             # Database connection settings
    │   └── mail.properties           # SMTP settings for OTP emails
    ├── java/com/stylemart/
    │   ├── controller/               # Servlets (one per feature area)
    │   ├── dao/                      # Data-access layer (PreparedStatement only)
    │   ├── model/                    # Plain data classes
    │   ├── service/                  # Business logic (e.g. AuthService)
    │   ├── util/                     # Helpers (pricing, file upload, password hashing, email)
    │   └── listener/                 # App startup listener
    └── webapp/
        ├── WEB-INF/views/            # JSP views (account pages, admin panel, product listing/details)
        ├── assets/
        │   ├── css/                  # style.css, auth.css
        │   ├── js/                   # main.js
        │   └── img/products/         # Product images
        └── *.jsp                     # Top-level pages (home, cart, checkout, login, etc.)
```

---

## Getting Started (Local Setup)

### Prerequisites
- JDK 17+
- Maven
- Apache Tomcat 10.1+
- MySQL (e.g. via XAMPP)

### 1. Database
1. Start MySQL (via XAMPP or your preferred method).
2. Open phpMyAdmin and create a database.
3. Import `database/stylemart_web.sql`, then apply any files in `database/migrations/` in order.
4. Update `src/main/resources/db.properties` with your database name, username, and password.

### 2. Build
```bash
mvn clean package
```
This produces `target/stylemart-web.war`.

### 3. Deploy
Copy the WAR file into Tomcat's `webapps/` folder:
```bash
copy target\stylemart-web.war C:\path\to\tomcat\webapps\stylemart-web.war
```
Start Tomcat, then visit:
```
http://localhost:8080/stylemart-web/
```

### 4. Product image storage (recommended)
By default, images uploaded through the admin panel are written to a folder outside the deployed webapp (so they survive future rebuilds/redeploys). To enable this, copy `deploy/stylemart-web-context.xml` into:
```
<tomcat>/conf/Catalina/localhost/stylemart-web.xml
```
This maps `${user.home}/stylemart-uploads/products` to the app's `/assets/img/products` URL path — create that folder if it doesn't already exist.

---

## Notes

- OTP emails are sent via the settings in `mail.properties` — configure a real SMTP account for this to work end-to-end.
- Online payment is simulated for demonstration purposes; no real payment gateway is integrated.
- All SQL queries use `PreparedStatement` to prevent SQL injection.

---

## License

This project is for educational/portfolio purposes.