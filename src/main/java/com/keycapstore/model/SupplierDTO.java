package com.keycapstore.model;

public class SupplierDTO {

    private int supplierId;
    private String name;
    private String phone;
    private String address;
    private String email;

    public SupplierDTO() {}

    public SupplierDTO(int supplierId, String name, String phone, String address, String email) {
        this.supplierId = supplierId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.email = email;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return name;
    }
}