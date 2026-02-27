package com.keycapstore.model;

import java.time.LocalDateTime;

public class Warranty {

    private int           warrantyId;
    private int           orderItemId;
    private int           customerId;
    private int           employeeId; 
    private String        reason;
    private String        status;        // pending / approved / rejected / completed
    private LocalDateTime requestDate;
    private String        responseNote;  

    public Warranty() {}

    public Warranty(int warrantyId, int orderItemId, int customerId, int employeeId,
                    String reason, String status, LocalDateTime requestDate, String responseNote) {
        this.warrantyId   = warrantyId;
        this.orderItemId  = orderItemId;
        this.customerId   = customerId;
        this.employeeId   = employeeId;
        this.reason       = reason;
        this.status       = status;
        this.requestDate  = requestDate;
        this.responseNote = responseNote;
    }

    /** Dùng khi khách hàng gửi yêu cầu bảo hành mới */
    public Warranty(int orderItemId, int customerId, String reason) {
        this.orderItemId = orderItemId;
        this.customerId  = customerId;
        this.reason      = reason;
        this.status      = "pending";
        this.requestDate = LocalDateTime.now();
    }

    public int getWarrantyId() {
        return warrantyId;
    }

    public void setWarrantyId(int warrantyId) {
        this.warrantyId = warrantyId;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getResponseNote() {
        return responseNote;
    }

    public void setResponseNote(String responseNote) {
        this.responseNote = responseNote;
    }

}