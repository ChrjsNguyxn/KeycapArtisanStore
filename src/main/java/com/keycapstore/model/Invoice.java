package com.keycapstore.model;

import java.util.Date;

public class Invoice {
    private int id;
    private String empName;
    private String customerName;
    private String customerPhone;
    private Date createdAt;
    private double totalAmount;

    public Invoice() {
    }

    public Invoice(int id, String empName, String customerName, String customerPhone, Date createdAt,
            double totalAmount) {
        this.id = id;
        this.empName = empName;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
    }

    public int getId() {
        return id;
    }

    public String getEmpName() {
        return empName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}