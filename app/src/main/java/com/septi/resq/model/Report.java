package com.septi.resq.model;

public class Report {
    private long id;
    private String title;
    private String location;
    private String timestamp;
    private double latitude;
    private double longitude;
    private Emergency.EmergencyStatus status; // Change to use EmergencyStatus enum


    public Report(String title, String location, String timestamp, double latitude, double longitude, Emergency.EmergencyStatus status) {
        this.title = title;
        this.location = location;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    // Update getter and setter for status
    public Emergency.EmergencyStatus getStatus() {
        return status;
    }

    public void setStatus(Emergency.EmergencyStatus status) {
        this.status = status;
    }
    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}