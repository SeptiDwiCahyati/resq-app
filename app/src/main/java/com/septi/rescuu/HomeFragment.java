package com.septi.rescuu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.septi.rescuu.adapter.ActiveTeamsAdapter;
import com.septi.rescuu.adapter.QuickActionAdapter;
import com.septi.rescuu.adapter.RecentReportsAdapter;
import com.septi.rescuu.model.QuickAction;
import com.septi.rescuu.model.RescueTeam;
import com.septi.rescuu.model.Report;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.septi.rescuu.util.DummyData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private RecyclerView rvQuickActions;
    private RecyclerView rvActiveTeams;
    private RecyclerView rvRecentReports;
    private MaterialButton btnEmergency;
    private TextView btnToggleTeams;
    private boolean isShowingAllTeams = false;
    private ActiveTeamsAdapter activeTeamsAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(view);
        setupRecyclerViews();
        setupClickListeners();

        return view;
    }
    private void initializeViews(View view) {
        rvQuickActions = view.findViewById(R.id.rv_quick_actions);
        rvActiveTeams = view.findViewById(R.id.rv_active_teams);
        rvRecentReports = view.findViewById(R.id.rv_recent_reports);
        btnEmergency = view.findViewById(R.id.btn_emergency);
        btnToggleTeams = view.findViewById(R.id.btn_toggle_teams);

        if (btnToggleTeams != null) {
            setupToggleButton();
        }
    }

    private void setupRecyclerViews() {
        // Menyiapkan RecyclerView untuk Tim Aktif
        setupActiveTeamsRecyclerView();

        // Menyiapkan RecyclerView untuk Laporan Terbaru
        setupRecentReportsRecyclerView();

        // Menyiapkan RecyclerView untuk Tindakan Cepat
        setupQuickActionsRecyclerView();
    }

    /**
     * Menyiapkan RecyclerView untuk Tim Aktif.
     * Menampilkan tim yang tersedia dalam layout horizontal.
     */
    private void setupActiveTeamsRecyclerView() {
        // Mengambil tim aktif yang statusnya "Tersedia"
        List<RescueTeam> availableTeams = DummyData.getActiveTeams().stream()
                .filter(team -> team.getStatus().equals("Tersedia"))
                .collect(Collectors.toList());

        // Menyiapkan layout RecyclerView secara horizontal
        LinearLayoutManager horizontalLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvActiveTeams.setLayoutManager(horizontalLayout);

        // Menetapkan adapter untuk tim aktif
        activeTeamsAdapter = new ActiveTeamsAdapter(availableTeams);
        rvActiveTeams.setAdapter(activeTeamsAdapter);
    }

    /**
     * Menyiapkan RecyclerView untuk Laporan Terbaru.
     * Menampilkan laporan terbaru dalam format daftar vertikal.
     */
    private void setupRecentReportsRecyclerView() {
        // Mengambil laporan terbaru dari data dummy
        List<Report> recentReports = DummyData.getRecentReports();

        // Menetapkan adapter untuk laporan terbaru
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
        adapter.setOnItemClickListener((quickAction, position) -> {
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

        // Update button text
        btnToggleTeams.setText(isShowingAllTeams ? "Kecilkan" : "Tampilkan Semua");

        // Update layout manager
        rvActiveTeams.setLayoutManager(new LinearLayoutManager(getContext(),
                isShowingAllTeams ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL,
                false));

        // Update adapter with new data and layout
        List<RescueTeam> teams = isShowingAllTeams
                ? DummyData.getActiveTeams()
                : DummyData.getActiveTeams().stream()
                .filter(team -> team.getStatus().equals("Tersedia"))
                .collect(Collectors.toList());

        activeTeamsAdapter.updateData(teams, isShowingAllTeams);

        // Smooth transition for layout change
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
            // Handle emergency button click
            // Implement emergency call logic here
        });
    }
}