package com.septi.resq.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.septi.resq.R;
import com.septi.resq.database.UserProfileDBHelper;
import com.septi.resq.model.UserProfile;
import com.septi.resq.viewmodel.UserProfileViewModel;

public class ProfileFragment extends Fragment {
    private UserProfileViewModel viewModel;
    private View rootView;
    private ShapeableImageView imgProfile;
    private TextInputEditText etName, etEmail, etPhone;
    private MaterialButton btnSave;
    private FloatingActionButton fabEditPhoto;
    private Uri selectedImageUri;
    private UserProfileDBHelper dbHelper;
    private UserProfile currentProfile;
    private static final long DEFAULT_USER_ID = 1;

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        selectedImageUri = data.getData();
                        imgProfile.setImageURI(selectedImageUri);
                    }
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new UserProfileDBHelper(requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        androidx.appcompat.widget.Toolbar toolbar = rootView.findViewById(R.id.toolbar);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Profile");
        }

        initViews();
        setupListeners();
        loadProfileData();
        return rootView;
    }


    private void initViews() {
        imgProfile = rootView.findViewById(R.id.img_profile);
        etName = rootView.findViewById(R.id.et_name);
        etEmail = rootView.findViewById(R.id.et_email);
        etPhone = rootView.findViewById(R.id.et_phone);
        btnSave = rootView.findViewById(R.id.btn_save);
        fabEditPhoto = rootView.findViewById(R.id.fab_edit_photo);
    }

    private void setupListeners() {
        fabEditPhoto.setOnClickListener(v -> checkAndRequestPermissions());

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImage.launch(intent);
    }

    private void loadProfileData() {
        currentProfile = dbHelper.getProfile(DEFAULT_USER_ID);
        if (currentProfile != null) {
            etName.setText(currentProfile.getName());
            etEmail.setText(currentProfile.getEmail());
            etPhone.setText(currentProfile.getPhone());
            if (currentProfile.getPhotoUri() != null && !currentProfile.getPhotoUri().isEmpty()) {
                imgProfile.setImageURI(Uri.parse(currentProfile.getPhotoUri()));
            }
        } else {
            currentProfile = new UserProfile();
            currentProfile.setId(DEFAULT_USER_ID);
        }
    }

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean readGranted = result.getOrDefault(android.Manifest.permission.READ_EXTERNAL_STORAGE, false);
                Boolean mediaGranted = result.getOrDefault(android.Manifest.permission.READ_MEDIA_IMAGES, false);

                if (readGranted || mediaGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(requireContext(), "Permission denied. Cannot access photos.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // untuk Android 13+ (API 33+)
            requestPermissionLauncher.launch(new String[]{
                    android.Manifest.permission.READ_MEDIA_IMAGES
            });
        } else {
            // Untuk Android 12 dibawahnya
            requestPermissionLauncher.launch(new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }
    }


    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (validateInput(name, email, phone)) {
            currentProfile.setName(name);
            currentProfile.setEmail(email);
            currentProfile.setPhone(phone);
            if (selectedImageUri != null) {
                currentProfile.setPhotoUri(selectedImageUri.toString());
            }

            boolean success;
            if (dbHelper.getProfile(DEFAULT_USER_ID) == null) {
                success = dbHelper.insertProfile(currentProfile) != -1;
            } else {
                success = dbHelper.updateProfile(currentProfile);
            }

            if (success) {
                viewModel.updateUserProfile(currentProfile);
                Toast.makeText(getContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to save profile", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean validateInput(String name, String email, String phone) {
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            return false;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}