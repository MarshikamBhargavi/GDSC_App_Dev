package com.example.gallery;


public class DataClass {
    private String imageUrl;

    // Empty constructor needed for Firebase
    public DataClass() {}

    public DataClass(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
