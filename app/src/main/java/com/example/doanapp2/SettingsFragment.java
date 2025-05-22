package com.example.doanapp2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch; // Sửa thành androidx.appcompat.widget.SwitchCompat nếu dùng theme AppCompat
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat; // Sử dụng SwitchCompat
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private Spinner spinnerTemperatureUnit, spinnerLanguage;
    private SwitchCompat switchNotifications; // Đổi thành SwitchCompat

    public static final String SHARED_PREFS = "appSettingsPrefs";
    public static final String TEMP_UNIT_KEY = "tempUnit";
    public static final String LANGUAGE_KEY = "language";
    public static final String NOTIFICATIONS_KEY = "notificationsEnabled";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        spinnerTemperatureUnit = view.findViewById(R.id.spinnerTemperatureUnit);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
        switchNotifications = view.findViewById(R.id.switchNotifications); // Đảm bảo ID này khớp với SwitchCompat trong XML

        // Thiết lập Adapter cho Spinners bằng Java
        // Đảm bảo bạn đã tạo các mảng này trong strings.xml:
        // R.array.temperature_units_array và R.array.language_options_array
        if (getContext() != null) {
            ArrayAdapter<CharSequence> tempAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.temperature_units_array, android.R.layout.simple_spinner_item);
            tempAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTemperatureUnit.setAdapter(tempAdapter);

            ArrayAdapter<CharSequence> langAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.language_options_array, android.R.layout.simple_spinner_item);
            langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLanguage.setAdapter(langAdapter);
        }

        loadSettings();

        spinnerTemperatureUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String unitValue = (position == 0) ? "metric" : "imperial";
                saveStringSetting(TEMP_UNIT_KEY, unitValue);
                Toast.makeText(getContext(), "Đơn vị nhiệt độ: " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String langValue = (position == 0) ? "vi" : "en"; // Giả sử Tiếng Việt là 0, Tiếng Anh là 1
                saveStringSetting(LANGUAGE_KEY, langValue);
                Toast.makeText(getContext(), "Ngôn ngữ: " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBooleanSetting(NOTIFICATIONS_KEY, isChecked);
            Toast.makeText(getContext(), "Thông báo: " + (isChecked ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void saveStringSetting(String key, String value) {
        if (getContext() == null) return;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void saveBooleanSetting(String key, boolean value) {
        if (getContext() == null) return;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void loadSettings() {
        if (getContext() == null) return;
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        String tempUnit = sharedPreferences.getString(TEMP_UNIT_KEY, "metric");
        if (spinnerTemperatureUnit.getAdapter() != null) { // Kiểm tra adapter trước khi setSelection
            if (tempUnit.equals("imperial")) {
                spinnerTemperatureUnit.setSelection(1);
            } else {
                spinnerTemperatureUnit.setSelection(0);
            }
        }


        String language = sharedPreferences.getString(LANGUAGE_KEY, "vi");
        if (spinnerLanguage.getAdapter() != null) { // Kiểm tra adapter
            if (language.equals("en")) {
                spinnerLanguage.setSelection(1);
            } else {
                spinnerLanguage.setSelection(0);
            }
        }


        boolean notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_KEY, true);
        switchNotifications.setChecked(notificationsEnabled);
    }
}