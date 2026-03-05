package com.keycapstore.model;

import java.util.Date;

public class StockEntryRecord {
    private int entryId;
    private String productName;
    private String employeeName;
    private int quantityAdded;
    private double entryPrice;
    private Date entryDate;
    private String note;

    public StockEntryRecord(int entryId, String productName, String employeeName, int quantityAdded, double entryPrice,
            Date entryDate, String note) {
        this.entryId = entryId;
        this.productName = productName;
        this.employeeName = employeeName;
        this.quantityAdded = quantityAdded;
        this.entryPrice = entryPrice;
        this.entryDate = entryDate;
        this.note = note;
    }

    public int getEntryId() {
        return entryId;
    }

    public String getProductName() {
        return productName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public int getQuantityAdded() {
        return quantityAdded;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public String getNote() {
        return note;
    }
}