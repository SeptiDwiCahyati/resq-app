package com.septi.resq.model;

public class Report {
    private final String title;
    private final String location;
    private final String timestamp;

    public Report( String title, String location, String timestamp ) {
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