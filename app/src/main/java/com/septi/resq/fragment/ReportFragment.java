package com.septi.resq.fragment;

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
    private EmergencyAdapter adapter; // Adapter buat nampilin data emergency

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout fragment
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // Setup AppBar (toolbar di atas)
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // Set judul toolbar dan matikan tombol back
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Emergency Reports");
        }

        // RecyclerView buat nampilin daftar laporan
        RecyclerView recyclerView = view.findViewById(R.id.emergencyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Layout linear (vertikal)
        recyclerView.setHasFixedSize(true); // Supaya performa lebih oke

        // Ambil ViewModel dari activity
        // ViewModel buat manage data dan UI
        EmergencyViewModel viewModel1 = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);

        // SearchView buat filter data
        // Komponen buat fitur pencarian
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Filter data berdasarkan query yang diketik
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter data waktu user ngetik
                adapter.filter(newText);
                return true;
            }
        });

        // Setup database helper
        // Helper buat interaksi database
        EmergencyDBHelper dbHelper = new EmergencyDBHelper(requireContext());

        // Inisialisasi ViewModel dengan database helper
        EmergencyViewModel viewModel = new ViewModelProvider(requireActivity()).get(EmergencyViewModel.class);
        viewModel.init(dbHelper);

        // Setup adapter buat RecyclerView
        adapter = new EmergencyAdapter(new ArrayList<>(), requireContext());
        recyclerView.setAdapter(adapter);

        // Observasi perubahan data dari ViewModel
        viewModel.getEmergencies().observe(getViewLifecycleOwner(), emergencies -> {
            if (emergencies != null) {
                adapter.updateData(emergencies); // Update data di adapter
            }
        });

        return view; // Balikin view yang udah di-setup
    }
}
