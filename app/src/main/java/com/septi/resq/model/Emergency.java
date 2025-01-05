package com.septi.resq.model;

public class Emergency {
    private long id;
    private double latitude;
    private double longitude;
    private String type;
    private String description;
    private String timestamp;
    private String photoPath;
    private EmergencyStatus status;

    public Emergency(double latitude, double longitude, String type, String description, String timestamp, String photoPath) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
        this.photoPath = photoPath;
        this.status = EmergencyStatus.MENUNGGU; // Default status
    }
    // Add getters and setters for status
    public EmergencyStatus getStatus() {
        return status;
    }

    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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