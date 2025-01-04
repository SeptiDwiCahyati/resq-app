package com.septi.resq.fragment.report;

import android.annotation.SuppressLint;
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
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.septi.resq.OverviewActivity;
import com.septi.resq.R;
import com.septi.resq.SelectLocationActivity;
import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.model.Emergency;
import com.septi.resq.viewmodel.EmergencyViewModel;

public class ReportDetailActivity extends AppCompatActivity {
    private TextView typeTextView, descriptionTextView, locationTextView, timestampTextView;
    private ImageView imageView;
    private EmergencyDBHelper dbHelper;
    private Emergency currentEmergency;
    private static final int LOCATION_REQUEST_CODE = 1001;
    private EmergencyViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        typeTextView = findViewById(R.id.typeTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        locationTextView = findViewById(R.id.locationTextView);
        timestampTextView = findViewById(R.id.timestampTextView);
        imageView = findViewById(R.id.imageView);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnViewOnMap = findViewById(R.id.btnViewOnMap);
        viewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        viewModel.init(new EmergencyDBHelper(this));

        long emergencyId = getIntent().getLongExtra("emergencyId", -1L);

        if (emergencyId == -1L) {
            Toast.makeText(this, "Invalid emergency ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Setup toolbar
        // Remove the cast to AppCompatActivity and use 'this' instead
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Report Detail");
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
        btnViewOnMap.setOnClickListener(v -> navigateToMap());
    }

    @SuppressLint("DefaultLocale")
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
        Button btnEditLocation = dialogView.findViewById(R.id.btnEditLocation);

        etType.setText(currentEmergency.getType());
        etDescription.setText(currentEmergency.getDescription());

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Edit Emergency")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        btnEditLocation.setOnClickListener(v -> {
            dialog.dismiss();
            openLocationSelection();
        });

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String newType = etType.getText().toString().trim();
                String newDescription = etDescription.getText().toString().trim();

                if (!newType.isEmpty()) {
                    currentEmergency.setType(newType);
                    currentEmergency.setDescription(newDescription);
                    viewModel.updateEmergency(currentEmergency);
                    populateViews();
                    Toast.makeText(ReportDetailActivity.this, "Emergency updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ReportDetailActivity.this, "Type cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }


    private void navigateToMap() {
        Intent intent = new Intent(this, OverviewActivity.class);
        intent.putExtra("navigateToMap", true);
        intent.putExtra("emergencyId", currentEmergency.getId());
        intent.putExtra("latitude", currentEmergency.getLatitude());
        intent.putExtra("longitude", currentEmergency.getLongitude());
        startActivity(intent);
        finish();
    }
    private void openLocationSelection() {
        Intent intent = new Intent(this, SelectLocationActivity.class);
        intent.putExtra("latitude", currentEmergency.getLatitude());
        intent.putExtra("longitude", currentEmergency.getLongitude());
        startActivityForResult(intent, LOCATION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);

            currentEmergency.setLatitude(latitude);
            currentEmergency.setLongitude(longitude);
            dbHelper.updateEmergency(currentEmergency);
            populateViews();
            Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Emergency")
                .setMessage("Are you sure you want to delete this emergency?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    viewModel.deleteEmergency(currentEmergency.getId());
                    Toast.makeText(this, "Emergency deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // This will handle the back button press
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}