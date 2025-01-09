package com.septi.resq;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.septi.resq.model.RescueTeam;
import com.septi.resq.model.TrackingStatus;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class CustomTeamInfoWindow extends InfoWindow {
    private final RescueTeam team;
    private final TrackingStatus lastStatus;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable closeRunnable;

    public CustomTeamInfoWindow(int layoutResId, MapView mapView, RescueTeam team, TrackingStatus lastStatus) {
        super(layoutResId, mapView);
        this.team = team;
        this.lastStatus = lastStatus;
        this.closeRunnable = this::close;

    }

    @Override
    public void onOpen(Object item) {
        View view = getView();

        TextView teamNameText = view.findViewById(R.id.tv_team_name);
        TextView teamStatusText = view.findViewById(R.id.tv_status);
        TextView teamContactText = view.findViewById(R.id.tv_contact);

        teamNameText.setText(team.getName());

        String statusText;
        if (!team.isAvailable()) {
            if (lastStatus != null && "COMPLETED".equals(lastStatus.getStatus())) {
                statusText = "Selesai bertugas di lokasi";
            } else {
                statusText = "Tidak tersedia / Dalam tugas";
            }
        } else {
            statusText = "Available";
        }
        teamStatusText.setText(statusText);
        teamContactText.setText("Contact: " + team.getContactNumber());
        view.setOnClickListener(v -> close());
        handler.postDelayed(closeRunnable, 10000);
    }

    @Override
    public void onClose() {
        handler.removeCallbacks(closeRunnable);
    }
}