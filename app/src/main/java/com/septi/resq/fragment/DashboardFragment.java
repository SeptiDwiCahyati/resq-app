package com.septi.resq.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.septi.resq.OverviewActivity;
import com.septi.resq.R;
import com.septi.resq.adapter.ActiveTeamsAdapter;
import com.septi.resq.adapter.QuickActionAdapter;
import com.septi.resq.adapter.RecentReportsAdapter;
import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.database.RescueTeamDBHelper;
import com.septi.resq.database.TrackingDBHelper;
import com.septi.resq.database.UserProfileDBHelper;
import com.septi.resq.model.Emergency;
import com.septi.resq.model.QuickAction;
import com.septi.resq.model.Report;
import com.septi.resq.model.RescueTeam;
import com.septi.resq.model.UserProfile;
import com.septi.resq.utils.DummyData;
import com.septi.resq.utils.GeocodingHelper;
import com.septi.resq.utils.LocationUtils;
import com.septi.resq.viewmodel.EmergencyViewModel;
import com.septi.resq.viewmodel.UserProfileViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private RecyclerView rvQuickActions;
    private RecyclerView rvActiveTeams;
    private MaterialButton btnEmergency;
    private TextView btnToggleTeams;
    private boolean isShowingAllTeams = false;
    private boolean isCalculatingDistances = false;
    private ActiveTeamsAdapter activeTeamsAdapter;
    private ShapeableImageView ivProfile;
    private UserProfileDBHelper dbHelper;
    private TextView tvUsername;
    private UserProfileViewModel viewModel;
    private RescueTeamDBHelper rescueTeamDBHelper;
    private EmergencyViewModel emergencyViewModel;
    private RecentReportsAdapter recentReportsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = requireContext();
        rescueTeamDBHelper = new RescueTeamDBHelper(context);
        viewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        emergencyViewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);

        emergencyViewModel.init(
                new EmergencyDBHelper(context),
                new TrackingDBHelper(context)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(rootView);
        setupRecyclerViews(rootView);
        setupObservers();
        setupClickListeners();

        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                String username = profile.getName();
                if (username != null && username.length() > 7) {
                    username = username.substring(0, 7);
                }
                tvUsername.setText(username);

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

    public void updateTeamDistances() {
        if (isCalculatingDistances) {
            return;
        }

        isCalculatingDistances = true;
        Location currentLocation = LocationUtils.getLastKnownLocation(requireContext());

        if (currentLocation != null && activeTeamsAdapter != null) {
            List<RescueTeam> teams = isShowingAllTeams ?
                    rescueTeamDBHelper.getAllTeams() :
                    rescueTeamDBHelper.getAvailableTeams();

            for (RescueTeam team : teams) {
                Location teamLocation = new Location("");
                teamLocation.setLatitude(team.getLatitude());
                teamLocation.setLongitude(team.getLongitude());
                team.setDistance(currentLocation.distanceTo(teamLocation) / 1000.0);
            }

            activeTeamsAdapter.updateData(teams, isShowingAllTeams);
        }
        isCalculatingDistances = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtils.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateTeamDistances();
            }
        }
    }


    private void updateCurrentDate(TextView tvCurrentDate) {
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        tvCurrentDate.setText(currentDate);
    }

    private void initializeViews(View view) {
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


    private void setupRecyclerViews(View rootView) {
        setupActiveTeamsRecyclerView();
        setupRecentReportsRecyclerView(rootView);
        setupQuickActionsRecyclerView();
    }

    /**
     * Menyiapkan RecyclerView untuk Tim Aktif.
     * Menampilkan tim yang tersedia dalam layout horizontal.
     */
    private void setupActiveTeamsRecyclerView() {
        LinearLayoutManager horizontalLayout = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        rvActiveTeams.setLayoutManager(horizontalLayout);

        List<RescueTeam> availableTeams = rescueTeamDBHelper.getAvailableTeams();
        activeTeamsAdapter = new ActiveTeamsAdapter(availableTeams);

        activeTeamsAdapter.setOnItemClickListener((team, position) -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_tracking);

            ((OverviewActivity) requireActivity()).navigateToTrackingWithTeam(team);
        });

        rvActiveTeams.setAdapter(activeTeamsAdapter);

        if (LocationUtils.hasLocationPermission(requireContext())) {
            updateTeamDistances();
        }
    }

    /**
     * Menyiapkan RecyclerView untuk Laporan Terbaru.
     * Menampilkan laporan terbaru dalam format daftar vertikal.
     */

    private void setupRecentReportsRecyclerView(View rootView) {
        TextView tvCurrentDate = rootView.findViewById(R.id.tv_current_date);
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
        tvCurrentDate.setText(currentDate);

        RecyclerView rvRecentReports = rootView.findViewById(R.id.rv_recent_reports);
        rvRecentReports.setLayoutManager(new LinearLayoutManager(getContext()));
        recentReportsAdapter = new RecentReportsAdapter(new ArrayList<>());
        rvRecentReports.setAdapter(recentReportsAdapter);
    }

    private void setupObservers() {
        emergencyViewModel.getEmergencies().observe(getViewLifecycleOwner(), emergencies -> {
            List<Report> reports = new ArrayList<>();
            for (Emergency emergency : emergencies) {
                String timestamp = getRelativeTimeSpan(emergency.getTimestamp());
                Report newReport = new Report(
                        emergency.getType(),
                        "Loading address...",
                        timestamp,
                        emergency.getLatitude(),
                        emergency.getLongitude(),
                        emergency.getStatus()
                );
                reports.add(newReport);
                fetchAddressForReport(newReport);
            }
            recentReportsAdapter.updateReports(reports);
        });

        emergencyViewModel.getNewEmergency().observe(getViewLifecycleOwner(), emergency -> {
            if (emergency != null) {
                String timestamp = getRelativeTimeSpan(emergency.getTimestamp());
                Report newReport = new Report(
                        emergency.getType(),
                        "Loading address...",
                        timestamp,
                        emergency.getLatitude(),
                        emergency.getLongitude(),
                        emergency.getStatus()
                );

                List<Report> currentReports = new ArrayList<>(recentReportsAdapter.getReports());
                currentReports.add(0, newReport);
                recentReportsAdapter.updateReports(currentReports);

                fetchAddressForReport(newReport);
            }
        });

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
    }

    private void fetchAddressForReport(Report report) {
        GeocodingHelper.getAddressFromLocation(
                requireContext(),
                report.getLatitude(),
                report.getLongitude(),
                new GeocodingHelper.GeocodingCallback() {
                    @Override
                    public void onAddressReceived(String address) {
                        report.setLocation(address);
                        recentReportsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {
                        report.setLocation("Location unavailable");
                        recentReportsAdapter.notifyDataSetChanged();
                    }
                }
        );
    }


    private String getRelativeTimeSpan(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault());
            Date pastDate = sdf.parse(timestamp);
            Date now = new Date();

            long diffInMillis = now.getTime() - pastDate.getTime();
            long diffMinutes = diffInMillis / (60 * 1000);
            long diffHours = diffMinutes / 60;
            long diffDays = diffHours / 24;

            if (diffMinutes < 60) {
                return diffMinutes + " menit yang lalu";
            } else if (diffHours < 24) {
                return diffHours + " jam yang lalu";
            } else {
                return diffDays + " hari yang lalu";
            }
        } catch (ParseException e) {
            return timestamp;
        }
    }


    /**
     * Menyiapkan RecyclerView untuk Tindakan Cepat.
     * Menampilkan tindakan cepat dalam layout grid (4 kolom).
     */

    private void setupQuickActionsRecyclerView() {
        List<QuickAction> quickActions = DummyData.getQuickActions();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        rvQuickActions.setLayoutManager(gridLayoutManager);
        QuickActionAdapter adapter = new QuickActionAdapter(quickActions);
        adapter.setOnItemClickListener((quickAction, position) -> {
            if (position == 7) {
                showAllActionsDialog();
            } else {
                handleActionClick(quickAction);
            }
        });
        rvQuickActions.setAdapter(adapter);
    }

    private void showAllActionsDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_all_actions, null);

        RecyclerView dialogRecyclerView = dialogView.findViewById(R.id.dialog_recycler_view);
        Button btnClose = dialogView.findViewById(R.id.dialog_button_close);

        final androidx.appcompat.app.AlertDialog dialog = builder.setView(dialogView).create();

        List<QuickAction> allActions = DummyData.getAllActions();
        dialogRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        QuickActionAdapter dialogAdapter = new QuickActionAdapter(allActions);
        dialogAdapter.setOnItemClickListener((quickAction, position) -> {
            handleActionClick(quickAction);
            dialog.dismiss();
        });
        dialogRecyclerView.setAdapter(dialogAdapter);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void handleActionClick(QuickAction quickAction) {
        if (quickAction.getPhoneNumber() != null) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + quickAction.getPhoneNumber()));
            startActivity(callIntent);
        }
    }


    private void setupToggleButton() {
        btnToggleTeams.setText("Tampilkan Semua");
        btnToggleTeams.setOnClickListener(v -> toggleTeamsView());
    }

    private void toggleTeamsView() {
        isShowingAllTeams = !isShowingAllTeams;
        btnToggleTeams.setText(isShowingAllTeams ? "Sembunyikan" : "Tampilkan Semua");

        rvActiveTeams.setLayoutManager(new LinearLayoutManager(getContext(),
                isShowingAllTeams ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL,
                false));

        if (LocationUtils.hasLocationPermission(requireContext())) {
            updateTeamDistances();
        } else {
            List<RescueTeam> teams = isShowingAllTeams ?
                    rescueTeamDBHelper.getAllTeams() :
                    rescueTeamDBHelper.getAvailableTeams();
            activeTeamsAdapter.updateData(teams, isShowingAllTeams);
        }

        rvActiveTeams.scheduleLayoutAnimation();
    }




    private void setupClickListeners() {
        btnEmergency.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:112"));
            startActivity(callIntent);
        });

        ivProfile.setOnClickListener(v -> {
            Fragment profileFragment = new ProfileFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .hide(DashboardFragment.this)
                    .show(profileFragment)
                    .commit();

            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_profile);
        });
    }

}
