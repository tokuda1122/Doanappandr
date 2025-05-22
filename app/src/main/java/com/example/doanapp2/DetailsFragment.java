package com.example.doanapp2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailsFragment extends Fragment {

    private TextView textHumidity, textWindSpeed, textPressure, textFeelsLike, textCityNameDetails;
    private HomeFragment.OpenWeatherMapApi weatherApi; // Sử dụng lại interface từ HomeFragment
    private String currentCity;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        textCityNameDetails = view.findViewById(R.id.textCityNameDetails);
        textHumidity = view.findViewById(R.id.textHumidity);
        textWindSpeed = view.findViewById(R.id.textWindSpeed);
        textPressure = view.findViewById(R.id.textPressure);
        textFeelsLike = view.findViewById(R.id.textFeelsLike);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(HomeFragment.OpenWeatherMapApi.class);


        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("city")) {
            currentCity = bundle.getString("city");
            if (currentCity != null && !currentCity.isEmpty()) {
                fetchWeatherDetails(currentCity);
            } else {
                textCityNameDetails.setText("Chưa chọn thành phố");
                Toast.makeText(getContext(), "Chưa có thành phố nào được chọn.", Toast.LENGTH_SHORT).show();
            }
        } else {
            textCityNameDetails.setText("Chưa chọn thành phố");
            Toast.makeText(getContext(), "Vui lòng chọn thành phố từ trang chủ!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private String getTemperatureUnitPreference() {
        if (getContext() == null) return "metric";
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SettingsFragment.SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SettingsFragment.TEMP_UNIT_KEY, "metric");
    }

    private String getTemperatureDisplaySuffix(String unitPreference) {
        return "imperial".equals(unitPreference) ? "°F" : "°C";
    }

    private void fetchWeatherDetails(String city) {
        if (city == null || city.isEmpty() || weatherApi == null || getContext() == null) {
            Toast.makeText(getContext(), "Không thể tải dữ liệu chi tiết.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (textCityNameDetails != null) { // Kiểm tra null
            textCityNameDetails.setText("Chi tiết cho: " + city);
        }


        String apiKey = BuildConfig.WEATHER_API_KEY;
        String tempUnitApi = getTemperatureUnitPreference();

        Call<HomeFragment.WeatherResponse> call = weatherApi.getCurrentWeather(city, apiKey, tempUnitApi);
        call.enqueue(new Callback<HomeFragment.WeatherResponse>() {
            @Override
            public void onResponse(Call<HomeFragment.WeatherResponse> call, Response<HomeFragment.WeatherResponse> response) {
                if (getContext() == null) return; // Kiểm tra context trước khi dùng
                if (response.isSuccessful() && response.body() != null) {
                    HomeFragment.WeatherResponse weather = response.body();
                    if (weather.main != null && weather.weather != null && !weather.weather.isEmpty() && weather.wind != null) {
                        textHumidity.setText(String.format(Locale.getDefault(), "%d%%", (int) weather.main.humidity));
                        textWindSpeed.setText(String.format(Locale.getDefault(), "%.1f m/s", weather.wind.speed));
                        textPressure.setText(String.format(Locale.getDefault(), "%.0f hPa", weather.main.pressure));

                        String tempSuffix = getTemperatureDisplaySuffix(tempUnitApi);
                        textFeelsLike.setText(String.format(Locale.getDefault(), "%.0f%s", weather.main.feels_like, tempSuffix));
                    } else {
                        Toast.makeText(getContext(), "Dữ liệu chi tiết không đầy đủ!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải dữ liệu chi tiết! Mã: " + response.code() , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HomeFragment.WeatherResponse> call, Throwable t) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Lỗi mạng (chi tiết): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateCity(String newCity) {
        currentCity = newCity;
        // Không cần setArguments lại ở đây nếu Fragment đã được thêm vào Activity
        // việc gọi fetchWeatherDetails là đủ nếu view đã được tạo.

        if (isAdded() && getView() != null && newCity != null && !newCity.isEmpty()) {
            fetchWeatherDetails(newCity);
        } else if (newCity != null && !newCity.isEmpty()){
            // Nếu view chưa sẵn sàng, lưu lại thành phố để fetch khi onCreateView
            Bundle args = getArguments();
            if (args == null) args = new Bundle();
            args.putString("city", newCity);
            setArguments(args);
        }
    }
}