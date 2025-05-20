package com.example.doanapp2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements OnCitySelectedListener {

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Khởi tạo BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_details) {
                selectedFragment = new DetailsFragment();
            } else if (itemId == R.id.nav_forecast) {
                selectedFragment = new ForecastFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            } else {
                return false;
            }
            loadFragment(selectedFragment);
            return true;
        });

        // Khởi tạo FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Khởi tạo permission launcher
        requestMultiplePermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            Boolean fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (fineLocationGranted || coarseLocationGranted) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, R.string.error_location_permission, Toast.LENGTH_SHORT).show();
                loadFragmentWithCity(new HomeFragment(), "Lào Cai");
            }
        });

        // Kiểm tra và yêu cầu quyền
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestMultiplePermissionsLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        // Tải fragment Trang chủ mặc định
        if (savedInstanceState == null) {
            loadFragmentWithCity(new HomeFragment(), "Lào Cai");
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            // Truyền tọa độ tới HomeFragment
                            HomeFragment homeFragment = new HomeFragment();
                            Bundle bundle = new Bundle();
                            bundle.putDouble("latitude", latitude);
                            bundle.putDouble("longitude", longitude);
                            homeFragment.setArguments(bundle);
                            loadFragment(homeFragment);
                        } else {
                            Toast.makeText(this, R.string.error_location_unavailable, Toast.LENGTH_SHORT).show();
                            loadFragmentWithCity(new HomeFragment(), "Lào Cai");
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Toast.makeText(this, R.string.error_location_unavailable, Toast.LENGTH_SHORT).show();
                        loadFragmentWithCity(new HomeFragment(), "Lào Cai");
                    });
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).setCitySelectedListener(this);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadFragmentWithCity(Fragment fragment, String city) {
        Bundle bundle = new Bundle();
        bundle.putString("city", city);
        fragment.setArguments(bundle);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).setCitySelectedListener(this);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCitySelected(String city) {
        // Cập nhật các Fragment khác với thành phố được chọn
        Bundle bundle = new Bundle();
        bundle.putString("city", city);
        DetailsFragment detailsFragment = new DetailsFragment();
        ForecastFragment forecastFragment = new ForecastFragment();
        detailsFragment.setArguments(bundle);
        forecastFragment.setArguments(bundle);
        // Có thể lưu thành phố vào SharedPreferences nếu cần
    }
}