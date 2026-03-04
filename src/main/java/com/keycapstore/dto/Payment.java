package com.keycapstore.dto;

import java.time.LocalDateTime;

public class Payment {

    private int paymentId;
    private int orderId;
    private double amount;
    private String paymentMethod; // CASH, CARD, TRANSFER
    private String paymentStatus; // PAID, FAILED
    private LocalDateTime paymentDate;

    public Payment() {}
    public Payment(int orderId, double amount, String paymentMethod, String paymentStatus) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    // getters & setters
}