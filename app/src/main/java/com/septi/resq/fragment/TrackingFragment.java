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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.septi.resq.R;
import com.septi.resq.database.RescueTeamDBHelper;
import com.septi.resq.model.Emergency;
import com.septi.resq.model.RescueTeam;
import com.septi.resq.utils.MarkerUtils;
import com.septi.resq.viewmodel.EmergencyViewModel;

import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private static final String OSRM_API_URL = "https://router.project-osrm.org/route/v1/driving/";
    private static final float SPEED = 80.0f;
    private final Handler animationHandler = new Handler();
    private RescueTeamDBHelper dbHelper;


    private Marker ambulanceMarker;


    private Polyline routeLine;

    private List<GeoPoint> currentRoute;
    private int currentRouteIndex = 0;
    private boolean isMoving = false;

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
        List<RescueTeam> availableTeams = dbHelper.getAvailableTeams();
        for (RescueTeam team : availableTeams) {
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(team.getLatitude(), team.getLongitude()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(team.getName());
            marker.setSnippet("Contact: " + team.getContactNumber());

            Drawable rescueIcon = getResources().getDrawable(R.drawable.ic_ambulance);
            Drawable resizedIcon = MarkerUtils.resizeMarkerIcon(getContext(), rescueIcon, MARKER_SIZE_DP);
            marker.setIcon(resizedIcon);

            map.getOverlays().add(marker);
            rescueTeamMarkers.put(team.getId(), marker);
            rescueTeamMovingStatus.put(team.getId(), false);
        }
    }

    private Long findNearestTeam(GeoPoint emergencyLocation) {
        Long nearestTeamId = null;
        double shortestDistance = Double.MAX_VALUE;

        for (Map.Entry<Long, Marker> entry : rescueTeamMarkers.entrySet()) {
            Long teamId = entry.getKey();
            Marker teamMarker = entry.getValue();
            double distance = calculateDistance(teamMarker.getPosition(), emergencyLocation);

            if (distance < shortestDistance && Boolean.FALSE.equals(rescueTeamMovingStatus.get(teamId))) {
                shortestDistance = distance;
                nearestTeamId = teamId;
            }
        }

        return nearestTeamId;
    }


    private void addEmergencyMarker(Emergency emergency) {
        GeoPoint emergencyPosition = new GeoPoint(emergency.getLatitude(), emergency.getLongitude());
        Marker marker = new Marker(map);
        marker.setPosition(emergencyPosition);
        marker.setTitle(emergency.getType());
        marker.setSnippet(emergency.getDescription());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Set emergency icon based on type
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
                iconDrawable = R.drawable.error_image;
                break;
        }

        Drawable icon = getResources().getDrawable(iconDrawable);
        Drawable resizedIcon = MarkerUtils.resizeMarkerIcon(getContext(), icon, MARKER_SIZE_DP);
        marker.setIcon(resizedIcon);

        marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
            // Find the nearest available rescue team
            Long nearestTeamId = findNearestTeam(clickedMarker.getPosition());

            if (nearestTeamId != null) {
                // Stop any existing movement of this team
                stopRescueTeamMovement(nearestTeamId);

                // Calculate route for nearest team
                Marker teamMarker = rescueTeamMarkers.get(nearestTeamId);
                calculateRoute(nearestTeamId, teamMarker.getPosition(), clickedMarker.getPosition());

                // Update marker title to show it's responding
                teamMarker.setTitle(teamMarker.getTitle() + " (Responding)");
                map.invalidate();
            }
            return true;
        });

        map.getOverlays().add(marker);
        emergencyMarkers.add(marker);
        map.invalidate();
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
        String url = OSRM_API_URL +
                start.getLongitude() + "," + start.getLatitude() + ";" +
                end.getLongitude() + "," + end.getLatitude() +
                "?overview=full&geometries=polyline";

        new Thread(() -> {
            try {
                URL routeUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) routeUrl.openConnection();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String geometry = jsonResponse.getJSONArray("routes")
                        .getJSONObject(0)
                        .getString("geometry");

                List<GeoPoint> route = decodePolyline(geometry);
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

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void drawRoute(List<GeoPoint> points) {
        if (routeLine != null) {
            map.getOverlays().remove(routeLine);
        }

        routeLine = new Polyline();
        routeLine.setPoints(points);
        routeLine.setColor(Color.BLUE);
        routeLine.setWidth(10f);
        map.getOverlays().add(routeLine);
        map.invalidate();
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

    private List<GeoPoint> decodePolyline(String encoded) {
        List<GeoPoint> points = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encoded.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encoded.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            points.add(new GeoPoint(lat * 1e-5, lng * 1e-5));
        }
        return points;
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