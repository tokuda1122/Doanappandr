package com.example.doanapp2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private Spinner spinnerTemperatureUnit, spinnerLanguage;
    private Switch switchNotifications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Khởi tạo UI
        spinnerTemperatureUnit = view.findViewById(R.id.spinnerTemperatureUnit);
        spinnerLanguage = view.findViewById(R.id.spinnerLanguage);
        switchNotifications = view.findViewById(R.id.switchNotifications);

        // Xử lý sự kiện cho Spinner đơn vị nhiệt độ
        spinnerTemperatureUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String unit = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Đơn vị nhiệt độ: " + unit, Toast.LENGTH_SHORT).show();
                // Thêm logic lưu đơn vị (ví dụ: SharedPreferences)
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Xử lý sự kiện cho Spinner ngôn ngữ
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String language = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Ngôn ngữ: " + language, Toast.LENGTH_SHORT).show();
                // Thêm logic thay đổi ngôn ngữ (ví dụ: thay đổi locale)
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Xử lý sự kiện cho Switch thông báo
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(getContext(), "Thông báo: " + (isChecked ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
            // Thêm logic lưu trạng thái thông báo (ví dụ: SharedPreferences)
        });

        return view;
    }
}