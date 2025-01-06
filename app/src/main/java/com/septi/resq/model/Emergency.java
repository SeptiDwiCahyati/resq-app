
package com.septi.resq.model;

public class Emergency {
    public enum EmergencyStatus {
        MENUNGGU,
        PROSES,
        SELESAI
    }

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
        this.status = EmergencyStatus.MENUNGGU;
    }

    public Emergency(double latitude, double longitude, String type, String description, String timestamp) {
        this(latitude, longitude, type, description, timestamp, null);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public EmergencyStatus getStatus() {
        return status;
    }

    public void setStatus(EmergencyStatus status) {
        this.status = status;
    }
}