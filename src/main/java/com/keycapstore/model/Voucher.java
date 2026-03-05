package com.keycapstore.model;

import java.util.Date;

public class Voucher {
    private int id;
    private String code;
    private double discountPercent;
    private int quantity;
    private Date startDate;
    private Date expiredDate;

    public Voucher() {
    }

    public Voucher(int id, String code, double discountPercent, int quantity) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.quantity = quantity;
    }

    public Voucher(int id, String code, double discountPercent, int quantity, Date startDate, Date expiredDate) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.quantity = quantity;
        this.startDate = startDate;
        this.expiredDate = expiredDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(Date expiredDate) {
        this.expiredDate = expiredDate;
    }
}