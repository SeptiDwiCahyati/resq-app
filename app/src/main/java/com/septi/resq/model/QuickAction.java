package com.septi.resq.model;

public class QuickAction {
    private final String title;
    private final int iconResource;

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