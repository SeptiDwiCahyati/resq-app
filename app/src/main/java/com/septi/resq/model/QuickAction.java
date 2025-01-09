package com.septi.resq.model;

public class QuickAction {
    private final String title;
    private final int iconResource;
    private final String phoneNumber;

    public QuickAction(String title, int iconResource, String phoneNumber) {
        this.title = title;
        this.iconResource = iconResource;
        this.phoneNumber = phoneNumber;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResource() {
        return iconResource;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}