package com.example.doanapp2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailsFragment extends Fragment {

    private TextView textHumidity, textWindSpeed, textPressure, textFeelsLike;
    private OpenWeatherMapApi weatherApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);

        // Khởi tạo UI
        textHumidity = view.findViewById(R.id.textHumidity);
        textWindSpeed = view.findViewById(R.id.textWindSpeed);
        textPressure = view.findViewById(R.id.textPressure);
        textFeelsLike = view.findViewById(R.id.textFeelsLike);

        // Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(OpenWeatherMapApi.class);

        // Lấy dữ liệu từ HomeFragment (giả sử thành phố đã được chọn)
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("city")) {
            String city = bundle.getString("city");
            fetchWeatherDetails(city);
        } else {
            Toast.makeText(getContext(), "Vui lòng chọn thành phố trước!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void fetchWeatherDetails(String city) {
        String apiKey = BuildConfig.WEATHER_API_KEY;

        Call<HomeFragment.WeatherResponse> call = weatherApi.getCurrentWeather(city, apiKey, "metric");
        call.enqueue(new Callback<HomeFragment.WeatherResponse>() {
            @Override
            public void onResponse(Call<HomeFragment.WeatherResponse> call, Response<HomeFragment.WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HomeFragment.WeatherResponse weather = response.body();
                    if (weather.main != null && weather.weather != null && !weather.weather.isEmpty()) {
                        textHumidity.setText(String.format("%d%%", (int) weather.main.humidity));
                        textWindSpeed.setText(String.format("%.1f m/s", weather.wind != null ? weather.wind.speed : 0));
                        textPressure.setText(String.format("%.0f hPa", weather.main.pressure));
                        textFeelsLike.setText(String.format("%.0f°C", weather.main.feelsLike));
                    } else {
                        Toast.makeText(getContext(), "Không thể tải dữ liệu chi tiết!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HomeFragment.WeatherResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Interface API (sử dụng lại từ HomeFragment)
    interface OpenWeatherMapApi {
        @retrofit2.http.GET("weather")
        Call<HomeFragment.WeatherResponse> getCurrentWeather(
                @retrofit2.http.Query("q") String city,
                @retrofit2.http.Query("appid") String apiKey,
                @retrofit2.http.Query("units") String units);
    }
}