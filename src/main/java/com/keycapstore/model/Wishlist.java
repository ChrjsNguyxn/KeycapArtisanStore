package com.keycapstore.model;

import java.time.LocalDateTime;


public class Wishlist {

    private int           wishlistId;
    private int           customerId;
    private int           productId;
    private LocalDateTime createdAt;

    
    public Wishlist() {}

    public Wishlist(int wishlistId, int customerId, int productId, LocalDateTime createdAt) {
        this.wishlistId = wishlistId;
        this.customerId = customerId;
        this.productId  = productId;
        this.createdAt  = createdAt;
    }

    /** Dùng khi thêm sản phẩm vào wishlist */
    public Wishlist(int customerId, int productId) {
        this.customerId = customerId;
        this.productId  = productId;
        this.createdAt  = LocalDateTime.now();
    }

    public int getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(int wishlistId) {
        this.wishlistId = wishlistId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    
}