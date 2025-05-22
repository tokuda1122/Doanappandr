package com.example.doanapp2;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private String currentSelectedCity = "Lào Cai"; // Thành phố mặc định hoặc được cập nhật
    private BottomNavigationView bottomNavigationView;

    public static final String SHARED_PREFS_APP = "appGlobalPrefs";
    public static final String CITY_KEY = "currentCity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity); // Liên kết với main_activity.xml

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_APP, Context.MODE_PRIVATE);
        currentSelectedCity = sharedPreferences.getString(CITY_KEY, "Lào Cai");

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            Bundle args = new Bundle();
            int itemId = item.getItemId();
            String tag = null;

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                tag = "HomeFragment";
            } else if (itemId == R.id.nav_details) {
                selectedFragment = new DetailsFragment();
                tag = "DetailsFragment";
                args.putString("city", currentSelectedCity);
                selectedFragment.setArguments(args);
            } else if (itemId == R.id.nav_forecast) {
                selectedFragment = new ForecastFragment();
                tag = "ForecastFragment";
                args.putString("city", currentSelectedCity);
                selectedFragment.setArguments(args);
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
                tag = "SettingsFragment";
            } else {
                return false;
            }
            loadFragment(selectedFragment, tag);
            return true;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestMultiplePermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            boolean fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            boolean coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (fineLocationGranted || coarseLocationGranted) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Quyền vị trí bị từ chối.", Toast.LENGTH_SHORT).show();
                loadFragmentWithCity(new HomeFragment(), currentSelectedCity, "HomeFragment");
            }
        });

        if (savedInstanceState == null) {
            checkAndRequestPermissions();
        }
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestMultiplePermissionsLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadFragmentWithCity(new HomeFragment(), currentSelectedCity, "HomeFragment");
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        HomeFragment homeFragment = new HomeFragment();
                        Bundle bundle = new Bundle();
                        bundle.putDouble("latitude", latitude);
                        bundle.putDouble("longitude", longitude);
                        homeFragment.setArguments(bundle);
                        loadFragment(homeFragment, "HomeFragment");
                    } else {
                        Toast.makeText(this, "Không thể lấy vị trí. Sử dụng thành phố mặc định.", Toast.LENGTH_SHORT).show();
                        loadFragmentWithCity(new HomeFragment(), currentSelectedCity, "HomeFragment");
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Lỗi khi lấy vị trí. Sử dụng thành phố mặc định.", Toast.LENGTH_SHORT).show();
                    loadFragmentWithCity(new HomeFragment(), currentSelectedCity, "HomeFragment");
                });
    }

    private void loadFragment(Fragment fragment, String tag) {
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).setCitySelectedListener(this);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment, tag);
        transaction.commit();
    }

    private void loadFragmentWithCity(Fragment fragment, String city, String tag) {
        this.currentSelectedCity = city;
        saveCurrentCity(city);

        Bundle bundle = new Bundle();
        bundle.putString("city", city);
        fragment.setArguments(bundle);
        loadFragment(fragment, tag);
    }

    @Override
    public void onCitySelected(String city) {
        this.currentSelectedCity = city;
        saveCurrentCity(city);

        Fragment detailsFrag = getSupportFragmentManager().findFragmentByTag("DetailsFragment");
        if (detailsFrag instanceof DetailsFragment && detailsFrag.isVisible()) {
            ((DetailsFragment) detailsFrag).updateCity(city);
        }

        Fragment forecastFrag = getSupportFragmentManager().findFragmentByTag("ForecastFragment");
        if (forecastFrag instanceof ForecastFragment && forecastFrag.isVisible()) {
            ((ForecastFragment) forecastFrag).updateCity(city);
        }
    }

    private void saveCurrentCity(String city) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_APP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CITY_KEY, city);
        editor.apply();
    }
}