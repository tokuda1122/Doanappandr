package com.example.doanapp2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private EditText inputCity;
    private TextView textCityName, textTemperature, textWeatherDescription;
    private ImageView weatherIcon;
    private RecyclerView hourlyForecastRecyclerView;
    private HourlyForecastAdapter hourlyForecastAdapter;
    private OpenWeatherMapApi weatherApi;
    private Map<String, String> weatherTranslations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragament_home, container, false);

        // Khởi tạo UI
        inputCity = view.findViewById(R.id.inputCity);
        textCityName = view.findViewById(R.id.textCityName);
        textTemperature = view.findViewById(R.id.textTemperature);
        textWeatherDescription = view.findViewById(R.id.textWeatherDescription);
        weatherIcon = view.findViewById(R.id.weatherIcon);
        hourlyForecastRecyclerView = view.findViewById(R.id.hourlyForecastRecyclerView);

        // Khởi tạo translations
        weatherTranslations = new HashMap<>();
        weatherTranslations.put("clear sky", "Trời quang");
        weatherTranslations.put("few clouds", "Ít mây");
        weatherTranslations.put("scattered clouds", "Mây rải rác");
        weatherTranslations.put("broken clouds", "Mây đứt quãng");
        weatherTranslations.put("shower rain", "Mưa rào");
        weatherTranslations.put("rain", "Mưa");
        weatherTranslations.put("thunderstorm", "Dông");
        weatherTranslations.put("snow", "Tuyết");
        weatherTranslations.put("mist", "Sương mù");
        weatherTranslations.put("overcast clouds", "Mây u ám");
        weatherTranslations.put("light rain", "Mưa nhẹ");
        weatherTranslations.put("heavy intensity rain", "Mưa mạnh");
        weatherTranslations.put("light intensity shower rain", "Mưa rào nhẹ");
        weatherTranslations.put("moderate rain", "Mưa vừa");
        weatherTranslations.put("heavy intensity shower rain", "Mưa rào mạnh");
        weatherTranslations.put("very heavy rain", "Mưa rất mạnh");
        weatherTranslations.put("extreme rain", "Mưa cực");
        weatherTranslations.put("freezing rain", "Mưa đá");
        weatherTranslations.put("light snow", "Tuyết nhẹ");
        weatherTranslations.put("moderate snow", "Tuyết vừa");
        weatherTranslations.put("heavy snow", "Tuyết mạnh");
        weatherTranslations.put("light shower snow", "Tuyết rào nhẹ");
        weatherTranslations.put("shower snow", "Tuyết rào");
        weatherTranslations.put("heavy shower snow", "Tuyết rào mạnh");

        // Thiết lập RecyclerView
        hourlyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        hourlyForecastAdapter = new HourlyForecastAdapter(new ArrayList<>());
        hourlyForecastRecyclerView.setAdapter(hourlyForecastAdapter);

        // Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(OpenWeatherMapApi.class);

        // Xử lý nút tìm kiếm
        view.findViewById(R.id.buttonSearch).setOnClickListener(v -> {
            String city = inputCity.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeatherDataByCity(city);
            } else {
                Toast.makeText(getContext(), R.string.error_empty_city, Toast.LENGTH_SHORT).show();
            }
        });

        // Tải thời tiết mặc định
        fetchWeatherDataByCity("Lào Cai");

        return view;
    }

    public void fetchWeatherDataByLocation(double lat, double lon) {
        String apiKey = BuildConfig.WEATHER_API_KEY;

        // Gọi API thời tiết hiện tại theo tọa độ
        Call<WeatherResponse> currentWeatherCall = weatherApi.getCurrentWeatherByLatLon(lat, lon, apiKey, "metric");
        currentWeatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    updateCurrentWeatherUI(weather);
                } else {
                    Toast.makeText(getContext(), R.string.error_weather_fetch, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

        // Gọi API dự báo theo giờ theo tọa độ
        Call<ForecastResponse> forecastCall = weatherApi.getHourlyForecastByLatLon(lat, lon, apiKey, "metric");
        forecastCall.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse forecast = response.body();
                    updateHourlyForecastUI(forecast);
                } else {
                    Toast.makeText(getContext(), R.string.error_forecast_fetch, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeatherDataByCity(String city) {
        String apiKey = BuildConfig.WEATHER_API_KEY;

        // Gọi API thời tiết hiện tại theo thành phố
        Call<WeatherResponse> currentWeatherCall = weatherApi.getCurrentWeather(city, apiKey, "metric");
        currentWeatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    updateCurrentWeatherUI(weather);
                } else {
                    Toast.makeText(getContext(), R.string.error_weather_fetch, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

        // Gọi API dự báo theo giờ theo thành phố
        Call<ForecastResponse> forecastCall = weatherApi.getHourlyForecast(city, apiKey, "metric");
        forecastCall.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse forecast = response.body();
                    updateHourlyForecastUI(forecast);
                } else {
                    Toast.makeText(getContext(), R.string.error_forecast_fetch, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentWeatherUI(WeatherResponse weather) {
        if (weather.main != null && weather.weather != null && !weather.weather.isEmpty()) {
            textCityName.setText(weather.name);
            textTemperature.setText(String.format(Locale.getDefault(), "%.0f°C", weather.main.temp));
            String description = weather.weather.get(0).description;
            textWeatherDescription.setText(weatherTranslations.getOrDefault(description.toLowerCase(), description));

            String iconCode = weather.weather.get(0).icon;
            if (iconCode != null && !iconCode.isEmpty()) {
                String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                Picasso.get()
                        .load(iconUrl)
                        .placeholder(R.drawable.ic_weather_placeholder)
                        .error(R.drawable.ic_error)
                        .into(weatherIcon);
            } else {
                weatherIcon.setImageDrawable(null);
            }
        } else {
            Toast.makeText(getContext(), R.string.error_weather_fetch, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateHourlyForecastUI(ForecastResponse forecast) {
        List<HourlyForecast> hourlyForecasts = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (int i = 0; i < Math.min(12, forecast.list.size()); i++) {
            ForecastResponse.ForecastItem item = forecast.list.get(i);
            String time = sdf.format(new Date(item.dt * 1000L));
            String temperature = String.format(Locale.getDefault(), "%.0f°C", item.main.temp);
            String iconCode = item.weather.get(0).icon;
            String iconUrl = iconCode != null && !iconCode.isEmpty() ?
                    "https://openweathermap.org/img/wn/" + iconCode + "@2x.png" : "";
            hourlyForecasts.add(new HourlyForecast(time, temperature, iconUrl));
        }

        hourlyForecastAdapter.updateData(hourlyForecasts);
    }

    // Interface API
    interface OpenWeatherMapApi {
        @retrofit2.http.GET("weather")
        Call<WeatherResponse> getCurrentWeather(@retrofit2.http.Query("q") String city, @retrofit2.http.Query("appid") String apiKey, @retrofit2.http.Query("units") String units);

        @retrofit2.http.GET("forecast")
        Call<ForecastResponse> getHourlyForecast(@retrofit2.http.Query("q") String city, @retrofit2.http.Query("appid") String apiKey, @retrofit2.http.Query("units") String units);

        @retrofit2.http.GET("weather")
        Call<WeatherResponse> getCurrentWeatherByLatLon(@retrofit2.http.Query("lat") double lat, @retrofit2.http.Query("lon") double lon, @retrofit2.http.Query("appid") String apiKey, @retrofit2.http.Query("units") String units);

        @retrofit2.http.GET("forecast")
        Call<ForecastResponse> getHourlyForecastByLatLon(@retrofit2.http.Query("lat") double lat, @retrofit2.http.Query("lon") double lon, @retrofit2.http.Query("appid") String apiKey, @retrofit2.http.Query("units") String units);
    }

    // Class model
    static class WeatherResponse {
        public String name;
        public Main main;
        public List<Weather> weather;

        static class Main {
            public float temp;
        }

        static class Weather {
            public String description;
            public String icon;
        }
    }

    static class ForecastResponse {
        public List<ForecastItem> list;

        static class ForecastItem {
            public long dt;
            public Main main;
            public List<Weather> weather;

            static class Main {
                public float temp;
            }

            static class Weather {
                public String icon;
            }
        }
    }

    static class HourlyForecast {
        public String time;
        public String temperature;
        public String iconUrl;

        public HourlyForecast(String time, String temperature, String iconUrl) {
            this.time = time;
            this.temperature = temperature;
            this.iconUrl = iconUrl;
        }
    }

    static class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {
        private List<HourlyForecast> forecastList;

        public HourlyForecastAdapter(List<HourlyForecast> forecastList) {
            this.forecastList = forecastList;
        }

        public void updateData(List<HourlyForecast> newForecastList) {
            this.forecastList = newForecastList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.hourly_forecast_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HourlyForecast forecast = forecastList.get(position);
            holder.textTime.setText(forecast.time);
            holder.textTemperature.setText(forecast.temperature);
            if (forecast.iconUrl != null && !forecast.iconUrl.isEmpty()) {
                Picasso.get()
                        .load(forecast.iconUrl)
                        .placeholder(R.drawable.ic_weather_placeholder)
                        .error(R.drawable.ic_error)
                        .into(holder.imageWeatherIcon);
            } else {
                holder.imageWeatherIcon.setImageDrawable(null);
            }
        }

        @Override
        public int getItemCount() {
            return forecastList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textTime, textTemperature;
            ImageView imageWeatherIcon;

            ViewHolder(View itemView) {
                super(itemView);
                textTime = itemView.findViewById(R.id.textTime);
                textTemperature = itemView.findViewById(R.id.textTemperature);
                imageWeatherIcon = itemView.findViewById(R.id.imageWeatherIcon);
            }
        }
    }
}