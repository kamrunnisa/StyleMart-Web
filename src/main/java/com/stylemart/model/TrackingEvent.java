package com.stylemart.model;

import java.sql.Timestamp;

/** One row from `tracking_history` -- a single stage reached by an order, with its timestamp. */
public class TrackingEvent {
    private int id;
    private int orderId;
    private String stage; // placed | confirmed | packed | shipped | out_for_delivery | delivered
    private String note;
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
