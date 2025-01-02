package com.septi.resq.fragment;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.septi.resq.R;
import com.septi.resq.adapter.ActiveTeamsAdapter;
import com.septi.resq.adapter.QuickActionAdapter;
import com.septi.resq.adapter.RecentReportsAdapter;
import com.septi.resq.database.RescueTeamDBHelper;
import com.septi.resq.database.UserProfileDBHelper;
import com.septi.resq.model.QuickAction;
import com.septi.resq.model.RescueTeam;
import com.septi.resq.model.Report;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.septi.resq.model.UserProfile;
import com.septi.resq.utils.LocationUtils;
import com.septi.resq.viewmodel.UserProfileViewModel;
import com.septi.resq.utils.DummyData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private RecyclerView rvQuickActions;
    private Location currentLocation;
    private RecyclerView rvActiveTeams;
    private MaterialButton btnEmergency;
    private TextView btnToggleTeams;
    private boolean isShowingAllTeams = false;
    private ActiveTeamsAdapter activeTeamsAdapter;
    private ShapeableImageView ivProfile;
    private UserProfileDBHelper dbHelper;
    private TextView tvUsername;
    private UserProfileViewModel viewModel;
    private RescueTeamDBHelper rescueTeamDBHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rescueTeamDBHelper = new RescueTeamDBHelper(requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        LocationUtils.checkLocationPermission(requireActivity());

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        initializeViews(rootView);

        // Observe user profile changes
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                tvUsername.setText(profile.getName());
                if (profile.getPhotoUri() != null && !profile.getPhotoUri().isEmpty()) {
                    try {
                        Uri photoUri = Uri.parse(profile.getPhotoUri());
                        ivProfile.setImageURI(photoUri);
                    } catch (Exception e) {
                        ivProfile.setImageResource(R.drawable.ic_profile);
                    }
                }
            }
        });

        // Load initial profile data
        UserProfile initialProfile = dbHelper.getProfile(1);
        if (initialProfile != null) {
            viewModel.updateUserProfile(initialProfile);
        }

        setupRecyclerViews(rootView);
        setupClickListeners();

        TextView tvCurrentDate = rootView.findViewById(R.id.tv_current_date);
        updateCurrentDate(tvCurrentDate);

        return rootView;
    }


    private void updateCurrentDate( TextView tvCurrentDate ) {
        // Menggunakan SimpleDateFormat untuk format tanggal
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        tvCurrentDate.setText(currentDate);
    }

    private void initializeViews( View view ) {
        rvQuickActions = view.findViewById(R.id.rv_quick_actions);
        rvActiveTeams = view.findViewById(R.id.rv_active_teams);
        btnEmergency = view.findViewById(R.id.btn_emergency);
        btnToggleTeams = view.findViewById(R.id.btn_toggle_teams);
        tvUsername = view.findViewById(R.id.tv_username);
        ivProfile = view.findViewById(R.id.iv_profile);
        dbHelper = new UserProfileDBHelper(requireContext());
        if (btnToggleTeams != null) {
            setupToggleButton();
        }
    }


    private void setupRecyclerViews( View rootView ) {
        // Menyiapkan RecyclerView untuk Tim Aktif
        setupActiveTeamsRecyclerView();

        // Menyiapkan RecyclerView untuk Laporan Terbaru
        setupRecentReportsRecyclerView(rootView);

        // Menyiapkan RecyclerView untuk Tindakan Cepat
        setupQuickActionsRecyclerView();
    }

    /**
     * Menyiapkan RecyclerView untuk Tim Aktif.
     * Menampilkan tim yang tersedia dalam layout horizontal.
     */
    private void setupActiveTeamsRecyclerView() {
        // Get current location
        currentLocation = LocationUtils.getLastKnownLocation(requireContext());
        List<RescueTeam> availableTeams = rescueTeamDBHelper.getAvailableTeams();

        // Calculate distances
        if (currentLocation != null) {
            for (RescueTeam team : availableTeams) {
                Location teamLocation = new Location("");
                teamLocation.setLatitude(team.getLatitude());
                teamLocation.setLongitude(team.getLongitude());

                float distanceInMeters = currentLocation.distanceTo(teamLocation);
                team.setDistance(distanceInMeters / 1000.0); // Convert to kilometers
            }
        }

        LinearLayoutManager horizontalLayout = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        rvActiveTeams.setLayoutManager(horizontalLayout);

        activeTeamsAdapter = new ActiveTeamsAdapter(availableTeams);
        rvActiveTeams.setAdapter(activeTeamsAdapter);
    }


    /**
     * Menyiapkan RecyclerView untuk Laporan Terbaru.
     * Menampilkan laporan terbaru dalam format daftar vertikal.
     */
    private void setupRecentReportsRecyclerView( View rootView ) {
        // Mengambil laporan terbaru dari data dummy
        List<Report> recentReports = DummyData.getRecentReports();

        // Ambil referensi TextView untuk tanggal
        TextView tvCurrentDate = rootView.findViewById(R.id.tv_current_date);
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        tvCurrentDate.setText(currentDate);

        // Atur RecyclerView tanpa membatasi jumlah data
        RecyclerView rvRecentReports = rootView.findViewById(R.id.rv_recent_reports);
        rvRecentReports.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentReports.setAdapter(new RecentReportsAdapter(recentReports));
    }


    /**
     * Menyiapkan RecyclerView untuk Tindakan Cepat.
     * Menampilkan tindakan cepat dalam layout grid (4 kolom).
     */

    private void setupQuickActionsRecyclerView() {
        // Mengambil daftar tindakan cepat dari data dummy
        List<QuickAction> quickActions = DummyData.getQuickActions();

        // Menyiapkan layout GridLayoutManager untuk tindakan cepat (4 kolom)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        rvQuickActions.setLayoutManager(gridLayoutManager);

        // Menetapkan adapter untuk tindakan cepat
        QuickActionAdapter adapter = new QuickActionAdapter(quickActions);
        adapter.setOnItemClickListener(( quickAction, position ) -> {
            // Menangani klik pada tombol "Lihat Semua" (posisi 7)
            if (position == 7) {
                showAllActionsDialog();
            }
        });

        // Menetapkan adapter ke RecyclerView
        rvQuickActions.setAdapter(adapter);
    }


    private void setupToggleButton() {
        btnToggleTeams.setText("Tampilkan Semua");
        btnToggleTeams.setOnClickListener(v -> toggleTeamsView());
    }

    private void toggleTeamsView() {
        isShowingAllTeams = !isShowingAllTeams;
        btnToggleTeams.setText(isShowingAllTeams ? "Kecilkan" : "Tampilkan Semua");

        rvActiveTeams.setLayoutManager(new LinearLayoutManager(getContext(),
                isShowingAllTeams ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL,
                false));

        List<RescueTeam> teams = isShowingAllTeams ?
                rescueTeamDBHelper.getAllTeams() :
                rescueTeamDBHelper.getAvailableTeams();

        // Calculate distances for all teams
        if (currentLocation != null) {
            for (RescueTeam team : teams) {
                Location teamLocation = new Location("");
                teamLocation.setLatitude(team.getLatitude());
                teamLocation.setLongitude(team.getLongitude());

                float distanceInMeters = currentLocation.distanceTo(teamLocation);
                team.setDistance(distanceInMeters / 1000.0);
            }
        }

        activeTeamsAdapter.updateData(teams, isShowingAllTeams);
        rvActiveTeams.scheduleLayoutAnimation();
    }


    private void showAllActionsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_all_actions, null);

        RecyclerView dialogRecyclerView = dialogView.findViewById(R.id.dialog_recycler_view);
        Button btnClose = dialogView.findViewById(R.id.dialog_button_close);

        // Setup RecyclerView with all actions
        dialogRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        dialogRecyclerView.setAdapter(new QuickActionAdapter(DummyData.getAllActions()));

        // Create Dialog
        androidx.appcompat.app.AlertDialog dialog = builder.setView(dialogView).create();
        dialog.show();

        // Set border radius for dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Close button listener
        btnClose.setOnClickListener(v -> dialog.dismiss());
    }

    private void setupClickListeners() {
        btnEmergency.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:112"));
            startActivity(callIntent);
        });

    }
}