package com.stylemart.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * A row from `payments` -- one per order. `provider` and `failureReason` are
 * display-only extras (e.g. "Google Pay", "HDFC Net Banking") stored in the
 * optional columns added by database/migrations/002_payment_provider.sql.
 */
public class Payment {
    private int id;
    private int orderId;
    private String method;         // cod | online
    private String provider;       // e.g. "UPI - Google Pay", "Visa Card", "HDFC Net Banking"
    private String transactionId;
    private BigDecimal amount;
    private String status;         // pending | success | failed | refunded
    private String failureReason;
    private Timestamp paidAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public Timestamp getPaidAt() { return paidAt; }
    public void setPaidAt(Timestamp paidAt) { this.paidAt = paidAt; }

    public boolean isPending() { return "pending".equals(status); }
    public boolean isSuccess() { return "success".equals(status); }
    public boolean isFailed() { return "failed".equals(status); }
}
