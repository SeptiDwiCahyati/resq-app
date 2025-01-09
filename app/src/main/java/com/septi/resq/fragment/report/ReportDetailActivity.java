package com.septi.resq.fragment.report;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.septi.resq.OverviewActivity;
import com.septi.resq.R;
import com.septi.resq.SelectLocationActivity;
import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.database.TrackingDBHelper;
import com.septi.resq.model.Emergency;
import com.septi.resq.utils.GeocodingHelper;
import com.septi.resq.viewmodel.EmergencyViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ReportDetailActivity extends AppCompatActivity {
    private Chip typeChip, statusChip;
    private TextView descriptionTextView, locationTextView, timestampTextView;
    private ImageView imageView;
    private EmergencyDBHelper dbHelper;
    private Emergency currentEmergency;
    private static final int LOCATION_REQUEST_CODE = 1001;
    private EmergencyViewModel viewModel;
    private static final int PICK_IMAGE_REQUEST = 1002;
    private ImageView previewImageView;
    private String newImagePath;
    private Button btnEdit, btnDelete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        collapsingToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(this, android.R.color.white));

        typeChip = findViewById(R.id.typeChip);
        statusChip = findViewById(R.id.statusChip);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        locationTextView = findViewById(R.id.locationTextView);
        timestampTextView = findViewById(R.id.timestampTextView);
        imageView = findViewById(R.id.imageView);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        Button btnViewOnMap = findViewById(R.id.btnViewOnMap);

        viewModel = new ViewModelProvider(this).get(EmergencyViewModel.class);
        viewModel.init(
                new EmergencyDBHelper(this),
                new TrackingDBHelper(this)
        );

        long emergencyId = getIntent().getLongExtra("emergencyId", -1L);

        if (emergencyId == -1L) {
            Toast.makeText(this, "Laporan tidak Valid", Toast.LENGTH_SHORT).show();
            finish();
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detail Laporan");
        }

        dbHelper = new EmergencyDBHelper(this);
        currentEmergency = dbHelper.getEmergencyById((int) emergencyId);

        if (currentEmergency != null) {
            populateViews();
            updateButtonsVisibility();
        } else {
            Toast.makeText(this, "Laporan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnEdit.setOnClickListener(v -> {
            if (currentEmergency.getStatus() == Emergency.EmergencyStatus.MENUNGGU) {
                showEditDialog();
            } else {
                Toast.makeText(this, "Hanya laporan dengan status MENUNGGU yang dapat diedit", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (currentEmergency.getStatus() == Emergency.EmergencyStatus.MENUNGGU) {
                showDeleteConfirmation();
            } else {
                Toast.makeText(this, "Hanya laporan dengan status MENUNGGU yang dapat dihapus", Toast.LENGTH_SHORT).show();
            }
        });

        btnViewOnMap.setOnClickListener(v -> navigateToMap());
    }

    private void updateButtonsVisibility() {
        boolean isEditable = currentEmergency.getStatus() == Emergency.EmergencyStatus.MENUNGGU;
        btnEdit.setEnabled(isEditable);
        btnDelete.setEnabled(isEditable);

        // Optional: Update button appearance for disabled state
        if (!isEditable) {
            btnEdit.setAlpha(0.5f);
            btnDelete.setAlpha(0.5f);
        } else {
            btnEdit.setAlpha(1.0f);
            btnDelete.setAlpha(1.0f);
        }
    }
    @SuppressLint("DefaultLocale")
    private void populateViews() {
        typeChip.setText(currentEmergency.getType());
        statusChip.setText(currentEmergency.getStatus().toString());

        // Set status chip color based on status
        int statusColor;
        switch (currentEmergency.getStatus()) {
            case PROSES:
                statusColor = ContextCompat.getColor(this, R.color.status_in_progress);
                break;
            case SELESAI:
                statusColor = ContextCompat.getColor(this, R.color.status_resolved);
                break;
            default:
                statusColor = ContextCompat.getColor(this, R.color.status_pending);
                break;
        }
        statusChip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(statusColor));

        descriptionTextView.setText(currentEmergency.getDescription());

        double latitude = currentEmergency.getLatitude();
        double longitude = currentEmergency.getLongitude();

        locationTextView.setText(String.format("Location: %.6f, %.6f", latitude, longitude));

        GeocodingHelper.getAddressFromLocation(this, latitude, longitude, new GeocodingHelper.GeocodingCallback() {
            @Override
            public void onAddressReceived(String address) {
                locationTextView.setText(address);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ReportDetailActivity.this, "Gagal mendapatkan alamat", Toast.LENGTH_SHORT).show();
            }
        });

        timestampTextView.setText(currentEmergency.getTimestamp());

        String photoPath = currentEmergency.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);

            try {
                Uri photoUri;
                if (photoPath.startsWith("content://")) {
                    photoUri = Uri.parse(photoPath);
                } else {
                    File photoFile = new File(photoPath);
                    photoUri = FileProvider.getUriForFile(this,
                            getPackageName() + ".fileprovider",
                            photoFile);
                }

                Glide.with(this)
                        .load(photoUri)
                        .placeholder(R.drawable.error_image)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(imageView);

            } catch (Exception e) {
                e.printStackTrace();
                Glide.with(this)
                        .load(new File(photoPath))
                        .placeholder(R.drawable.error_image)
                        .error(R.drawable.error_image)
                        .centerCrop()
                        .into(imageView);
            }
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
        Button btnEditImage = dialogView.findViewById(R.id.btnEditImage);
        previewImageView = dialogView.findViewById(R.id.ivPreview);

        etType.setText(currentEmergency.getType());
        etDescription.setText(currentEmergency.getDescription());

        if (currentEmergency.getPhotoPath() != null && !currentEmergency.getPhotoPath().isEmpty()) {
            previewImageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(currentEmergency.getPhotoPath())
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .into(previewImageView);
        }

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Edit Emergency")
                .setPositiveButton("Simpan", null)
                .setNegativeButton("Batal", null)
                .create();

        btnEditLocation.setOnClickListener(v -> {
            dialog.dismiss();
            openLocationSelection();
        });

        btnEditImage.setOnClickListener(v -> openImagePicker());

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String newType = etType.getText().toString().trim();
                String newDescription = etDescription.getText().toString().trim();

                if (!newType.isEmpty()) {
                    currentEmergency.setType(newType);
                    currentEmergency.setDescription(newDescription);
                    if (newImagePath != null) {
                        currentEmergency.setPhotoPath(newImagePath);
                    }
                    viewModel.updateEmergency(currentEmergency);
                    populateViews();
                    Toast.makeText(ReportDetailActivity.this, "Laporan berhasil di update", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ReportDetailActivity.this, "Bagian ini tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            newImagePath = saveImageToPrivateStorage(imageUri);
            if (newImagePath != null && previewImageView != null) {
                previewImageView.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(newImagePath)
                        .placeholder(R.drawable.error_image)
                        .error(R.drawable.error_image)
                        .into(previewImageView);
            }
        } else if (requestCode == LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);

            currentEmergency.setLatitude(latitude);
            currentEmergency.setLongitude(longitude);
            dbHelper.updateEmergency(currentEmergency);
            populateViews();
            Toast.makeText(this, "Lokasi berhasil diupdate", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImageToPrivateStorage(Uri imageUri) {
        try {
            File directory;
            directory = new File(getFilesDir(), "emergency_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
            String imageFileName = "IMG_" + timeStamp + ".jpg";
            File file = new File(directory, imageFileName);

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Menghapus Laporan")
                .setMessage("Apa kamu yakin ingin menghapus Laporan")
                .setPositiveButton("Yes", (dialog, which) -> {
                    viewModel.deleteEmergency(currentEmergency.getId());
                    Toast.makeText(this, "Laporan dihapus", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}