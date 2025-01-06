package com.septi.resq;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


public class SelectLocationActivity extends AppCompatActivity {
    private MapView mapView;
    private Marker locationMarker;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Location");
        }

        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);

        mapView = findViewById(R.id.map_view);
        setupMap();

        Button btnConfirm = findViewById(R.id.btnConfirmLocation);
        btnConfirm.setOnClickListener(v -> confirmLocation());
    }

    private void setupMap() {
        mapView.setMultiTouchControls(true);
        GeoPoint startPoint = new GeoPoint(latitude, longitude);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(startPoint);
        locationMarker = new Marker(mapView);
        locationMarker.setPosition(startPoint);
        locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(locationMarker);
        mapView.getOverlays().add(new org.osmdroid.views.overlay.MapEventsOverlay(
                new org.osmdroid.events.MapEventsReceiver() {
                    @Override
                    public boolean singleTapConfirmedHelper(GeoPoint p) {
                        updateMarkerPosition(p);
                        return true;
                    }

                    @Override
                    public boolean longPressHelper(GeoPoint p) {
                        return false;
                    }
                }));
    }

    private void updateMarkerPosition(GeoPoint point) {
        latitude = point.getLatitude();
        longitude = point.getLongitude();
        locationMarker.setPosition(point);
        mapView.invalidate();
    }

    private void confirmLocation() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", latitude);
        resultIntent.putExtra("longitude", longitude);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}