-- Migration 002: adds display-only columns to `payments` for the new
-- payment gateway module. Purely additive -- safe to run on your existing
-- database, nothing else changes and no existing rows are affected.
--
-- Run with:  mysql -u <user> -p stylemart_web < database/migrations/002_payment_provider.sql

ALTER TABLE payments
    ADD COLUMN provider VARCHAR(60) NULL AFTER method,
    ADD COLUMN failure_reason VARCHAR(255) NULL AFTER status;
