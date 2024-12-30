package com.septi.rescuu;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    // Fragment Manager
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    // Fragments
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment reportFragment = new ReportFragment();
    private final Fragment mapFragment = new MapFragment();
    private final Fragment trackingFragment = new TrackingFragment();
    private final Fragment profileFragment = new ProfileFragment();

    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Add all fragments to the FragmentManager
        fragmentManager.beginTransaction().add(R.id.fragment_container, profileFragment, "5").hide(profileFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, trackingFragment, "4").hide(trackingFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, mapFragment, "3").hide(mapFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, reportFragment, "2").hide(reportFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, homeFragment, "1").commit();

        // Handle Bottom Navigation item clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (itemId == R.id.nav_report) {
                switchFragment(reportFragment);
                return true;
            } else if (itemId == R.id.nav_map) {
                switchFragment(mapFragment);
                return true;
            } else if (itemId == R.id.nav_tracking) {
                switchFragment(trackingFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }

            return false;
        });

    }

    // Method to switch fragments
    private void switchFragment(Fragment fragment) {
        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
        activeFragment = fragment;
    }
}
