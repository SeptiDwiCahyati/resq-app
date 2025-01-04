package com.septi.resq.utils;

import android.content.Context;

import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class RouteCalculator {
    private static final String OSRM_API_URL = "https://router.project-osrm.org/route/v1/driving/";

    public interface RouteCalculationCallback {
        void onRouteCalculated(List<GeoPoint> route);
        void onError(Exception e);
    }

    public static void calculateRoute(Context context, GeoPoint start, GeoPoint end, RouteCalculationCallback callback) {
        String url = OSRM_API_URL +
                start.getLongitude() + "," + start.getLatitude() + ";" +
                end.getLongitude() + "," + end.getLatitude() +
                "?overview=full&geometries=polyline";

        new Thread(() -> {
            try {
                URL routeUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) routeUrl.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String geometry = jsonResponse.getJSONArray("routes").getJSONObject(0).getString("geometry");

                List<GeoPoint> route = PolylineUtils.decodePolyline(geometry);
                if (callback != null) {
                    callback.onRouteCalculated(route);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).start();
    }
}
