package com.septi.resq.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.resq.R;
import com.septi.resq.adapter.EmergencyStatusCardAdapter;
import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.database.RescueTeamDBHelper;
import com.septi.resq.database.TrackingDBHelper;
import com.septi.resq.model.Emergency;
import com.septi.resq.model.RescueTeam;
import com.septi.resq.model.TrackingStatus;
import com.septi.resq.service.TrackingService;
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

    private EmergencyStatusCardAdapter adapter;
    private RecyclerView recyclerView;
    private static final int MARKER_SIZE_DP = 40;
    private Map<Long, Boolean> isTrackingStarted = new HashMap<>();
    private final Map<Long, Marker> rescueTeamMarkers = new HashMap<>();
    private final Map<Long, List<GeoPoint>> rescueTeamRoutes = new HashMap<>();
    private final Map<Long, Integer> rescueTeamRouteIndexes = new HashMap<>();
    private final Map<Long, Boolean> rescueTeamMovingStatus = new HashMap<>();
    private final Map<Long, Polyline> rescueTeamRouteLines = new HashMap<>();
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 1000; // 1 detik
    private final List<Marker> emergencyMarkers = new ArrayList<>();
    private TrackingDBHelper dbHelperTracking;
    private EmergencyViewModel viewModel;
    private static final float SPEED = 200.0f;
    private final Handler animationHandler = new Handler();
    private RescueTeamDBHelper dbHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE));
        dbHelper = new RescueTeamDBHelper(ctx);
        dbHelperTracking = new TrackingDBHelper(ctx);
        viewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);
        viewModel.init(new EmergencyDBHelper(ctx), dbHelperTracking);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking, container, false);

        initializeMap(view);
        initializeRecyclerView(view);

        viewModel.getEmergencies().observe(getViewLifecycleOwner(), emergencies -> {
            updateAllMarkers(emergencies);
            adapter.updateData(emergencies);
        });

        viewModel.getNewEmergency().observe(getViewLifecycleOwner(), this::addEmergencyMarker);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Tracking");
        }
        return view;
    }

    private void initializeRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.emergency_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmergencyStatusCardAdapter(new ArrayList<>(), requireContext(), viewModel);
        recyclerView.setAdapter(adapter);
    }


    private void initializeMap(View view) {
        map = view.findViewById(R.id.map);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(0.0530266, 111.4755201));

        List<RescueTeam> allTeams = dbHelper.getAllTeams();
        for (RescueTeam team : allTeams) {
            Marker marker = new Marker(map);

            TrackingStatus lastStatus = dbHelperTracking.getLastTrackingStatus(team.getId());

            if (lastStatus != null && "COMPLETED".equals(lastStatus.getStatus())) {
                marker.setPosition(new GeoPoint(lastStatus.getCurrentLat(), lastStatus.getCurrentLon()));
            } else {
                marker.setPosition(new GeoPoint(team.getLatitude(), team.getLongitude()));
            }

            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(team.getName());

            Drawable rescueIcon = getResources().getDrawable(R.drawable.ic_ambulance);
            Drawable resizedIcon = MarkerUtils.resizeMarkerIcon(getContext(), rescueIcon, MARKER_SIZE_DP);
            marker.setIcon(resizedIcon);

            if (!team.isAvailable()) {
                if (lastStatus != null && "COMPLETED".equals(lastStatus.getStatus())) {
                    marker.setSnippet("Selesai bertugas di lokasi");
                } else {
                    marker.setSnippet("Tidak tersedia / Dalam tugas");
                }
            } else {
                marker.setSnippet("Contact: " + team.getContactNumber());
            }

            marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
                clickedMarker.showInfoWindow();
                return true;
            });

            map.getOverlays().add(marker);
            rescueTeamMarkers.put(team.getId(), marker);
            rescueTeamMovingStatus.put(team.getId(), false);
        }
    }


    private Long findNearestTeam(GeoPoint emergencyLocation) {
        return rescueTeamMarkers.entrySet().stream()
                .filter(entry -> !Boolean.TRUE.equals(rescueTeamMovingStatus.get(entry.getKey())) &&
                        Objects.requireNonNull(rescueTeamMarkers.get(entry.getKey())).getSnippet().contains("Contact:"))
                .min((entry1, entry2) -> Double.compare(
                        calculateDistance(entry1.getValue().getPosition(), emergencyLocation),
                        calculateDistance(entry2.getValue().getPosition(), emergencyLocation)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }


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

        checkAndDispatchRescueTeam(emergency);
    }

    private void checkAndDispatchRescueTeam(Emergency emergency) {
        if (emergency.getStatus() == Emergency.EmergencyStatus.MENUNGGU) {
            GeoPoint emergencyLocation = new GeoPoint(emergency.getLatitude(), emergency.getLongitude());
            Long nearestTeamId = findNearestTeam(emergencyLocation);

            if (nearestTeamId != null) {
                new Handler().postDelayed(() -> {
                    dispatchRescueTeam(emergency, nearestTeamId, emergencyLocation);
                }, 15000);
            } else {
                Toast.makeText(getContext(), "Semua tim sedang dalam tugas!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void dispatchRescueTeam(Emergency emergency, Long nearestTeamId, GeoPoint emergencyLocation) {
        if (Boolean.TRUE.equals(isTrackingStarted.get(nearestTeamId))) {
            return;
        }
        isTrackingStarted.put(nearestTeamId, true);
        Marker teamMarker = rescueTeamMarkers.get(nearestTeamId);

        Intent serviceIntent = new Intent(getContext(), TrackingService.class);
        requireContext().startForegroundService(serviceIntent);

        TrackingStatus status = new TrackingStatus();
        status.setTeamId(nearestTeamId);
        status.setEmergencyId(emergency.getId());
        status.setStatus("IN_PROGRESS");
        assert teamMarker != null;
        status.setCurrentLat(teamMarker.getPosition().getLatitude());
        status.setCurrentLon(teamMarker.getPosition().getLongitude());
        status.setDestinationLat(emergencyLocation.getLatitude());
        status.setDestinationLon(emergencyLocation.getLongitude());
        status.setRouteIndex(0);

        dbHelperTracking.insertTracking(status);
        stopRescueTeamMovement(nearestTeamId);
        calculateRoute(nearestTeamId, teamMarker.getPosition(), emergencyLocation);

        teamMarker.setTitle(teamMarker.getTitle() + " (Responding)");
        teamMarker.setSnippet("Tidak tersedia / Dalam tugas");

        RescueTeam team = dbHelper.getTeamById(nearestTeamId);
        if (team != null) {
            team.setIsAvailable(false);
            dbHelper.updateTeamAvailability(team.getId(), false);
        }

        emergency.setStatus(Emergency.EmergencyStatus.PROSES);
        EmergencyViewModel viewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);
        viewModel.updateEmergency(emergency);

        map.invalidate();
    }

    private void updateAllMarkers(List<Emergency> emergencies) {
        for (Marker marker : emergencyMarkers) {
            map.getOverlays().remove(marker);
        }
        emergencyMarkers.clear();

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

        Integer currentIndex = rescueTeamRouteIndexes.get(teamId);
        if (currentIndex == null) currentIndex = 0;

        List<GeoPoint> remainingPoints = new ArrayList<>(points.subList(currentIndex, points.size()));

        Polyline routeLine = new Polyline();
        routeLine.setPoints(remainingPoints);

        int routeColor = getRouteColor(teamId);
        routeLine.setColor(routeColor);
        routeLine.setWidth(10f);

        map.getOverlays().add(routeLine);
        rescueTeamRouteLines.put(teamId, routeLine);
        map.invalidate();
    }


    private int getRouteColor(Long teamId) {
        int baseColor = Math.abs(teamId.hashCode());
        switch (Math.abs(baseColor % 5)) {
            case 0:
                return Color.BLUE;
            case 1:
                return Color.RED;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.MAGENTA;
            default:
                return Color.CYAN;
        }
    }

    private void startRescueTeamMovement(Long teamId) {
        if (!Boolean.TRUE.equals(rescueTeamMovingStatus.get(teamId)) &&
                rescueTeamRoutes.get(teamId) != null &&
                !Objects.requireNonNull(rescueTeamRoutes.get(teamId)).isEmpty()) {

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
        if (!isValidRescueTeamState(teamId)) return;

        List<GeoPoint> route = rescueTeamRoutes.get(teamId);
        Integer currentIndex = rescueTeamRouteIndexes.get(teamId);

        if (isDestinationReached(teamId, currentIndex, route)) {
            handleCompletion(teamId, route, currentIndex);
            return;
        }

        GeoPoint current = route.get(currentIndex);
        GeoPoint next = route.get(currentIndex + 1);

        updateMarkerPosition(teamId, current);
        updateTrackingStatus(teamId, currentIndex, current, "IN_PROGRESS");

        TrackingStatus tracking = dbHelperTracking.getActiveTracking(teamId);
        if (tracking != null) {
            adapter.updateTrackingStatus(tracking.getEmergencyId(), "IN_PROGRESS");
        }

        drawRoute(teamId, route);
        map.invalidate();

        long timeForSegment = calculateSegmentTime(current, next);

        animationHandler.postDelayed(() -> {
            rescueTeamRouteIndexes.put(teamId, currentIndex + 1);
            moveRescueTeam(teamId);
        }, Math.max(timeForSegment, 16));
    }

    private boolean isValidRescueTeamState(Long teamId) {
        List<GeoPoint> route = rescueTeamRoutes.get(teamId);
        Integer currentIndex = rescueTeamRouteIndexes.get(teamId);

        return route != null && currentIndex != null &&
                Boolean.TRUE.equals(rescueTeamMovingStatus.get(teamId));
    }

    private boolean isDestinationReached(Long teamId, Integer currentIndex, List<GeoPoint> route) {
        return currentIndex >= route.size() - 1;
    }

    private void handleCompletion(Long teamId, List<GeoPoint> route, Integer currentIndex) {
        rescueTeamMovingStatus.put(teamId, false);
        isTrackingStarted.put(teamId, false);
        removeRouteLine(teamId);
        GeoPoint finalPosition = route.get(currentIndex);
        updateTrackingStatus(teamId, currentIndex, finalPosition, "COMPLETED");

        // Replace updateEmergencyStatus call with tracking update via ViewModel
        TrackingStatus currentTracking = dbHelperTracking.getActiveTracking(teamId);
        if (currentTracking != null) {
            viewModel.updateTrackingStatus(currentTracking.getEmergencyId(), "COMPLETED");
        }
    }
    private void updateMarkerPosition(Long teamId, GeoPoint position) {
        Marker marker = rescueTeamMarkers.get(teamId);
        if (marker != null) marker.setPosition(position);
    }

    private void updateTrackingStatus(Long teamId, int currentIndex, GeoPoint currentPosition, String status) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        lastUpdateTime = currentTime;

        TrackingStatus currentTracking = dbHelperTracking.getActiveTracking(teamId);
        if (currentTracking != null) {
            // Update tracking status via ViewModel
            if (!status.equals(currentTracking.getStatus()) ||
                    currentPosition.getLatitude() != currentTracking.getCurrentLat() ||
                    currentPosition.getLongitude() != currentTracking.getCurrentLon()) {

                TrackingStatus updatedStatus = new TrackingStatus();
                updatedStatus.setTeamId(teamId);
                updatedStatus.setEmergencyId(currentTracking.getEmergencyId());
                updatedStatus.setStatus(status);
                updatedStatus.setCurrentLat(currentPosition.getLatitude());
                updatedStatus.setCurrentLon(currentPosition.getLongitude());
                updatedStatus.setDestinationLat(currentTracking.getDestinationLat());
                updatedStatus.setDestinationLon(currentTracking.getDestinationLon());
                updatedStatus.setRouteIndex(currentIndex);

                dbHelperTracking.updateTracking(updatedStatus);
                viewModel.updateTrackingStatus(currentTracking.getEmergencyId(), status);

                // Update marker if status is COMPLETED
                if ("COMPLETED".equals(status)) {
                    Marker marker = rescueTeamMarkers.get(teamId);
                    if (marker != null) {
                        marker.setSnippet("Selesai bertugas di lokasi");
                        map.invalidate();
                    }
                }
            }
        }
    }

    private void removeRouteLine(Long teamId) {
        Polyline existingRoute = rescueTeamRouteLines.get(teamId);
        if (existingRoute != null) {
            map.getOverlays().remove(existingRoute);
            rescueTeamRouteLines.remove(teamId);
            map.invalidate();
        }
    }


    private long calculateSegmentTime(GeoPoint current, GeoPoint next) {
        double distance = calculateDistance(current, next);
        return (long) ((distance / SPEED) * 3600000);
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

        if (dbHelperTracking != null) {
            for (Long teamId : rescueTeamMarkers.keySet()) {
                TrackingStatus activeStatus = dbHelperTracking.getActiveTracking(teamId);
                TrackingStatus lastStatus = dbHelperTracking.getLastTrackingStatus(teamId);

                if (activeStatus != null && !Boolean.TRUE.equals(isTrackingStarted.get(teamId))) {
                    // Hanya mulai tracking jika belum dimulai untuk tim ini
                    GeoPoint currentPos = new GeoPoint(activeStatus.getCurrentLat(), activeStatus.getCurrentLon());
                    GeoPoint destination = new GeoPoint(activeStatus.getDestinationLat(), activeStatus.getDestinationLon());

                    rescueTeamRouteIndexes.put(teamId, activeStatus.getRouteIndex());
                    isTrackingStarted.put(teamId, true); // Set flag bahwa tracking sudah dimulai
                    calculateRoute(teamId, currentPos, destination);
                } else if (lastStatus != null && "COMPLETED".equals(lastStatus.getStatus())) {
                    // Reset tracking flag ketika status completed
                    isTrackingStarted.put(teamId, false);

                    Marker marker = rescueTeamMarkers.get(teamId);
                    if (marker != null) {
                        marker.setPosition(new GeoPoint(lastStatus.getCurrentLat(), lastStatus.getCurrentLon()));
                        marker.setSnippet("Selesai bertugas di lokasi");
                        map.invalidate();
                    }
                }
            }
        }
    }


    // Tambahkan juga reset flag di onPause/onDestroy
    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
        // Reset semua tracking flags
        isTrackingStarted.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Long teamId : rescueTeamMarkers.keySet()) {
            stopRescueTeamMovement(teamId);
            isTrackingStarted.remove(teamId);
        }

        for (Polyline routeLine : rescueTeamRouteLines.values()) {
            if (routeLine != null) {
                map.getOverlays().remove(routeLine);
            }
        }
        rescueTeamRouteLines.clear();
    }

}