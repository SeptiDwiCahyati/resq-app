package com.septi.rescuu.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import com.septi.rescuu.R;
import com.septi.rescuu.model.Emergency;
import com.septi.rescuu.database.EmergencyDBHelper;
import com.septi.rescuu.utils.PulsingLocationOverlay;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment {
    private MapView mapView;
    private EmergencyDBHelper dbHelper;
    private Marker selectedLocation;
    private Marker currentLocationMarker;
    private List<Marker> emergencyMarkers;
    private GeoPoint lastKnownLocation;
    private PulsingLocationOverlay pulsingOverlay;
    private LocationManager locationManager;
    // Constants
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int MARKER_SIZE_DP = 40;
    private static final double DEFAULT_LATITUDE = -6.200000;
    private static final double DEFAULT_LONGITUDE = 106.816666;
    private static final double DEFAULT_ZOOM = 15.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize database helper first
        dbHelper = new EmergencyDBHelper(requireContext());
        emergencyMarkers = new ArrayList<>();
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

        // Set initial map position to default location (Jakarta)
        GeoPoint startPoint = new GeoPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        mapView.getController().setZoom(DEFAULT_ZOOM);
        mapView.getController().setCenter(startPoint);

        // Initialize location services
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        // Setup UI controls
        setupUIControls(view);

        // Request permissions and fetch current location
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestLocationPermissions();
        }

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

                // Remove previous selected location marker if it exists
                if (selectedLocation != null) {
                    mapView.getOverlays().remove(selectedLocation);
                }

                // Create new marker
                selectedLocation = new Marker(mapView);
                selectedLocation.setPosition(new GeoPoint(p.getLatitude(), p.getLongitude()));
                selectedLocation.setAnchor(0.5f, 1.0f);

                // Add to map overlays
                mapView.getOverlays().add(selectedLocation);
                mapView.invalidate();

                showReportDialog(p.getLatitude(), p.getLongitude());
                return true;
            }
        });
    }

    private void loadExistingMarkers() {
        // Clear existing emergency markers from both list and map
        for (Marker marker : emergencyMarkers) {
            mapView.getOverlays().remove(marker);
        }
        emergencyMarkers.clear();

        // Load markers from database
        for (Emergency emergency : dbHelper.getAllEmergencies()) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(emergency.getLatitude(), emergency.getLongitude()));
            updateMarkerInfo(marker, emergency);

            // Add to both map and our list
            mapView.getOverlays().add(marker);
            emergencyMarkers.add(marker);
        }
        mapView.invalidate();
    }

    private void reportEmergency() {
        if (lastKnownLocation != null) {
            showReportDialog(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        } else if (selectedLocation != null) {
            GeoPoint location = selectedLocation.getPosition();
            showReportDialog(location.getLatitude(), location.getLongitude());
        } else {
            Toast.makeText(requireContext(), "Silakan pilih lokasi kejadian terlebih dahulu",
                    Toast.LENGTH_SHORT).show();
        }
    }



    private void getCurrentLocation() {
        // First check if permissions are granted
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestLocationPermissions();
            return;
        }

        // Check if GPS is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(requireContext(), "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        // Show loading indicator
        Toast.makeText(requireContext(), "Mencari lokasi...", Toast.LENGTH_SHORT).show();

        // Try to get location from GPS first
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // If GPS location is null, try network provider
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {
            updateCurrentLocation(location);
        } else {
            try {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                        new android.location.LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                updateCurrentLocation(location);
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {}

                            @Override
                            public void onProviderEnabled(String provider) {}

                            @Override
                            public void onProviderDisabled(String provider) {}
                        }, null);
            } catch (SecurityException e) {
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateCurrentLocation(Location location) {
        GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        lastKnownLocation = currentLocation;

        mapView.getController().animateTo(currentLocation);
        mapView.getController().setZoom(18.0);

        if (currentLocationMarker == null) {
            currentLocationMarker = new Marker(mapView);
            mapView.getOverlays().add(currentLocationMarker);
        }

        // Set marker position and properties
        currentLocationMarker.setPosition(currentLocation);
        currentLocationMarker.setAnchor(0.5f, 0.5f);  // Changed to center anchor
        currentLocationMarker.setTitle("Lokasi Anda");

        Drawable icon = getResources().getDrawable(R.drawable.ic_location);
// Gunakan ukuran lebih kecil, misalnya 30dp
        currentLocationMarker.setIcon(resizeMarkerIcon(icon, 20));


        // Update or create pulsing overlay
        if (pulsingOverlay == null) {
            pulsingOverlay = new PulsingLocationOverlay(requireContext(), mapView, currentLocation);
            mapView.getOverlays().add(0, pulsingOverlay); // Add at index 0 to draw below marker
        } else {
            pulsingOverlay.updateLocation(currentLocation);
        }
        pulsingOverlay.startAnimation();

        mapView.invalidate();

        Toast.makeText(requireContext(), "Lokasi ditemukan!", Toast.LENGTH_SHORT).show();
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
                        // Remove temporary selected location marker
                        if (selectedLocation != null) {
                            mapView.getOverlays().remove(selectedLocation);
                            selectedLocation = null;
                        }

                        // Create and add permanent marker
                        Marker newMarker = new Marker(mapView);
                        newMarker.setPosition(new GeoPoint(latitude, longitude));
                        updateMarkerInfo(newMarker, emergency);

                        mapView.getOverlays().add(newMarker);
                        emergencyMarkers.add(newMarker);

                        mapView.invalidate();

                        Toast.makeText(requireContext(), "Kejadian berhasil dilaporkan",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", (dialog, which) -> {
                    // Remove temporary marker on cancel
                    if (selectedLocation != null) {
                        mapView.getOverlays().remove(selectedLocation);
                        selectedLocation = null;
                        mapView.invalidate();
                    }
                })
                .show();
    }


    // Helper method untuk resize marker icon
    private Drawable resizeMarkerIcon(Drawable icon, int sizeDp) {
        float density = getResources().getDisplayMetrics().density;
        int pixelSize = (int) (sizeDp * density);

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
// Gunakan ukuran default, misalnya MARKER_SIZE_DP
        Drawable resizedIcon = resizeMarkerIcon(icon, MARKER_SIZE_DP);
        marker.setIcon(resizedIcon);

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
    public void onPause() {
        super.onPause();
        if (pulsingOverlay != null) {
            pulsingOverlay.stopAnimation();
        }
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pulsingOverlay != null) {
            pulsingOverlay.startAnimation();
        }
        mapView.onResume();
        loadExistingMarkers();
    }
}