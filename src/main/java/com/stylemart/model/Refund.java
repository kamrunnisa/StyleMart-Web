package com.stylemart.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** A row from `refunds` -- created once a return reaches the refund_initiated stage. */
public class Refund {
    private int id;
    private int returnId;
    private int orderId;
    private BigDecimal amount;
    private String method;
    private String status; // initiated | processing | completed | failed
    private Timestamp initiatedAt;
    private Timestamp completedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReturnId() { return returnId; }
    public void setReturnId(int returnId) { this.returnId = returnId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getInitiatedAt() { return initiatedAt; }
    public void setInitiatedAt(Timestamp initiatedAt) { this.initiatedAt = initiatedAt; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
}
