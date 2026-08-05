-- Migration 003: order tracking timeline + cancel reasons + returns/refunds.
-- Purely additive: one new nullable column on `orders`, three new tables.
-- Nothing existing is renamed or dropped, so it's safe to run on top of your
-- current database (and of migration 002).
--
-- Run with:  mysql -u <user> -p stylemart_web < database/migrations/003_tracking_returns_refunds.sql

ALTER TABLE orders
    ADD COLUMN cancel_reason VARCHAR(100) NULL AFTER status;

-- Fine-grained delivery timeline, independent of `orders.status` (which stays
-- the simple state machine everything else already relies on). Every stage
-- reached gets one row here with a timestamp, which is what the tracker UI
-- renders as a progress bar.
CREATE TABLE tracking_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    stage ENUM('placed','confirmed','packed','shipped','out_for_delivery','delivered')
        NOT NULL,
    note VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE returns (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    user_id INT NOT NULL,
    reason VARCHAR(100) NOT NULL,
    comment VARCHAR(255) NULL,
    status ENUM('requested','pickup_scheduled','picked_up','refund_initiated','refund_completed','rejected')
        DEFAULT 'requested',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE refunds (
    id INT AUTO_INCREMENT PRIMARY KEY,
    return_id INT NOT NULL,
    order_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    method VARCHAR(30) NOT NULL,
    status ENUM('initiated','processing','completed','failed') DEFAULT 'initiated',
    initiated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (return_id) REFERENCES returns(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;
