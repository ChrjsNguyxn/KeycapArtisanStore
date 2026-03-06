package com.keycapstore.model;

import java.util.Date;

public class ImportOrderDTO {

    private int importId;
    private int supplierId;
    private int employeeId;
    private double totalCost;
    private Date importDate;
    private String note;

    public ImportOrderDTO() {
    }

    public ImportOrderDTO(int supplierId, int employeeId, double totalCost, String note) {
        this.supplierId = supplierId;
        this.employeeId = employeeId;
        this.totalCost = totalCost;
        this.note = note;
    }


    public int getImportId() {
        return importId;
    }

    public void setImportId(int importId) {
        this.importId = importId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}