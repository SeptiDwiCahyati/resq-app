package com.septi.resq.model;

public class UserProfile {
    private long id;
    private String name;
    private String email;
    private String phone;
    private String photoUri;

    public UserProfile() {
    }

    public UserProfile(long id, String name, String email, String phone, String photoUri) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.photoUri = photoUri;
    }

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPhotoUri() { return photoUri; }

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }
}