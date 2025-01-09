package com.septi.resq.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class GeocodingHelper {
    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/reverse";

    public interface GeocodingCallback {
        void onAddressReceived(String address);
        void onError(Exception e);
    }

    public static void getAddressFromLocation(Context context, double lat, double lon, GeocodingCallback callback) {
        new Thread(() -> {
            try {
                String urlString = String.format(Locale.US,
                        "%s?format=json&lat=%f&lon=%f&zoom=18&addressdetails=1",
                        NOMINATIM_API, lat, lon);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "ResQ-App");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                String address = json.getString("display_name");

                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onAddressReceived(address));

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError(e));
            }
        }).start();
    }
}