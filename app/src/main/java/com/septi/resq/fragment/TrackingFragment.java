package com.septi.resq.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;
import com.septi.resq.R;

public class TrackingFragment extends Fragment {
    private MapView map;
    private static final GeoPoint AMBULANCE_LOCATION = new GeoPoint(0.0530266, 111.4755201);
    private Marker ambulanceMarker;
    private Polyline routeLine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking, container, false);
        initializeMap(view);
        return view;
    }

    private void initializeMap(View view) {
        map = view.findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(AMBULANCE_LOCATION);

        // Add ambulance marker
        ambulanceMarker = new Marker(map);
        ambulanceMarker.setPosition(AMBULANCE_LOCATION);
        ambulanceMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        ambulanceMarker.setTitle("Ambulance");
        map.getOverlays().add(ambulanceMarker);

        // Setup incident markers
        setupIncidentMarkers();
    }

    private void setupIncidentMarkers() {
        // Example random incident locations in Sintang - replace with your database data
        addIncidentMarker(new GeoPoint(0.085235, 111.497224), "Traffic Accident");
        addIncidentMarker(new GeoPoint(0.086874, 111.498516), "Medical Emergency");
    }


    private void addIncidentMarker(GeoPoint position, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(position);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Set click listener for this incident marker
        marker.setOnMarkerClickListener((marker1, mapView) -> {
            drawDirectLine(AMBULANCE_LOCATION, marker1.getPosition());
            return true;
        });

        map.getOverlays().add(marker);
    }

    private void drawDirectLine(GeoPoint start, GeoPoint end) {
        // Clear previous route if exists
        if (routeLine != null) {
            map.getOverlays().remove(routeLine);
        }

        // Draw direct line between points
        routeLine = new Polyline();
        ArrayList<GeoPoint> points = new ArrayList<>();
        points.add(start);
        points.add(end);
        routeLine.setPoints(points);
        routeLine.setColor(Color.BLUE);
        routeLine.setWidth(10f);
        map.getOverlays().add(routeLine);
        map.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}