package com.keycapstore.model;

import java.sql.Timestamp;

public class ProductDTO {

    private int productId;
    private int categoryId;
    private int makerId;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private String profile;
    private String material;
    private boolean isActive;
    private Timestamp createdAt;

    
    private String categoryName;
    private String makerName;

    public ProductDTO() {}

    public ProductDTO(int productId, int categoryId, int makerId, String name,
                      String description, double price, int stockQuantity,
                      String profile, String material, boolean isActive,
                      Timestamp createdAt) {

        this.productId = productId;
        this.categoryId = categoryId;
        this.makerId = makerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.profile = profile;
        this.material = material;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
    @Override
public String toString() {
    return productId + " - " + name;
}

   

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getMakerId() { return makerId; }
    public void setMakerId(int makerId) { this.makerId = makerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getMakerName() { return makerName; }
    public void setMakerName(String makerName) { this.makerName = makerName; }
}