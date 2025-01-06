package com.septi.resq;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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
import com.septi.resq.utils.LocationUtils;

public class OverviewActivity extends AppCompatActivity implements LocationUtils.LocationPermissionCallback {

    // Fragment Manager
    private final FragmentManager fragmentManager = getSupportFragmentManager(); // Mengelola fragment untuk berpindah antar layar

    // Deklarasi fragment yang digunakan dalam aplikasi
    private final Fragment homeFragment = new DashboardFragment(); // Fragment untuk dashboard
    private final Fragment reportFragment = new ReportFragment(); // Fragment untuk laporan
    private final Fragment mapFragment = new MapFragment(); // Fragment untuk peta
    private final Fragment trackingFragment = new TrackingFragment(); // Fragment untuk pelacakan
    private final Fragment profileFragment = new ProfileFragment(); // Fragment untuk profil pengguna
    private LottieAnimationView loadingAnimation;

    private Fragment activeFragment = homeFragment; // Fragment yang sedang aktif ditampilkan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        // Inisialisasi Loading Animation
        loadingAnimation = findViewById(R.id.loading_animation);

        showLoading(); // Tampilkan animasi loading

        // Proses persiapan aplikasi
        setupFragments(); // Menginisialisasi fragment
        setupBottomNavigation(); // Mengatur navigasi bawah

        // Mengecek izin lokasi
        if (LocationUtils.hasLocationPermission(this)) {
            onLocationPermissionGranted();
        } else {
            LocationUtils.checkLocationPermission(this);
        }

        hideLoading(); // Sembunyikan animasi loading setelah persiapan selesai
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation); // Menghubungkan dengan komponen navigasi bawah di layout

        // Menentukan aksi untuk setiap item navigasi bawah
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                switchFragment(homeFragment); // Berpindah ke fragment dashboard
                return true;
            } else if (itemId == R.id.nav_report) {
                switchFragment(reportFragment); // Berpindah ke fragment laporan
                return true;
            } else if (itemId == R.id.nav_map) {
                switchFragment(mapFragment); // Berpindah ke fragment peta
                return true;
            } else if (itemId == R.id.nav_tracking) {
                switchFragment(trackingFragment); // Berpindah ke fragment pelacakan
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(profileFragment); // Berpindah ke fragment profil
                return true;
            }

            return false; // Tidak ada aksi untuk item yang tidak dikenali
        });

        // Cek apakah intent membawa data untuk navigasi langsung ke peta
        if (getIntent().getBooleanExtra("navigateToMap", false)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_map); // Menyetel navigasi peta sebagai aktif
            switchFragment(mapFragment); // Berpindah ke fragment peta
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


    // Method untuk berpindah antar fragment
    private void switchFragment(Fragment fragment) {
        showLoading(); // Tampilkan loading saat mulai navigasi
        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
        activeFragment = fragment; // Set fragment baru sebagai aktif
        hideLoading(); // Sembunyikan loading setelah navigasi selesai
    }


    @Override
    public void onLocationPermissionGranted() {
        // Hanya memperbarui jika fragment aktif adalah DashboardFragment
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof DashboardFragment) {
            DashboardFragment dashboardFragment = (DashboardFragment) currentFragment;
            dashboardFragment.updateTeamDistances(); // Memperbarui jarak tim di dashboard
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtils.PERMISSION_REQUEST_CODE) { // Mengecek apakah request code sesuai
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onLocationPermissionGranted(); // Memanggil callback jika izin diberikan
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LocationUtils.hasLocationPermission(this)) { // Mengecek apakah izin lokasi telah diberikan
            onLocationPermissionGranted(); // Memanggil callback jika izin sudah ada
        }
    }
}
