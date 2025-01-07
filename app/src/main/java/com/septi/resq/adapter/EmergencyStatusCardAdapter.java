package com.septi.resq.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.resq.R;
import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.database.RescueTeamDBHelper;
import com.septi.resq.database.TrackingDBHelper;
import com.septi.resq.model.Emergency;
import com.septi.resq.model.RescueTeam;
import com.septi.resq.model.TrackingStatus;
import com.septi.resq.utils.GeocodingHelper;
import com.septi.resq.viewmodel.EmergencyViewModel;

import java.util.ArrayList;
import java.util.List;

public class EmergencyStatusCardAdapter extends RecyclerView.Adapter<EmergencyStatusCardAdapter.EmergencyCardViewHolder> {
    private Context context;
    private List<Emergency> emergencies;
    private List<Emergency> allEmergencies;
    private EmergencyDBHelper emergencyDBHelper;
    private RescueTeamDBHelper rescueTeamDBHelper;
    private TrackingDBHelper trackingDBHelper;
    private EmergencyViewModel viewModel;
    private final Handler handler = new Handler();
    private final Runnable updateTask;

    public EmergencyStatusCardAdapter(List<Emergency> emergencies, Context context, EmergencyViewModel viewModel) {
        this.emergencies = new ArrayList<>(emergencies);
        this.allEmergencies = new ArrayList<>(emergencies);
        this.context = context;
        this.emergencyDBHelper = new EmergencyDBHelper(context);
        this.rescueTeamDBHelper = new RescueTeamDBHelper(context);
        this.trackingDBHelper = new TrackingDBHelper(context);
        this.viewModel = viewModel;

        // Runnable untuk update setiap 5 detik
        updateTask = new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged(); // Refresh RecyclerView
                handler.postDelayed(this, 10000); // Jadwalkan ulang 5 detik kemudian
            }
        };
        handler.postDelayed(updateTask, 5000); // Jadwalkan pertama kali
    }


    @NonNull
    @Override
    public EmergencyCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.emergency_card_layout, parent, false);
        return new EmergencyCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyCardViewHolder holder, int position) {
        Emergency emergency = emergencies.get(position);

        // Set emergency information
        holder.typeTextView.setText(emergency.getType());
        holder.descriptionTextView.setText(emergency.getDescription());
        holder.timestampTextView.setText(emergency.getTimestamp());
        holder.statusTextView.setText(getStatusText(emergency.getStatus()));

        // Show location initially as coordinates
        String locationText = String.format("Location: %.6f, %.6f",
                emergency.getLatitude(), emergency.getLongitude());
        holder.locationTextView.setText(locationText);

        // Get address using GeocodingHelper
        GeocodingHelper.getAddressFromLocation(context,
                emergency.getLatitude(),
                emergency.getLongitude(),
                new GeocodingHelper.GeocodingCallback() {
                    @Override
                    public void onAddressReceived(String address) {
                        holder.locationTextView.setText("Location: " + address);
                    }

                    @Override
                    public void onError(Exception e) {
                        holder.locationTextView.setText("Location: Unknown");
                    }
                });

        // Check for assigned rescue team and display even if status is COMPLETED
        TrackingStatus trackingStatus = trackingDBHelper.getLastTrackingStatus(emergency.getId());
        if (trackingStatus != null) {
            RescueTeam team = rescueTeamDBHelper.getTeamById(trackingStatus.getTeamId());
            if (team != null) {
                holder.rescueTeamInfo.setVisibility(View.VISIBLE);
                holder.teamNameTextView.setText(team.getName());
                holder.teamContactTextView.setText("Contact: " + team.getContactNumber());
                holder.trackingStatusTextView.setText(getTrackingStatusText(trackingStatus.getStatus()));
            } else {
                holder.rescueTeamInfo.setVisibility(View.GONE);
            }
        } else {
            holder.rescueTeamInfo.setVisibility(View.GONE);
        }
    }


    public void updateTrackingStatus(long emergencyId, String trackingStatus) {
        // Simply delegate to ViewModel
        viewModel.updateTrackingStatus(emergencyId, trackingStatus);
    }

    private String getStatusText(Emergency.EmergencyStatus status) {
        switch (status) {
            case MENUNGGU:
                return "Menunggu Penanganan";
            case PROSES:
                return "Sedang Ditangani";
            case SELESAI:
                return "Selesai";
            default:
                return "Status Tidak Diketahui";
        }
    }

    private String getTrackingStatusText(String status) {
        switch (status) {
            case "IN_PROGRESS":
                return "Tim Sedang Menuju Lokasi";
            case "ARRIVED":
                return "Tim Telah Tiba di Lokasi";
            case "COMPLETED":
                return "Penanganan Selesai";
            default:
                return "Status Tidak Diketahui";
        }
    }

    @Override
    public int getItemCount() {
        return emergencies.size();
    }

    public void updateData(List<Emergency> newEmergencies) {
        this.emergencies = new ArrayList<>(newEmergencies);
        this.allEmergencies = new ArrayList<>(newEmergencies);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            emergencies = new ArrayList<>(allEmergencies);
        } else {
            List<Emergency> filteredList = new ArrayList<>();
            for (Emergency emergency : allEmergencies) {
                if (emergency.getType().toLowerCase().contains(query.toLowerCase()) ||
                        emergency.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(emergency);
                }
            }
            emergencies = filteredList;
        }
        notifyDataSetChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        handler.removeCallbacks(updateTask); // Hentikan pembaruan saat RecyclerView dilepas
    }


    static class EmergencyCardViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView;
        TextView descriptionTextView;
        TextView timestampTextView;
        TextView locationTextView;
        TextView statusTextView;
        LinearLayout rescueTeamInfo;
        TextView teamNameTextView;
        TextView teamContactTextView;
        TextView trackingStatusTextView;

        EmergencyCardViewHolder(View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.emergency_type);
            descriptionTextView = itemView.findViewById(R.id.emergency_description);
            timestampTextView = itemView.findViewById(R.id.emergency_timestamp);
            locationTextView = itemView.findViewById(R.id.emergency_location);
            statusTextView = itemView.findViewById(R.id.emergency_status);
            rescueTeamInfo = itemView.findViewById(R.id.rescue_team_info);
            teamNameTextView = itemView.findViewById(R.id.team_name);
            teamContactTextView = itemView.findViewById(R.id.team_contact);
            trackingStatusTextView = itemView.findViewById(R.id.tracking_status);
        }
    }


}