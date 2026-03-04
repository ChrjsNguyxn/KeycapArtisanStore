package com.keycapstore.model;

public class ImportOrderItemDTO {

    private int importItemId;
    private int importId;
    private int productId;
    private int quantity;
    private double importPrice;

    public ImportOrderItemDTO(){}

    public int getImportItemId() { return importItemId; }
    public void setImportItemId(int importItemId) { this.importItemId = importItemId; }

    public int getImportId() { return importId; }
    public void setImportId(int importId) { this.importId = importId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getImportPrice() { return importPrice; }
    public void setImportPrice(double importPrice) { this.importPrice = importPrice; }
}