package com.septi.resq.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.resq.R;
import com.septi.resq.fragment.report.ReportDetailActivity;
import com.septi.resq.model.Emergency;
import com.septi.resq.utils.GeocodingHelper;

import java.util.ArrayList;
import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.EmergencyViewHolder> {
    private final Context context;
    private List<Emergency> emergencies;
    private List<Emergency> allEmergencies;

    public EmergencyAdapter(List<Emergency> emergencies, Context context) {
        this.emergencies = new ArrayList<>(emergencies);
        this.allEmergencies = new ArrayList<>(emergencies);
        this.context = context;
    }

    @NonNull
    @Override
    public EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency, parent, false);
        return new EmergencyViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull EmergencyViewHolder holder, int position) {
        Emergency emergency = emergencies.get(position);
        holder.typeTextView.setText(emergency.getType());
        holder.descriptionTextView.setText(emergency.getDescription());
        holder.timestampTextView.setText(emergency.getTimestamp());
        holder.locationTextView.setText(String.format("Location: %.6f, %.6f", emergency.getLatitude(), emergency.getLongitude()));

        GeocodingHelper.getAddressFromLocation(context, emergency.getLatitude(), emergency.getLongitude(), new GeocodingHelper.GeocodingCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onAddressReceived(String address) {
                holder.locationTextView.setText("Location: " + address);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onError(Exception e) {
                holder.locationTextView.setText("Location: Unknown");
            }
        });

        // Add OnClickListener to the entire CardView
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailActivity.class);
            intent.putExtra("emergencyId", emergency.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return emergencies.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Emergency> newEmergencies) {
        this.emergencies = new ArrayList<>(newEmergencies);
        this.allEmergencies = new ArrayList<>(newEmergencies);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
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

    static class EmergencyViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView;
        TextView descriptionTextView;
        TextView timestampTextView;
        TextView locationTextView;

        EmergencyViewHolder(View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.emergencyTypeTextView);
            descriptionTextView = itemView.findViewById(R.id.emergencyDescriptionTextView);
            timestampTextView = itemView.findViewById(R.id.emergencyTimestampTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
        }
    }
}