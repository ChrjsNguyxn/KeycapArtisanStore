package com.keycapstore.model;

import java.util.Date;

public class ShippingOrder {
    private int invoiceId;
    private String customerName;
    private String phone;
    private String address;
    private double totalAmount;
    private String status;
    private String shippingMethod;
    private Date createdAt;
    private String trackingNumber;

    public ShippingOrder(int invoiceId, String customerName, String phone, String address, double totalAmount,
            String status, String shippingMethod, Date createdAt, String trackingNumber) {
        this.invoiceId = invoiceId;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.totalAmount = totalAmount;
        this.status = status;
        this.shippingMethod = shippingMethod;
        this.createdAt = createdAt;
        this.trackingNumber = trackingNumber;
    }

    // Getters
    public int getInvoiceId() {
        return invoiceId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}