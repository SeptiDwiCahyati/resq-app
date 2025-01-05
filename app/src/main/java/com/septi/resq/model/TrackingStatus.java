package com.septi.resq.model;

public class TrackingStatus {
    private long id;
    private long teamId;
    private long emergencyId;
    private String status;
    private double currentLat;
    private double currentLon;
    private double destinationLat;
    private double destinationLon;
    private int routeIndex;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getEmergencyId() {
        return emergencyId;
    }

    public void setEmergencyId(long emergencyId) {
        this.emergencyId = emergencyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public double getCurrentLon() {
        return currentLon;
    }

    public void setCurrentLon(double currentLon) {
        this.currentLon = currentLon;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLon() {
        return destinationLon;
    }

    public void setDestinationLon(double destinationLon) {
        this.destinationLon = destinationLon;
    }

    public int getRouteIndex() {
        return routeIndex;
    }

    public void setRouteIndex(int routeIndex) {
        this.routeIndex = routeIndex;
    }
}