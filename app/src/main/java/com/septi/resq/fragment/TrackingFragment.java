package com.septi.resq.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.septi.resq.R;
import com.septi.resq.database.RescueTeamDBHelper;
import com.septi.resq.model.Emergency;
import com.septi.resq.model.RescueTeam;
import com.septi.resq.utils.EmergencyMarkerUtils;
import com.septi.resq.utils.MarkerUtils;
import com.septi.resq.utils.RouteCalculator;
import com.septi.resq.viewmodel.EmergencyViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TrackingFragment extends Fragment {
    private MapView map;
    private static final int MARKER_SIZE_DP = 40;

    private final Map<Long, Marker> rescueTeamMarkers = new HashMap<>();
    private final Map<Long, List<GeoPoint>> rescueTeamRoutes = new HashMap<>();
    private final Map<Long, Integer> rescueTeamRouteIndexes = new HashMap<>();
    private final Map<Long, Boolean> rescueTeamMovingStatus = new HashMap<>();
    private final Map<Long, Polyline> rescueTeamRouteLines = new HashMap<>();

    private final List<Marker> emergencyMarkers = new ArrayList<>();

    private static final float SPEED = 80.0f;
    private final Handler animationHandler = new Handler();
    private RescueTeamDBHelper dbHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        dbHelper = new RescueTeamDBHelper(ctx);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking, container, false);
        initializeMap(view);
        EmergencyViewModel viewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);
        viewModel.getEmergencies().observe(getViewLifecycleOwner(), this::updateAllMarkers);
        viewModel.getNewEmergency().observe(getViewLifecycleOwner(), this::addEmergencyMarker);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Tracking");
        }
        return view;
    }

    private void initializeMap(View view) {
        map = view.findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(0.0530266, 111.4755201));

        // Initialize rescue team markers
        List<RescueTeam> allTeams = dbHelper.getAllTeams(); // Ambil semua tim, termasuk yang sedang bertugas
        for (RescueTeam team : allTeams) {
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(team.getLatitude(), team.getLongitude()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(team.getName());

            Drawable rescueIcon = getResources().getDrawable(R.drawable.ic_ambulance);
            Drawable resizedIcon = MarkerUtils.resizeMarkerIcon(getContext(), rescueIcon, MARKER_SIZE_DP);
            marker.setIcon(resizedIcon);

            // Atur status berdasarkan ketersediaan
            if (!team.isAvailable()) {
                marker.setSnippet("Tidak tersedia / Dalam tugas");
            } else {
                marker.setSnippet("Contact: " + team.getContactNumber());
            }

            marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
                // Tampilkan informasi
                clickedMarker.showInfoWindow();
                return true; // Menghentikan event lebih lanjut
            });

            map.getOverlays().add(marker);
            rescueTeamMarkers.put(team.getId(), marker);
            rescueTeamMovingStatus.put(team.getId(), false);
        }
    }


    private Long findNearestTeam(GeoPoint emergencyLocation) {
        return rescueTeamMarkers.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(rescueTeamMovingStatus.get(entry.getKey())) == false &&
                        rescueTeamMarkers.get(entry.getKey()).getSnippet().contains("Contact:"))
                .min((entry1, entry2) -> Double.compare(
                        calculateDistance(entry1.getValue().getPosition(), emergencyLocation),
                        calculateDistance(entry2.getValue().getPosition(), emergencyLocation)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }




    // Modify your addEmergencyMarker method in TrackingFragment
    private void addEmergencyMarker(Emergency emergency) {
        EmergencyMarkerUtils.addEmergencyMarker(
                getContext(),
                map,
                emergency,
                rescueTeamMarkers,
                rescueTeamMovingStatus,
                emergencyMarkers,
                MARKER_SIZE_DP,
                clickedMarker -> {
                    // This will still handle manual clicks if needed
                    if (emergency.getStatus() == Emergency.EmergencyStatus.MENUNGGU) {
                        Long nearestTeamId = findNearestTeam(clickedMarker.getPosition());
                        if (nearestTeamId != null) {
                            checkAndDispatchRescueTeam(emergency);
                        } else {
                            Toast.makeText(getContext(), "Semua tim sedang dalam tugas!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // Automatically check for dispatch when marker is added
        checkAndDispatchRescueTeam(emergency);
    }

    private void checkAndDispatchRescueTeam(Emergency emergency) {
        if (emergency.getStatus() == Emergency.EmergencyStatus.MENUNGGU) {
            GeoPoint emergencyLocation = new GeoPoint(emergency.getLatitude(), emergency.getLongitude());
            Long nearestTeamId = findNearestTeam(emergencyLocation);

            if (nearestTeamId != null) {
                // Get the team marker
                Marker teamMarker = rescueTeamMarkers.get(nearestTeamId);

                // Stop any existing movement
                stopRescueTeamMovement(nearestTeamId);

                // Calculate and start the route
                calculateRoute(nearestTeamId, teamMarker.getPosition(), emergencyLocation);

                // Update team marker title and snippet
                teamMarker.setTitle(teamMarker.getTitle() + " (Responding)");
                teamMarker.setSnippet("Tidak tersedia / Dalam tugas");

                // Update rescue team availability in database
                RescueTeam team = dbHelper.getTeamById(nearestTeamId);
                if (team != null) {
                    team.setIsAvailable(false);  // Using the new setter
                    dbHelper.updateTeamAvailability(team.getId(), false);
                }

                // Update emergency status
                emergency.setStatus(Emergency.EmergencyStatus.PROSES);
                EmergencyViewModel viewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);
                viewModel.updateEmergency(emergency);

                // Update the map
                map.invalidate();
            }
        }
    }



    private void updateAllMarkers(List<Emergency> emergencies) {
        // Clear existing markers
        for (Marker marker : emergencyMarkers) {
            map.getOverlays().remove(marker);
        }
        emergencyMarkers.clear();

        // Add updated markers
        for (Emergency emergency : emergencies) {
            addEmergencyMarker(emergency);
        }
    }

    private void calculateRoute(Long teamId, GeoPoint start, GeoPoint end) {
        RouteCalculator.calculateRoute(getContext(), start, end, new RouteCalculator.RouteCalculationCallback() {
            @Override
            public void onRouteCalculated(List<GeoPoint> route) {
                rescueTeamRoutes.put(teamId, route);
                rescueTeamRouteIndexes.put(teamId, 0);

                requireActivity().runOnUiThread(() -> {
                    // Clear any existing routes on the map
                    for (Polyline routeLine : rescueTeamRouteLines.values()) {
                        if (routeLine != null) {
                            map.getOverlays().remove(routeLine);
                        }
                    }
                    rescueTeamRouteLines.clear();

                    // Draw new route and start movement
                    drawRoute(teamId, route);
                    startRescueTeamMovement(teamId);
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }


    private void drawRoute(Long teamId, List<GeoPoint> points) {
        Polyline existingRoute = rescueTeamRouteLines.get(teamId);
        if (existingRoute != null) {
            map.getOverlays().remove(existingRoute);
        }

        Polyline routeLine = new Polyline();
        routeLine.setPoints(points);
        routeLine.setColor(Color.BLUE);
        routeLine.setWidth(10f);
        map.getOverlays().add(routeLine);
        rescueTeamRouteLines.put(teamId, routeLine);
        map.invalidate();
    }

    private void startRescueTeamMovement(Long teamId) {
        if (!Boolean.TRUE.equals(rescueTeamMovingStatus.get(teamId)) &&
                rescueTeamRoutes.get(teamId) != null &&
                !rescueTeamRoutes.get(teamId).isEmpty()) {

            rescueTeamMovingStatus.put(teamId, true);
            rescueTeamRouteIndexes.put(teamId, 0);
            moveRescueTeam(teamId);
        }
    }

    private void stopRescueTeamMovement(Long teamId) {
        rescueTeamMovingStatus.put(teamId, false);
        animationHandler.removeCallbacksAndMessages(null);
    }

    private void moveRescueTeam(Long teamId) {
        List<GeoPoint> route = rescueTeamRoutes.get(teamId);
        Integer currentIndex = rescueTeamRouteIndexes.get(teamId);

        if (!Boolean.TRUE.equals(rescueTeamMovingStatus.get(teamId)) ||
                currentIndex >= route.size() - 1) {
            rescueTeamMovingStatus.put(teamId, false);
            return;
        }

        GeoPoint current = route.get(currentIndex);
        GeoPoint next = route.get(currentIndex + 1);

        double distance = calculateDistance(current, next);
        long timeForSegment = (long) ((distance / SPEED) * 3600000);

        Marker marker = rescueTeamMarkers.get(teamId);
        marker.setPosition(current);
        map.invalidate();

        animationHandler.postDelayed(() -> {
            rescueTeamRouteIndexes.put(teamId, currentIndex + 1);
            moveRescueTeam(teamId);
        }, Math.max(timeForSegment, 16));
    }


    private double calculateDistance(GeoPoint p1, GeoPoint p2) {
        double R = 6371;
        double dLat = Math.toRadians(p2.getLatitude() - p1.getLatitude());
        double dLon = Math.toRadians(p2.getLongitude() - p1.getLongitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(p1.getLatitude())) * Math.cos(Math.toRadians(p2.getLatitude())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Long teamId : rescueTeamMarkers.keySet()) {
            stopRescueTeamMovement(teamId);
        }
    }
}