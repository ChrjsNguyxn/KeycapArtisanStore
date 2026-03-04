package com.keycapstore.dto;
import java.util.Date;
public class Voucher {
    private int voucherId;
    private String code;
    private double discountPercent;
    private double maxDiscount;
    private Date startDate;
    private Date endDate;
    private boolean isActive;

    public Voucher() {}

    public Voucher(int voucherId, String code,
                   double discountPercent, double maxDiscount,
                   Date startDate, Date endDate,
                   boolean isActive) {
        this.voucherId = voucherId;
        this.code = code;
        this.discountPercent = discountPercent;
        this.maxDiscount = maxDiscount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public int getVoucherId() { return voucherId; }
    public void setVoucherId(int voucherId) { this.voucherId = voucherId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }

    public double getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(double maxDiscount) { this.maxDiscount = maxDiscount; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}