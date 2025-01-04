package com.septi.resq.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class LocationUtils {
    private static final String PERMISSION_REQUESTED_KEY = "location_permission_requested";

    public static final int PERMISSION_REQUEST_CODE = 1;

    // Check if location permissions are granted
    public static boolean hasLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    // Check and request location permissions
    public static void checkLocationPermission(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("permissions", Context.MODE_PRIVATE);
        boolean wasRequested = prefs.getBoolean(PERMISSION_REQUESTED_KEY, false);

        if (hasLocationPermission(activity)) {
            if (activity instanceof LocationPermissionCallback) {
                ((LocationPermissionCallback) activity).onLocationPermissionGranted();
            }
        } else if (wasRequested && isPermissionDeniedPermanently(activity)) {
            showSettingsDialog(activity, "Izin Lokasi Diperlukan",
                    "Izinkan aplikasi untuk mengakses lokasi Anda di pengaturan.");
        } else {
            prefs.edit().putBoolean(PERMISSION_REQUESTED_KEY, true).apply();
            requestLocationPermissions(activity);
        }
    }


    public interface LocationPermissionCallback {
        void onLocationPermissionGranted();
    }


    // Check if the user denied permissions permanently
    public static boolean isPermissionDeniedPermanently(Activity activity) {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) &&
                !hasLocationPermission(activity);
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
                Toast.makeText(context, "Izin Lokasi Diperlukan untuk mendapatkan Lokasi saat Ini", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Silahkan Izinkan Lokasi", Toast.LENGTH_SHORT).show();
        }

        return location;
    }

    // Request location permissions from the user
    public static void requestLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    // Show dialog to direct the user to app settings
    public static void showSettingsDialog(Activity activity, String title, String message) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Buka Pengaturan", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}


