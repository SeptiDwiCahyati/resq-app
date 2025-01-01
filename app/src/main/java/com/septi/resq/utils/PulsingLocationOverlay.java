package com.septi.resq.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.SystemClock;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class PulsingLocationOverlay extends org.osmdroid.views.overlay.Overlay {
    private final GeoPoint location;
    private final MapView mapView;
    private final Paint paint;
    private final int maxRadius;
    private long startTime;
    private boolean isAnimating = true;

    public PulsingLocationOverlay( Context context, MapView mapView, GeoPoint location) {
        super(context);
        this.mapView = mapView;
        this.location = location;
        this.maxRadius = 100; // Maximum radius in pixels

        paint = new Paint();
        paint.setColor(Color.parseColor("#294285F4")); // Semi-transparent blue
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        startTime = SystemClock.uptimeMillis();
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (shadow) return;
        if (!isAnimating) return;

        Point point = new Point();
        mapView.getProjection().toPixels(location, point);

        long elapsed = SystemClock.uptimeMillis() - startTime;
        // Animation duration in milliseconds
        int duration = 1500;
        float progress = (elapsed % duration) / (float) duration;

        // Calculate current radius using sine function for smooth animation
        float radius = maxRadius * (float) Math.abs(Math.sin(progress * Math.PI));

        // Update paint alpha based on radius
        paint.setAlpha((int) (80 * (1 - radius / maxRadius)));

        // Center the pulse circle on the marker
        canvas.drawCircle(point.x, point.y, radius, paint);

        // Request next frame
        mapView.postInvalidate();
    }

    public void startAnimation() {
        isAnimating = true;
        startTime = SystemClock.uptimeMillis();
        mapView.invalidate();
    }

    public void stopAnimation() {
        isAnimating = false;
        mapView.invalidate();
    }

    public void updateLocation(GeoPoint newLocation) {
        location.setCoords(newLocation.getLatitude(), newLocation.getLongitude());
        mapView.invalidate();
    }
}