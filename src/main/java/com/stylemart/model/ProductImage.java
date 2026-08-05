package com.stylemart.model;

/** One row from product_images -- just enough to display a thumbnail and delete it. */
public class ProductImage {
    private int id;
    private String imageUrl;

    public ProductImage() {}

    public ProductImage(int id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
