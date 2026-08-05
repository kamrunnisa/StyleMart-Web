package com.stylemart.model;

import java.sql.Timestamp;

/** A row from `returns`, optionally hydrated with its {@link Refund} once one exists. */
public class ReturnRequest {
    private int id;
    private int orderId;
    private int userId;
    private String reason;
    private String comment;
    private String status; // requested | pickup_scheduled | picked_up | refund_initiated | refund_completed | rejected
    private Timestamp requestedAt;
    private Timestamp updatedAt;
    private Refund refund;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Timestamp requestedAt) { this.requestedAt = requestedAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Refund getRefund() { return refund; }
    public void setRefund(Refund refund) { this.refund = refund; }
}
