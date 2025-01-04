package com.septi.resq;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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

    private Fragment activeFragment = homeFragment; // Fragment yang sedang aktif ditampilkan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview); // Menghubungkan dengan layout activity_overview

        // Mengatur warna status bar agar sesuai dengan tema aplikasi
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation); // Menginisialisasi bottom navigation
        LocationUtils.checkLocationPermission(this); // Mengecek izin lokasi pengguna

        // Menambahkan semua fragment ke FragmentManager dan menyembunyikan fragment yang tidak aktif
        fragmentManager.beginTransaction().add(R.id.fragment_container, profileFragment, "5").hide(profileFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, trackingFragment, "4").hide(trackingFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, mapFragment, "3").hide(mapFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, reportFragment, "2").hide(reportFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, homeFragment, "1").commit();

        // Cek apakah intent membawa data untuk navigasi langsung ke peta
        if (getIntent().getBooleanExtra("navigateToMap", false)) {
            switchFragment(mapFragment); // Berpindah ke fragment peta
            bottomNavigationView.setSelectedItemId(R.id.nav_map); // Menyetel navigasi peta sebagai aktif
            Bundle args = new Bundle();
            args.putLong("emergencyId", getIntent().getLongExtra("emergencyId", -1)); // Memasukkan ID darurat
            args.putDouble("latitude", getIntent().getDoubleExtra("latitude", 0)); // Memasukkan latitude lokasi
            args.putDouble("longitude", getIntent().getDoubleExtra("longitude", 0)); // Memasukkan longitude lokasi
            mapFragment.setArguments(args); // Mengirimkan data ke fragment peta
        }

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
    }

    // Method untuk berpindah antar fragment
    private void switchFragment(Fragment fragment) {
        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit(); // Menyembunyikan fragment aktif dan menampilkan fragment baru
        activeFragment = fragment; // Menyetel fragment baru sebagai aktif
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
