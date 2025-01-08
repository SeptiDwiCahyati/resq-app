package com.septi.resq.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.Toast;

import com.septi.resq.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocationSearchManager {
    private final Context context;
    private final MapView mapView;
    private Marker searchMarker;
    private final OkHttpClient client;
    private OnSearchResultListener listener;

    public interface OnSearchResultListener {
        void onSearchResults(List<String> suggestions);
    }

    public LocationSearchManager(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        this.client = new OkHttpClient();
    }

    public void setOnSearchResultListener(OnSearchResultListener listener) {
        this.listener = listener;
    }

    public void searchSuggestions(String query) {
        if (query.length() < 3) return; // Wait for at least 3 characters
        new SearchSuggestionsTask().execute(query);
    }

    public void searchLocation(String query) {
        new SearchLocationTask().execute(query);
    }

    public void clearSearch() {
        if (searchMarker != null) {
            mapView.getOverlays().remove(searchMarker);
            searchMarker = null;
            mapView.invalidate();
        }
    }

    private class SearchSuggestionsTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {
            String query = params[0];
            List<String> suggestions = new ArrayList<>();

            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String url = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery +
                        "&format=json&limit=5&addressdetails=1";

                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", context.getPackageName())
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    JSONArray results = new JSONArray(response.body().string());

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String displayName = result.getString("display_name");
                        suggestions.add(displayName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return suggestions;
        }

        @Override
        protected void onPostExecute(List<String> suggestions) {
            if (listener != null) {
                listener.onSearchResults(suggestions);
            }
        }
    }

    private class SearchLocationTask extends AsyncTask<String, Void, GeoPoint> {
        private String locationName;

        @Override
        protected GeoPoint doInBackground(String... params) {
            String query = params[0];

            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String url = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery +
                        "&format=json&limit=1";

                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", context.getPackageName())
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    JSONArray results = new JSONArray(response.body().string());

                    if (results.length() > 0) {
                        JSONObject result = results.getJSONObject(0);
                        double lat = result.getDouble("lat");
                        double lon = result.getDouble("lon");
                        locationName = result.getString("display_name");
                        return new GeoPoint(lat, lon);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(GeoPoint location) {
            if (location != null) {
                clearSearch(); // Remove existing marker

                // Add new marker
                searchMarker = new Marker(mapView);
                searchMarker.setPosition(location);
                searchMarker.setAnchor(0.5f, 1.0f);
                searchMarker.setTitle(locationName);

                // Set marker icon
                Drawable icon = context.getResources().getDrawable(R.drawable.ic_my_location);
                Drawable resizedIcon = MarkerUtils.resizeMarkerIcon(context, icon, 40);
                searchMarker.setIcon(resizedIcon);

                mapView.getOverlays().add(searchMarker);

                // Animate to location
                mapView.getController().animateTo(location);
                mapView.getController().setZoom(16.0);
                mapView.invalidate();

                // Show marker info
                searchMarker.showInfoWindow();
            } else {
                Toast.makeText(context, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        }
    }
}