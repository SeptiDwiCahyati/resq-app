package com.septi.resq;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.septi.resq.fragment.DashboardFragment;
import com.septi.resq.fragment.MapFragment;
import com.septi.resq.fragment.ProfileFragment;
import com.septi.resq.fragment.TrackingFragment;
import com.septi.resq.fragment.report.ReportFragment;
import com.septi.resq.model.RescueTeam;
import com.septi.resq.utils.LocationUtils;

public class OverviewActivity extends AppCompatActivity implements LocationUtils.LocationPermissionCallback {

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private final Fragment homeFragment = new DashboardFragment();
    private final Fragment reportFragment = new ReportFragment();
    private final Fragment mapFragment = new MapFragment();
    private final Fragment trackingFragment = new TrackingFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private LottieAnimationView loadingAnimation;

    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        loadingAnimation = findViewById(R.id.loading_animation);

        showLoading();
        setupFragments();
        setupBottomNavigation();

        if (LocationUtils.hasLocationPermission(this)) {
            onLocationPermissionGranted();
        } else {
            LocationUtils.checkLocationPermission(this);
        }

        hideLoading();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                switchFragment(homeFragment); // fragment dashboard
                return true;
            } else if (itemId == R.id.nav_report) {
                switchFragment(reportFragment); // fragment laporan
                return true;
            } else if (itemId == R.id.nav_map) {
                switchFragment(mapFragment); // fragment peta
                return true;
            } else if (itemId == R.id.nav_tracking) {
                switchFragment(trackingFragment); // fragment pelacakan
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(profileFragment); // fragment profil
                return true;
            }

            return false;
        });

        if (getIntent().getBooleanExtra("navigateToMap", false)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_map);
            switchFragment(mapFragment);
        }
    }

    private void setupFragments() {
        fragmentManager.beginTransaction().add(R.id.fragment_container, profileFragment, "5").hide(profileFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, trackingFragment, "4").hide(trackingFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, mapFragment, "3").hide(mapFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, reportFragment, "2").hide(reportFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, homeFragment, "1").commit();
    }


    private void showLoading() {
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();
    }

    private void hideLoading() {
        loadingAnimation.setVisibility(View.GONE);
        loadingAnimation.cancelAnimation();
    }

    public void navigateToTrackingWithTeam(RescueTeam team) {
        switchFragment(trackingFragment);
        if (trackingFragment instanceof TrackingFragment) {
            ((TrackingFragment) trackingFragment).showTeamInfo(team);
        }
    }

    private void switchFragment(Fragment fragment) {
        showLoading();
        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
        activeFragment = fragment;
        hideLoading();
    }


    @Override
    public void onLocationPermissionGranted() {
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
