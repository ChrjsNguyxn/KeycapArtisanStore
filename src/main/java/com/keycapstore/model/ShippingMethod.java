package com.keycapstore.model;

public class ShippingMethod {
    private int id;
    private String name;
    private double price;

    public ShippingMethod(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    @Override
    public String toString() {
        return this.name; // Để tí hiện lên Dropdown (ComboBox) cho đẹp
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}