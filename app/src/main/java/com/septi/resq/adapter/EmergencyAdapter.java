package com.septi.resq.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.septi.resq.R;
import com.septi.resq.model.Emergency;

import java.util.ArrayList;
import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.EmergencyViewHolder> {
    private List<Emergency> emergencies;
    private List<Emergency> allEmergencies; // Untuk menyimpan data asli

    public EmergencyAdapter(List<Emergency> emergencies) {
        this.emergencies = new ArrayList<>(emergencies);
        this.allEmergencies = new ArrayList<>(emergencies);
    }

    @NonNull
    @Override
    public EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency, parent, false);
        return new EmergencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyViewHolder holder, int position) {
        Emergency emergency = emergencies.get(position);
        holder.typeTextView.setText(emergency.getType());
        holder.descriptionTextView.setText(emergency.getDescription());
        holder.timestampTextView.setText(emergency.getTimestamp());
        holder.locationTextView.setText(String.format("Location: %.6f, %.6f",
                emergency.getLatitude(), emergency.getLongitude()));

        if (emergency.getPhotoPath() != null && !emergency.getPhotoPath().isEmpty()) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(emergency.getPhotoPath())
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);
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
            emergencies = new ArrayList<>(allEmergencies); // Reset ke data asli jika query kosong
        } else {
            List<Emergency> filteredList = new ArrayList<>();
            for (Emergency emergency : allEmergencies) {
                if (emergency.getType().toLowerCase().contains(query.toLowerCase()) ||
                        emergency.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(emergency); // Tambahkan data yang cocok
                }
            }
            emergencies = filteredList;
        }
        notifyDataSetChanged(); // Update RecyclerView
    }


    static class EmergencyViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView;
        TextView descriptionTextView;
        TextView timestampTextView;
        TextView locationTextView;
        ImageView imageView;

        EmergencyViewHolder(View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.emergencyTypeTextView);
            descriptionTextView = itemView.findViewById(R.id.emergencyDescriptionTextView);
            timestampTextView = itemView.findViewById(R.id.emergencyTimestampTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            imageView = itemView.findViewById(R.id.emergencyImageView);
        }
    }
}
