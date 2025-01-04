package com.septi.resq.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.septi.resq.R;
import com.septi.resq.model.Emergency;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.Map;

public class EmergencyMarkerUtils {
    public static void addEmergencyMarker(
            Context context,
            MapView map,
            Emergency emergency,
            Map<Long, Marker> rescueTeamMarkers,
            Map<Long, Boolean> rescueTeamMovingStatus,
            List<Marker> emergencyMarkers,
            int markerSize,
            MarkerActionListener actionListener
    ) {
        GeoPoint emergencyPosition = new GeoPoint(emergency.getLatitude(), emergency.getLongitude());
        Marker marker = new Marker(map);
        marker.setPosition(emergencyPosition);
        marker.setTitle(emergency.getType());
        marker.setSnippet(emergency.getDescription());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        int iconDrawable = getIconDrawable(emergency.getType());
        Drawable icon = context.getResources().getDrawable(iconDrawable);
        Drawable resizedIcon = MarkerUtils.resizeMarkerIcon(context, icon, markerSize);
        marker.setIcon(resizedIcon);

        marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
            actionListener.onMarkerClicked(clickedMarker);
            return true;
        });

        map.getOverlays().add(marker);
        emergencyMarkers.add(marker);
        map.invalidate();
    }

    private static int getIconDrawable(String emergencyType) {
        switch (emergencyType) {
            case "Kecelakaan":
                return R.drawable.ic_accident;
            case "Kebakaran":
                return R.drawable.ic_fire;
            case "Bencana Alam":
                return R.drawable.ic_disaster;
            case "Kriminal":
                return R.drawable.ic_police;
            case "Medis":
                return R.drawable.ic_emergency;
            default:
                return R.drawable.error_image;
        }
    }

    public interface MarkerActionListener {
        void onMarkerClicked(Marker marker);
    }
}
