package com.septi.resq.fragment.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.resq.R;
import com.septi.resq.adapter.EmergencyAdapter;
import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.viewmodel.EmergencyViewModel;

import java.util.ArrayList;

public class ReportFragment extends Fragment {
    private EmergencyAdapter adapter;
    private EmergencyViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Emergency Reports");
        }

        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.emergencyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        viewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);

        // Setup SearchView
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        EmergencyDBHelper dbHelper = new EmergencyDBHelper(requireContext());
        viewModel.init(dbHelper);

        adapter = new EmergencyAdapter(new ArrayList<>(), requireContext(), viewModel);
        recyclerView.setAdapter(adapter);

        viewModel.getEmergencies().observe(getViewLifecycleOwner(), emergencies -> {
            if (emergencies != null) {
                adapter.updateData(emergencies);
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