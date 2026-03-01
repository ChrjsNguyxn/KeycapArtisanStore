package com.keycapstore.model;

public class ProductImageDTO {

    private int imageId;
    private int productId;
    private String imageUrl;
    private boolean isThumbnail;

    public ProductImageDTO() {}

    public ProductImageDTO(int imageId, int productId,
                           String imageUrl, boolean isThumbnail) {
        this.imageId = imageId;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
    }

    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isThumbnail() { return isThumbnail; }
    public void setThumbnail(boolean thumbnail) { isThumbnail = thumbnail; }
}