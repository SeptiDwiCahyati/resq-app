package com.septi.resq.model;

public class RescueTeam {
    private String name;
    private String distance;
    private String status;

    public RescueTeam( String name, String distance, String status ) {
        this.name = name;
        this.distance = distance;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    public String getStatus() {
        return status;
    }
}