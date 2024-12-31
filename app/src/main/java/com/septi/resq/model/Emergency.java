package com.septi.resq.model;

public class Emergency {
    private long id;
    private double latitude;
    private double longitude;
    private String type;
    private String description;
    private String timestamp;
    private String photoPath;

    public Emergency(double latitude, double longitude, String type, String description, String timestamp, String photoPath) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
        this.photoPath = photoPath;
    }

    // Add constructor without photo path for backward compatibility
    public Emergency(double latitude, double longitude, String type, String description, String timestamp) {
        this(latitude, longitude, type, description, timestamp, null);
    }

    // Existing getters
    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Add getter and setter for photoPath
    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public void setId(long id) {
        this.id = id;
    }
}