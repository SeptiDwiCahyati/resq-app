package com.septi.resq.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class LocationUtils {
    private static final int PERMISSION_REQUEST_CODE = 1;

    // Check if location permissions are granted
    public static boolean hasLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Get the last known location from GPS or network
    public static Location getLastKnownLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;

        if (hasLocationPermission(context)) {
            try {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            } catch (SecurityException e) {
                // Handle the case where location permission is missing at runtime
                Toast.makeText(context, "Location permission is required to get the current location", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show();
        }

        return location;
    }

    // Request location permissions from the user
    public static void requestLocationPermissions(Context context) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(context, "Context must be an Activity", Toast.LENGTH_SHORT).show();
        }
    }
}
