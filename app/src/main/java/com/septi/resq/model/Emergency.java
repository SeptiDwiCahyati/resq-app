package com.septi.resq.model;

public class Emergency {
    private long id;
    private final double latitude;
    private final double longitude;
    private String type;
    private String description;
    private final String timestamp;
    private String photoPath;

    public Emergency(double latitude, double longitude, String type, String description, String timestamp, String photoPath) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
        this.photoPath = photoPath;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Emergency(double latitude, double longitude, String type, String description, String timestamp) {
        this(latitude, longitude, type, description, timestamp, null);
    }

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

    public String getPhotoPath() {
        return photoPath;
    }


    public void setId(long id) {
        this.id = id;
    }
}