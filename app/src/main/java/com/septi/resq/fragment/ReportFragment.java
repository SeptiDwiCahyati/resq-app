package com.septi.resq.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.resq.R;
import com.septi.resq.adapter.EmergencyAdapter;
import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.viewmodel.EmergencyViewModel;


public class ReportFragment extends Fragment {
    private EmergencyAdapter adapter;
    private EmergencyDBHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.emergencyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new EmergencyDBHelper(requireContext());
        EmergencyViewModel viewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);
        viewModel.init(dbHelper);

        adapter = new EmergencyAdapter(dbHelper.getAllEmergencies());
        recyclerView.setAdapter(adapter);

        // Observe emergency updates
        viewModel.getEmergencies().observe(getViewLifecycleOwner(), emergencies -> adapter.updateData(emergencies));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the list when fragment becomes visible
        if (adapter != null) {
            adapter.updateData(dbHelper.getAllEmergencies());
        }
    }
}