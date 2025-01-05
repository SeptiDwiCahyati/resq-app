package com.septi.resq.model;

public class RescueTeam {
    private long id;
    private String name;
    private double latitude;
    private double longitude;
    private String contactNumber;
    private Double distance;
    private boolean isAvailable;

    public RescueTeam(long id, String name, double latitude, double longitude,
                      String contactNumber, Double distance, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNumber = contactNumber;
        this.distance = distance;
        this.isAvailable = isAvailable;
    }

    // Getters and setters
    public long getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getContactNumber() { return contactNumber; }
    public Double getDistance() { return distance; }
    public boolean isAvailable() { return isAvailable; }
    public String getStatus() { return isAvailable ? "Tersedia" : "Dalam Tugas"; }

    public void setDistance(Double distance) { this.distance = distance; }

    public void setIsAvailable(boolean available) {
        this.isAvailable = available;
    }
}