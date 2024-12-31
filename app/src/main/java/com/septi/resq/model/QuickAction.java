package com.septi.resq.model;

public class QuickAction {
    private String title;
    private int iconResource;

    public QuickAction( String title, int iconResource ) {
        this.title = title;
        this.iconResource = iconResource;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResource() {
        return iconResource;
    }
}