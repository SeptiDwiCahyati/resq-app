package com.septi.resq;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.model.Emergency;

public class EmergencyDetailActivity extends AppCompatActivity {
    private TextView typeTextView, descriptionTextView, locationTextView, timestampTextView;
    private ImageView imageView;
    private Button btnEdit, btnDelete;
    private EmergencyDBHelper dbHelper;
    private Emergency currentEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_detail);

        typeTextView = findViewById(R.id.typeTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        locationTextView = findViewById(R.id.locationTextView);
        timestampTextView = findViewById(R.id.timestampTextView);
        imageView = findViewById(R.id.imageView);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        long emergencyId = getIntent().getLongExtra("emergencyId", -1L);

        if (emergencyId == -1L) {
            Toast.makeText(this, "Invalid emergency ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        dbHelper = new EmergencyDBHelper(this);
        currentEmergency = dbHelper.getEmergencyById((int) emergencyId);

        if (currentEmergency != null) {
            populateViews();
        } else {
            Toast.makeText(this, "Emergency not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnEdit.setOnClickListener(v -> showEditDialog());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void populateViews() {
        typeTextView.setText(currentEmergency.getType());
        descriptionTextView.setText(currentEmergency.getDescription());
        locationTextView.setText(String.format("Location: %.6f, %.6f", currentEmergency.getLatitude(), currentEmergency.getLongitude()));
        timestampTextView.setText(currentEmergency.getTimestamp());

        if (currentEmergency.getPhotoPath() != null && !currentEmergency.getPhotoPath().isEmpty()) {
            Glide.with(this)
                    .load(currentEmergency.getPhotoPath())
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_emergency, null);

        EditText etType = dialogView.findViewById(R.id.etType);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        etType.setText(currentEmergency.getType());
        etDescription.setText(currentEmergency.getDescription());

        builder.setView(dialogView)
                .setTitle("Edit Emergency")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newType = etType.getText().toString().trim();
                    String newDescription = etDescription.getText().toString().trim();

                    if (!newType.isEmpty()) {
                        currentEmergency.setType(newType);
                        currentEmergency.setDescription(newDescription);
                        dbHelper.updateEmergency(currentEmergency);
                        populateViews();
                        Toast.makeText(this, "Emergency updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Type cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Emergency")
                .setMessage("Are you sure you want to delete this emergency?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.deleteEmergency(currentEmergency.getId());
                    Toast.makeText(this, "Emergency deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
