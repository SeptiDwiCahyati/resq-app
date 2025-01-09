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

import com.septi.resq.CustomTeamInfoWindow;
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
    private static final int MARKER_SIZE_DP = 40;
    private final Map<Long, Boolean> isTrackingStarted = new HashMap<>();
    private final Map<Long, Marker> rescueTeamMarkers = new HashMap<>();
    private final Map<Long, List<GeoPoint>> rescueTeamRoutes = new HashMap<>();
    private final Map<Long, Integer> rescueTeamRouteIndexes = new HashMap<>();
    private final Map<Long, Boolean> rescueTeamMovingStatus = new HashMap<>();
    private final Map<Long, Polyline> rescueTeamRouteLines = new HashMap<>();
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 1000;
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
            List<Emergency> filteredEmergencies = new ArrayList<>();
            for (Emergency emergency : emergencies) {
                if (emergency.getStatus() != Emergency.EmergencyStatus.SELESAI) {
                    filteredEmergencies.add(emergency);
                }
            }
            updateAllMarkers(filteredEmergencies);
            adapter.updateData(filteredEmergencies);
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

    public void showTeamInfo(RescueTeam team) {
        if (map != null && rescueTeamMarkers.containsKey(team.getId())) {
            Marker teamMarker = rescueTeamMarkers.get(team.getId());
            if (teamMarker != null) {
                TrackingStatus lastStatus = dbHelperTracking.getLastTrackingStatus(team.getId());
                CustomTeamInfoWindow infoWindow = new CustomTeamInfoWindow(
                        R.layout.team_info_window,
                        map,
                        team,
                        lastStatus
                );

                teamMarker.setInfoWindow(infoWindow);
                infoWindow.open(teamMarker, teamMarker.getPosition(), 0, -teamMarker.getIcon().getIntrinsicHeight());
                map.getController().animateTo(teamMarker.getPosition());
            }
        }
    }

    private void initializeRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.emergency_recycler_view);
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
            TrackingStatus lastStatus = dbHelperTracking.getLastTrackingStatus(team.getId());
            GeoPoint position;
            String snippet;

            if (lastStatus != null && "COMPLETED".equals(lastStatus.getStatus())) {
                position = new GeoPoint(lastStatus.getCurrentLat(), lastStatus.getCurrentLon());
            } else {
                position = new GeoPoint(team.getLatitude(), team.getLongitude());
            }

            if (!team.isAvailable()) {
                snippet = lastStatus != null && "COMPLETED".equals(lastStatus.getStatus())
                        ? "Selesai bertugas di lokasi"
                        : "Tidak tersedia / Dalam tugas";
            } else {
                snippet = "Contact: " + team.getContactNumber();
            }

            Marker marker = createTeamMarker(team, position, snippet, lastStatus);
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

        if (route == null) throw new AssertionError();
        if (isDestinationReached(currentIndex, route)) {
            TrackingStatus currentTracking = dbHelperTracking.getActiveTracking(teamId);
            if (currentTracking != null && "RETURNING".equals(currentTracking.getStatus())) {
                handleReturnCompletion(teamId, currentIndex);
            } else {
                handleArrival(teamId, route, currentIndex);
            }
            return;
        }

        GeoPoint current = route.get(currentIndex);
        GeoPoint next = route.get(currentIndex + 1);
        updateMarkerPosition(teamId, current);
        TrackingStatus tracking = dbHelperTracking.getActiveTracking(teamId);
        if (tracking != null) {
            String currentStatus = tracking.getStatus();
            if (!"RETURNING".equals(currentStatus) && !"ARRIVED".equals(currentStatus)) {
                adapter.updateTrackingStatus(tracking.getEmergencyId(), "IN_PROGRESS");
                updateTrackingStatus(teamId, currentIndex, current, "IN_PROGRESS");
            } else {
                updateTrackingStatus(teamId, currentIndex, current, currentStatus);
            }
        }

        drawRoute(teamId, route);
        map.invalidate();

        long timeForSegment = calculateSegmentTime(current, next);

        animationHandler.postDelayed(() -> {
            rescueTeamRouteIndexes.put(teamId, currentIndex + 1);
            moveRescueTeam(teamId);
        }, Math.max(timeForSegment, 16));
    }

    private void handleArrival(Long teamId, List<GeoPoint> route, Integer currentIndex) {
        GeoPoint finalPosition = route.get(currentIndex);
        updateMarkerPosition(teamId, finalPosition);
        updateTrackingStatus(teamId, currentIndex, finalPosition, "ARRIVED");

        TrackingStatus tracking = dbHelperTracking.getActiveTracking(teamId);
        if (tracking != null) {
            adapter.updateTrackingStatus(tracking.getEmergencyId(), "ARRIVED");

            Marker marker = rescueTeamMarkers.get(teamId);
            if (marker != null) {
                marker.setSnippet("Telah tiba di lokasi");
                map.invalidate();
            }

            new Handler().postDelayed(() -> startReturningToBase(teamId, route, currentIndex, tracking.getEmergencyId()), 15000);
        }
    }

    private void startReturningToBase(Long teamId, List<GeoPoint> route, Integer currentIndex, Long emergencyId) {
        rescueTeamMovingStatus.put(teamId, false);
        removeRouteLine(teamId);

        RescueTeam team = dbHelper.getTeamById(teamId);
        if (team != null) {
            Marker currentMarker = rescueTeamMarkers.get(teamId);
            if (currentMarker != null) {
                map.getOverlays().remove(currentMarker);
            }

            GeoPoint currentLocation = route.get(currentIndex);
            GeoPoint baseLocation = new GeoPoint(team.getLatitude(), team.getLongitude());

            TrackingStatus currentStatus = dbHelperTracking.getActiveTracking(teamId);
            if (currentStatus != null && "ARRIVED".equals(currentStatus.getStatus())) {
                TrackingStatus returnStatus = new TrackingStatus();
                returnStatus.setTeamId(teamId);
                returnStatus.setEmergencyId(emergencyId);
                returnStatus.setStatus("RETURNING");
                returnStatus.setCurrentLat(currentLocation.getLatitude());
                returnStatus.setCurrentLon(currentLocation.getLongitude());
                returnStatus.setDestinationLat(team.getLatitude());
                returnStatus.setDestinationLon(team.getLongitude());
                returnStatus.setRouteIndex(0);

                dbHelperTracking.insertTracking(returnStatus);
                adapter.updateTrackingStatus(emergencyId, "RETURNING");

                // Remove emergency marker when team starts returning
                EmergencyDBHelper emergencyDBHelper = new EmergencyDBHelper(requireContext());
                Emergency emergency = emergencyDBHelper.getEmergencyById(emergencyId);
                if (emergency != null) {
                    emergency.setStatus(Emergency.EmergencyStatus.SELESAI);
                    viewModel.updateEmergency(emergency);

                    for (Marker marker : emergencyMarkers) {
                        if (marker.getId().equals("emergency_" + emergency.getId())) {
                            map.getOverlays().remove(marker);
                            emergencyMarkers.remove(marker);
                            break;
                        }
                    }
                }
            }

            RouteCalculator.calculateRoute(getContext(), currentLocation, baseLocation, new RouteCalculator.RouteCalculationCallback() {
                @Override
                public void onRouteCalculated(List<GeoPoint> returnRoute) {
                    rescueTeamRoutes.put(teamId, returnRoute);
                    rescueTeamRouteIndexes.put(teamId, 0);
                    rescueTeamMovingStatus.put(teamId, true);

                    requireActivity().runOnUiThread(() -> {
                        Marker newMarker = createTeamMarker(
                                team,
                                currentLocation,
                                "Kembali ke POS",
                                dbHelperTracking.getLastTrackingStatus(teamId)
                        );

                        rescueTeamMarkers.put(teamId, newMarker);
                        drawRoute(teamId, returnRoute);
                        moveRescueTeam(teamId);

                        Toast.makeText(getContext(),
                                team.getName() + " mulai kembali ke POS",
                                Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(),
                            "Error calculating return route: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean isValidRescueTeamState(Long teamId) {
        List<GeoPoint> route = rescueTeamRoutes.get(teamId);
        Integer currentIndex = rescueTeamRouteIndexes.get(teamId);

        return route != null && currentIndex != null &&
                Boolean.TRUE.equals(rescueTeamMovingStatus.get(teamId));
    }

    private boolean isDestinationReached(Integer currentIndex, List<GeoPoint> route) {
        return currentIndex >= route.size() - 1;
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

                if (!"RETURNING".equals(status) && currentTracking.getEmergencyId() != -1) {
                    viewModel.updateTrackingStatus(currentTracking.getEmergencyId(), status);
                }

                Marker marker = rescueTeamMarkers.get(teamId);
                if (marker != null) {
                    switch (status) {
                        case "ARRIVED":
                            marker.setSnippet("Telah tiba di lokasi");
                            break;
                        case "COMPLETED":
                            marker.setSnippet("Selesai bertugas di lokasi");
                            break;
                        case "RETURNING":
                            marker.setSnippet("Kembali ke POS");
                            break;
                        case "AVAILABLE":
                            RescueTeam team = dbHelper.getTeamById(teamId);
                            if (team != null) {
                                marker.setSnippet("Contact: " + team.getContactNumber());
                            }
                            break;
                    }
                    map.invalidate();
                }
            }
        }
    }

    private void handleReturnCompletion(Long teamId, Integer currentIndex) {
        rescueTeamMovingStatus.put(teamId, false);
        isTrackingStarted.put(teamId, false);
        removeRouteLine(teamId);

        RescueTeam team = dbHelper.getTeamById(teamId);
        if (team != null) {
            final GeoPoint baseLocation = new GeoPoint(team.getLatitude(), team.getLongitude());

            Marker oldMarker = rescueTeamMarkers.get(teamId);
            if (oldMarker != null) {
                map.getOverlays().remove(oldMarker);
            }

            TrackingStatus activeTracking = dbHelperTracking.getActiveTracking(teamId);
            if (activeTracking != null) {
                TrackingStatus completedStatus = new TrackingStatus();
                completedStatus.setTeamId(teamId);
                completedStatus.setEmergencyId(activeTracking.getEmergencyId());
                completedStatus.setStatus("COMPLETED");
                completedStatus.setCurrentLat(baseLocation.getLatitude());
                completedStatus.setCurrentLon(baseLocation.getLongitude());
                completedStatus.setDestinationLat(baseLocation.getLatitude());
                completedStatus.setDestinationLon(baseLocation.getLongitude());
                completedStatus.setRouteIndex(currentIndex);

                dbHelperTracking.insertTracking(completedStatus);

                if (activeTracking.getEmergencyId() != -1) {
                    viewModel.updateTrackingStatus(activeTracking.getEmergencyId(), "COMPLETED");
                }

                new Handler().postDelayed(() -> {
                    team.setIsAvailable(true);
                    dbHelper.updateTeamAvailability(team.getId(), true);

                    Marker newMarker = createTeamMarker(
                            team,
                            baseLocation,
                            "Contact: " + team.getContactNumber(),
                            completedStatus
                    );

                    rescueTeamMarkers.put(teamId, newMarker);

                    TrackingStatus availableStatus = new TrackingStatus();
                    availableStatus.setTeamId(teamId);
                    availableStatus.setEmergencyId(-1);
                    availableStatus.setStatus("AVAILABLE");
                    availableStatus.setCurrentLat(baseLocation.getLatitude());
                    availableStatus.setCurrentLon(baseLocation.getLongitude());
                    availableStatus.setDestinationLat(baseLocation.getLatitude());
                    availableStatus.setDestinationLon(baseLocation.getLongitude());
                    availableStatus.setRouteIndex(currentIndex);

                    dbHelperTracking.insertTracking(availableStatus);

                    map.invalidate();

                    requireActivity().runOnUiThread(() -> Toast.makeText(
                            getContext(),
                            team.getName() + " telah kembali ke POS dan siap bertugas",
                            Toast.LENGTH_SHORT).show()
                    );
                }, 5000);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();

        if (dbHelperTracking != null) {
            for (Long teamId : rescueTeamMarkers.keySet()) {
                TrackingStatus activeStatus = dbHelperTracking.getActiveTracking(teamId);
                RescueTeam team = dbHelper.getTeamById(teamId);

                if (team != null) {
                    GeoPoint baseLocation = new GeoPoint(team.getLatitude(), team.getLongitude());

                    if (activeStatus != null && !Boolean.TRUE.equals(isTrackingStarted.get(teamId))) {
                        if ("RETURNING".equals(activeStatus.getStatus())) {
                            GeoPoint currentPos = new GeoPoint(activeStatus.getCurrentLat(), activeStatus.getCurrentLon());
                            rescueTeamRouteIndexes.put(teamId, activeStatus.getRouteIndex());
                            isTrackingStarted.put(teamId, true);
                            calculateRoute(teamId, currentPos, baseLocation);
                        } else if ("COMPLETED".equals(activeStatus.getStatus()) || "AVAILABLE".equals(activeStatus.getStatus())) {
                            Marker existingMarker = rescueTeamMarkers.get(teamId);
                            if (existingMarker != null) {
                                map.getOverlays().remove(existingMarker);
                            }

                            Marker newMarker = createTeamMarker(
                                    team,
                                    baseLocation,
                                    "Contact: " + team.getContactNumber(),
                                    activeStatus
                            );
                            rescueTeamMarkers.put(teamId, newMarker);
                        } else {
                            GeoPoint currentPos = new GeoPoint(activeStatus.getCurrentLat(), activeStatus.getCurrentLon());
                            GeoPoint destination = new GeoPoint(activeStatus.getDestinationLat(), activeStatus.getDestinationLon());
                            rescueTeamRouteIndexes.put(teamId, activeStatus.getRouteIndex());
                            isTrackingStarted.put(teamId, true);
                            calculateRoute(teamId, currentPos, destination);
                        }
                    } else if (activeStatus == null) {
                        Marker existingMarker = rescueTeamMarkers.get(teamId);
                        if (existingMarker != null) {
                            map.getOverlays().remove(existingMarker);
                        }

                        Marker newMarker = createTeamMarker(
                                team,
                                baseLocation,
                                "Contact: " + team.getContactNumber(),
                                null
                        );
                        rescueTeamMarkers.put(teamId, newMarker);
                    }
                }
            }
        }
        map.invalidate();
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


    private Marker createTeamMarker(RescueTeam team, GeoPoint position, String snippet, TrackingStatus status) {
        Marker marker = new Marker(map);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(team.getName());
        marker.setSnippet(snippet);
        Drawable rescueIcon = getResources().getDrawable(R.drawable.ic_ambulance);
        Drawable resizedIcon = MarkerUtils.resizeMarkerIcon(getContext(), rescueIcon, MARKER_SIZE_DP);
        marker.setIcon(resizedIcon);

        CustomTeamInfoWindow infoWindow = new CustomTeamInfoWindow(
                R.layout.team_info_window,
                map,
                team,
                status != null ? status : dbHelperTracking.getLastTrackingStatus(team.getId())
        );
        marker.setInfoWindow(infoWindow);

        marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
            infoWindow.open(marker, marker.getPosition(), 0, -marker.getIcon().getIntrinsicHeight());
            return true;
        });

        map.getOverlays().add(marker);

        return marker;
    }


    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
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