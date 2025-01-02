package com.septi.resq;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.septi.resq.fragment.DashboardFragment;
import com.septi.resq.fragment.MapFragment;
import com.septi.resq.fragment.ProfileFragment;
import com.septi.resq.fragment.ReportFragment;
import com.septi.resq.fragment.TrackingFragment;
import com.septi.resq.utils.LocationUtils;

public class OverviewActivity extends AppCompatActivity implements LocationUtils.LocationPermissionCallback {


    // Fragment Manager
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private final Fragment homeFragment = new DashboardFragment();
    private final Fragment reportFragment = new ReportFragment();
    private final Fragment mapFragment = new MapFragment();
    private final Fragment trackingFragment = new TrackingFragment();
    private final Fragment profileFragment = new ProfileFragment();

    private Fragment activeFragment = homeFragment;



    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        LocationUtils.checkLocationPermission(this);
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
    private void switchFragment( Fragment fragment ) {
        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
        activeFragment = fragment;
    }
    @Override
    public void onLocationPermissionGranted() {
        // Only update if we're on the dashboard fragment
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof DashboardFragment) {
            DashboardFragment dashboardFragment = (DashboardFragment) currentFragment;
            dashboardFragment.updateTeamDistances();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtils.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onLocationPermissionGranted();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (LocationUtils.hasLocationPermission(this)) {
            onLocationPermissionGranted();
        }
    }


}
