package com.keycapstore.dto;

public class Voucher {

    private int voucherId;
    private String code;
    private double discountPercent;

    public Voucher() {}

    public Voucher(int voucherId, String code, double discountPercent) {
        this.voucherId = voucherId;
        this.code = code;
        this.discountPercent = discountPercent;
    }

    public int getVoucherId() {
        return voucherId;
    }

    public String getCode() {
        return code;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }
}