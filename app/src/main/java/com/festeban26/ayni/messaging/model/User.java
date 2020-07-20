package com.festeban26.ayni.messaging.model;

public class User {
    private String mId;
    private String mUsername;
    private String mImageUrl;

    public User(String id, String username, String imageUrl) {
        this.mId = id;
        this.mUsername = username;
        this.mImageUrl = imageUrl;
    }

    public User() {
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }
}
