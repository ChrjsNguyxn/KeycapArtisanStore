package com.keycapstore.dto;

import java.time.LocalDateTime;

public class Order {

    private int orderId;
    private int employeeId;
    private int customerId;
    private double totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public Order() {
    }

    public Order(int employeeId, int customerId, double totalAmount, String status) {
        this.employeeId = employeeId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public Order(int orderId, int employeeId, int customerId, double totalAmount, String status,
            LocalDateTime createdAt) {
        this.orderId = orderId;
        this.employeeId = employeeId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
