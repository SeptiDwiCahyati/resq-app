package com.septi.resq.model;

public class Emergency {
    private long id;
    private double latitude;
    private double longitude;
    private String type;
    private String description;
    private String timestamp;

    public Emergency( double latitude, double longitude, String type, String description, String timestamp ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId( long id ) {
        this.id = id;
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
}