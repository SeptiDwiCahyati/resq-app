package com.septi.rescuu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.septi.rescuu.model.Emergency;
import com.septi.rescuu.database.EmergencyDBHelper;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MapFragment extends Fragment {
    private MapView mapView;
    private EmergencyDBHelper dbHelper;
    private Marker selectedLocation;
    private Marker currentLocationMarker;
    private LocationManager locationManager;
    // Constants
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int MARKER_SIZE_DP = 48;
    private static final double DEFAULT_LATITUDE = -6.200000;
    private static final double DEFAULT_LONGITUDE = 106.816666;
    private static final double DEFAULT_ZOOM = 15.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize database helper first
        dbHelper = new EmergencyDBHelper(requireContext());

        // Initialize OpenStreetMap configuration
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize map AFTER inflating the view
        mapView = view.findViewById(R.id.map_view);

        // Configure map base settings
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setMinZoomLevel(4.0);
        mapView.setMaxZoomLevel(19.0);

        // Enable hardware acceleration
        mapView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Set initial map position (default to Jakarta, Indonesia)
        GeoPoint startPoint = new GeoPoint(-6.200000, 106.816666);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(startPoint);

        // Initialize location services
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        // Setup UI controls
        setupUIControls(view);

        // Request permissions
        requestLocationPermissions();

        // Add map click listener
        setupMapClickListener();

        // Load existing markers
        loadExistingMarkers();

        return view;
    }

    private void setupUIControls(View view) {
        FloatingActionButton myLocationButton = view.findViewById(R.id.my_location_button);
        myLocationButton.setOnClickListener(v -> getCurrentLocation());

        MaterialButton reportButton = view.findViewById(R.id.report_button);
        reportButton.setOnClickListener(v -> reportEmergency());
    }

    private void setupMapClickListener() {
        mapView.getOverlays().add(new org.osmdroid.views.overlay.Overlay() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
                org.osmdroid.api.IGeoPoint p = mapView.getProjection().fromPixels(
                        (int) e.getX(), (int) e.getY());

                // Create or move marker
                if (selectedLocation == null) {
                    selectedLocation = new Marker(mapView);
                    mapView.getOverlays().add(selectedLocation);
                }
                selectedLocation.setPosition(new GeoPoint(p.getLatitude(), p.getLongitude()));
                selectedLocation.setAnchor(0.5f, 1.0f);
                mapView.invalidate();

                showReportDialog(p.getLatitude(), p.getLongitude());
                return true;
            }
        });
    }

    private void loadExistingMarkers() {
        for (Emergency emergency : dbHelper.getAllEmergencies()) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(emergency.getLatitude(), emergency.getLongitude()));
            updateMarkerInfo(marker, emergency);
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            mapView.getController().animateTo(currentLocation);

            // Update or add marker
            if (currentLocationMarker == null) {
                currentLocationMarker = new Marker(mapView);
                mapView.getOverlays().add(currentLocationMarker);
            }
            currentLocationMarker.setPosition(currentLocation);
            currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.invalidate();
        }
    }

    private void reportEmergency() {
        if (selectedLocation != null) {
            GeoPoint location = selectedLocation.getPosition();
            showReportDialog(location.getLatitude(), location.getLongitude());
        } else {
            Toast.makeText(requireContext(), "Silakan pilih lokasi kejadian terlebih dahulu",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showReportDialog(final double latitude, final double longitude) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_report_emergency, null);

        Spinner typeSpinner = dialogView.findViewById(R.id.spinner_emergency_type);
        EditText descriptionEdit = dialogView.findViewById(R.id.edit_description);

        // Setup emergency type spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Kecelakaan", "Kebakaran", "Bencana Alam", "Kriminal", "Medis", "Lainnya"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("Laporkan Kejadian Darurat")
                .setView(dialogView)
                .setPositiveButton("Laporkan", (dialog, which) -> {
                    String type = typeSpinner.getSelectedItem().toString();
                    String description = descriptionEdit.getText().toString();
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()).format(new Date());

                    Emergency emergency = new Emergency(
                            latitude,
                            longitude,
                            type,
                            description,
                            timestamp
                    );

                    long id = dbHelper.insertEmergency(emergency);
                    if (id > 0) {
                        updateMarkerInfo(selectedLocation, emergency);
                        Toast.makeText(requireContext(), "Kejadian berhasil dilaporkan",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }


    // Helper method untuk resize marker icon
    private Drawable resizeMarkerIcon(Drawable icon) {
        float density = getResources().getDisplayMetrics().density;
        int pixelSize = (int) (MARKER_SIZE_DP * density);

        Bitmap bitmap = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        icon.draw(canvas);

        return new BitmapDrawable(getResources(), bitmap);
    }

    private void updateMarkerInfo(Marker marker, Emergency emergency) {
        marker.setTitle(emergency.getType());
        marker.setSnippet("Waktu: " + emergency.getTimestamp() + "\n" +
                "Deskripsi: " + emergency.getDescription());

        // Set fixed anchor points
        marker.setAnchor(0.5f, 1.0f);
        marker.setDraggable(false);

        // Set and resize icon
        int iconDrawable;
        switch (emergency.getType()) {
            case "Kecelakaan":
                iconDrawable = R.drawable.ic_accident;
                break;
            case "Kebakaran":
                iconDrawable = R.drawable.ic_fire;
                break;
            case "Bencana Alam":
                iconDrawable = R.drawable.ic_disaster;
                break;
            case "Kriminal":
                iconDrawable = R.drawable.ic_police;
                break;
            case "Medis":
                iconDrawable = R.drawable.ic_emergency;
                break;
            default:
                iconDrawable = R.drawable.ic_fire;
                break;
        }

        Drawable icon = getResources().getDrawable(iconDrawable);
        Drawable resizedIcon = resizeMarkerIcon(icon);
        marker.setIcon(resizedIcon);

        // Set info window
        marker.setInfoWindow(new CustomInfoWindow(mapView));
    }

    // Custom InfoWindow implementation
    private class CustomInfoWindow extends org.osmdroid.views.overlay.infowindow.InfoWindow {
        public CustomInfoWindow(MapView mapView) {
            super(R.layout.marker_info_window, mapView);
        }

        @Override
        public void onOpen(Object item) {
            Marker marker = (Marker) item;
            View view = getView();
            if (view != null) {
                TextView titleText = view.findViewById(R.id.title);
                TextView snippetText = view.findViewById(R.id.snippet);

                titleText.setText(marker.getTitle());
                snippetText.setText(marker.getSnippet());
            }
        }

        @Override
        public void onClose() {
            // Optional: Cleanup code here if needed
        }
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        loadExistingMarkers();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}