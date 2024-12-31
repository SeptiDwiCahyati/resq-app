package com.septi.resq.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.septi.resq.R;
import com.septi.resq.adapter.EmergencyAdapter;
import com.septi.resq.database.EmergencyDBHelper;
import com.septi.resq.model.Emergency;

import java.util.List;

public class ReportFragment extends Fragment {
    private EmergencyAdapter adapter;
    private EmergencyDBHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.emergencyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize database helper
        dbHelper = new EmergencyDBHelper(requireContext());

        // Get emergencies from database
        List<Emergency> emergencies = dbHelper.getAllEmergencies();

        // Initialize and set adapter
        adapter = new EmergencyAdapter(emergencies);
        recyclerView.setAdapter(adapter);

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