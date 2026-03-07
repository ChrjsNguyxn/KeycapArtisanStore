package com.keycapstore.dto;

public class OrderItem {

    private int orderId; // associated order when persisted
    private int productId;
    private String productName;
    private double price;
    private int quantity;

    public OrderItem() {
        // no-args constructor
    }

    public OrderItem(int productId, String productName, double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotal() {
        return price * quantity;
    }

    public double getSubtotal() {
        return price * quantity;
    }

    public double getUnitPrice() {
        return price;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSubtotal(int quantity) {
        // subtotal is computed from price * quantity; setter updates quantity instead
        this.quantity = quantity;
    }
}
