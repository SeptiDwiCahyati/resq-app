package com.septi.resq.fragment.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.resq.R;
import com.septi.resq.adapter.EmergencyAdapter;
import com.septi.resq.model.Emergency;
import com.septi.resq.viewmodel.EmergencyViewModel;

import java.util.ArrayList;

public class ReportFragment extends Fragment {
    private EmergencyAdapter adapter;
    private EmergencyViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // Toolbar Setup
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Emergency Reports");
        }

        // RecyclerView Setup
        RecyclerView recyclerView = view.findViewById(R.id.emergencyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // ViewModel Setup
        viewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);
        adapter = new EmergencyAdapter(new ArrayList<>(), requireContext());
        recyclerView.setAdapter(adapter);

        // Status Card Views - Get references to just the number TextViews
        TextView statusWaitingCount = view.findViewById(R.id.statusWaiting);
        TextView statusInProgressCount = view.findViewById(R.id.statusInProgress);
        TextView statusCompletedCount = view.findViewById(R.id.statusCompleted);

        // Observe emergency data changes
        viewModel.getEmergencies().observe(getViewLifecycleOwner(), emergencies -> {
            if (emergencies != null) {
                adapter.updateData(emergencies);

                // Calculate status counts
                long waiting = emergencies.stream().filter(e -> e.getStatus() == Emergency.EmergencyStatus.MENUNGGU).count();
                long inProgress = emergencies.stream().filter(e -> e.getStatus() == Emergency.EmergencyStatus.PROSES).count();
                long completed = emergencies.stream().filter(e -> e.getStatus() == Emergency.EmergencyStatus.SELESAI).count();

                // Update just the count numbers
                statusWaitingCount.setText(String.valueOf(waiting));
                statusInProgressCount.setText(String.valueOf(inProgress));
                statusCompletedCount.setText(String.valueOf(completed));
            }
        });

        // Search functionality
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return true;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadEmergencies();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.loadEmergencies();
    }
}