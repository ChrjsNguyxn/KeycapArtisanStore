package com.keycapstore.model;

public class CustomerRank {
    private int rankId;
    private String name;
    private double discountPercent;

    public CustomerRank(int rankId, String name, double discountPercent) {
        this.rankId = rankId;
        this.name = name;
        this.discountPercent = discountPercent;
    }

    public int getRankId() {
        return rankId;
    }

    public String getName() {
        return name;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }
}