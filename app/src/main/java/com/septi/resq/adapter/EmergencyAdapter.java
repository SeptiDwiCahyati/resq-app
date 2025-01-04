package com.septi.resq.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.septi.resq.R;
import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.model.Emergency;

import java.util.ArrayList;
import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.EmergencyViewHolder> {
    private Context context;
    private EmergencyDBHelper dbHelper;
    private List<Emergency> emergencies;
    private List<Emergency> allEmergencies; // Untuk menyimpan data asli

    // Update constructor
    public EmergencyAdapter(List<Emergency> emergencies, Context context) {
        this.emergencies = new ArrayList<>(emergencies);
        this.allEmergencies = new ArrayList<>(emergencies);
        this.context = context;
        this.dbHelper = new EmergencyDBHelper(context);
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
        holder.btnEdit.setOnClickListener(v -> showEditDialog(emergency));
        holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(emergency));

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

    private void showEditDialog(Emergency emergency) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_emergency, null);

        EditText etType = dialogView.findViewById(R.id.etType);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        etType.setText(emergency.getType());
        etDescription.setText(emergency.getDescription());

        builder.setView(dialogView)
                .setTitle("Edit Emergency")
                .setPositiveButton("Save", (dialog, which) -> {
                    emergency.setType(etType.getText().toString());
                    emergency.setDescription(etDescription.getText().toString());

                    if (dbHelper.updateEmergency(emergency)) {
                        updateData(dbHelper.getAllEmergencies());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation(Emergency emergency) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Emergency")
                .setMessage("Are you sure you want to delete this emergency?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (dbHelper.deleteEmergency(emergency.getId())) {
                        updateData(dbHelper.getAllEmergencies());
                    }
                })
                .setNegativeButton("No", null)
                .show();
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

        Button btnEdit;
        Button btnDelete;

        EmergencyViewHolder(View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.emergencyTypeTextView);
            descriptionTextView = itemView.findViewById(R.id.emergencyDescriptionTextView);
            timestampTextView = itemView.findViewById(R.id.emergencyTimestampTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            imageView = itemView.findViewById(R.id.emergencyImageView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
