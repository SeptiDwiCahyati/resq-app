package com.septi.resq.model;

public class Report {
    private String title;
    private String location;
    private String timestamp;

    public Report(String title, String location, String timestamp) {
        this.title = title;
        this.location = location;
        this.timestamp = timestamp;
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
}