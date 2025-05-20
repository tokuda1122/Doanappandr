package com.example.doanapp2;

import android.os.Bundle;
import android.util.Log;
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
import retrofit2.http.GET;
import retrofit2.http.Query;

public class HomeFragment extends Fragment {

    private EditText inputCity;
    private TextView textCityName, textTemperature, textWeatherDescription;
    private ImageView weatherIcon;
    private RecyclerView hourlyForecastRecyclerView;
    private HourlyForecastAdapter hourlyForecastAdapter;
    private OpenWeatherMapApi weatherApi;
    private Map<String, String> weatherTranslations;
    private OnCitySelectedListener citySelectedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo bảng dịch mô tả thời tiết
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

        // Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(OpenWeatherMapApi.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragament_home, container, false);

        // Khởi tạo các thành phần giao diện
        inputCity = view.findViewById(R.id.inputCity);
        textCityName = view.findViewById(R.id.textCityName);
        textTemperature = view.findViewById(R.id.textTemperature);
        textWeatherDescription = view.findViewById(R.id.textWeatherDescription);
        weatherIcon = view.findViewById(R.id.imageWeatherIcon);
        hourlyForecastRecyclerView = view.findViewById(R.id.hourlyForecastRecyclerView);

        // Thiết lập RecyclerView
        hourlyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        hourlyForecastAdapter = new HourlyForecastAdapter(new ArrayList<>());
        hourlyForecastRecyclerView.setAdapter(hourlyForecastAdapter);

        // Xử lý sự kiện nút tìm kiếm
        view.findViewById(R.id.buttonSearch).setOnClickListener(v -> {
            String city = inputCity.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeatherDataByCity(city);
                // Thông báo cho các Fragment khác
                if (citySelectedListener != null) {
                    citySelectedListener.onCitySelected(city);
                }
            } else {
                Toast.makeText(getContext(), R.string.error_empty_city, Toast.LENGTH_SHORT).show();
            }
        });

        // Kiểm tra dữ liệu từ Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("city")) {
                String city = bundle.getString("city");
                fetchWeatherDataByCity(city);
            } else if (bundle.containsKey("latitude") && bundle.containsKey("longitude")) {
                double latitude = bundle.getDouble("latitude");
                double longitude = bundle.getDouble("longitude");
                fetchWeatherDataByLocation(latitude, longitude);
            }
        } else {
            fetchWeatherDataByCity("Lào Cai"); // Fallback to default city
        }

        return view;
    }

    public void setCitySelectedListener(OnCitySelectedListener listener) {
        this.citySelectedListener = listener;
    }

    public void fetchWeatherDataByCity(String city) {
        String apiKey = BuildConfig.WEATHER_API_KEY;

        // Gọi API thời tiết hiện tại
        Call<WeatherResponse> currentWeatherCall = weatherApi.getCurrentWeather(city, apiKey, "metric");
        currentWeatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    Log.d("WeatherApp", "Current weather response: " + response.body().toString());
                    if (weather.weather != null && !weather.weather.isEmpty()) {
                        Log.d("WeatherApp", "Icon code: " + weather.weather.get(0).icon);
                    } else {
                        Log.e("WeatherApp", "Weather list is null or empty");
                    }
                    updateCurrentWeatherUI(weather);
                } else {
                    Log.e("WeatherApp", "Current weather error: HTTP " + response.code() + ", message: " + response.message());
                    Toast.makeText(getContext(), R.string.error_weather_fetch, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherApp", "Current weather failure: " + t.getMessage());
                Toast.makeText(getContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

        // Gọi API dự báo hàng giờ
        Call<ForecastResponse> forecastCall = weatherApi.getHourlyForecast(city, apiKey, "metric");
        forecastCall.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse forecast = response.body();
                    Log.d("WeatherApp", "Forecast response: " + response.body().toString());
                    updateHourlyForecastUI(forecast);
                } else {
                    Log.e("WeatherApp", "Forecast error: HTTP " + response.code() + ", message: " + response.message());
                    Toast.makeText(getContext(), R.string.error_forecast_fetch, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                Log.e("WeatherApp", "Forecast failure: " + t.getMessage());
                Toast.makeText(getContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void fetchWeatherDataByLocation(double lat, double lon) {
        String apiKey = BuildConfig.WEATHER_API_KEY;

        // Gọi API thời tiết hiện tại bằng tọa độ
        Call<WeatherResponse> currentWeatherCall = weatherApi.getCurrentWeatherByLatLon(lat, lon, apiKey, "metric");
        currentWeatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    Log.d("WeatherApp", "Current weather response (location): " + response.body().toString());
                    if (weather.weather != null && !weather.weather.isEmpty()) {
                        Log.d("WeatherApp", "Icon code: " + weather.weather.get(0).icon);
                    } else {
                        Log.e("WeatherApp", "Weather list is null or empty");
                    }
                    updateCurrentWeatherUI(weather);
                } else {
                    Log.e("WeatherApp", "Current weather error (location): HTTP " + response.code() + ", message: " + response.message());
                    Toast.makeText(getContext(), R.string.error_weather_fetch, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherApp", "Current weather failure (location): " + t.getMessage());
                Toast.makeText(getContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

        // Gọi API dự báo hàng giờ bằng tọa độ
        Call<ForecastResponse> forecastCall = weatherApi.getHourlyForecastByLatLon(lat, lon, apiKey, "metric");
        forecastCall.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse forecast = response.body();
                    Log.d("WeatherApp", "Forecast response (location): " + response.body().toString());
                    updateHourlyForecastUI(forecast);
                } else {
                    Log.e("WeatherApp", "Forecast error (location): HTTP " + response.code() + ", message: " + response.message());
                    Toast.makeText(getContext(), R.string.error_forecast_fetch, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                Log.e("WeatherApp", "Forecast failure (location): " + t.getMessage());
                Toast.makeText(getContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentWeatherUI(WeatherResponse weather) {
        textCityName.setText(weather.name);
        textTemperature.setText(String.format(Locale.getDefault(), "%.0f°C", weather.main.temp));
        String description = weather.weather.get(0).description;
        textWeatherDescription.setText(weatherTranslations.getOrDefault(description.toLowerCase(), description));

        // Tải biểu tượng thời tiết từ API
        String iconCode = weather.weather.get(0).icon;
        if (iconCode != null && !iconCode.isEmpty()) {
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            Log.d("WeatherApp", "Loading current weather icon: " + iconUrl);
            Picasso.get()
                    .load(iconUrl)
                    .into(weatherIcon, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("WeatherApp", "Current weather icon loaded successfully: " + iconUrl);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("WeatherApp", "Error loading current weather icon: " + e.getMessage());
                            weatherIcon.setImageDrawable(null); // Để ImageView trống
                        }
                    });
        } else {
            Log.e("WeatherApp", "Current weather icon code is null or empty");
            weatherIcon.setImageDrawable(null); // Để ImageView trống
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
        @GET("weather")
        Call<WeatherResponse> getCurrentWeather(@Query("q") String city, @Query("appid") String apiKey, @Query("units") String units);

        @GET("forecast")
        Call<ForecastResponse> getHourlyForecast(@Query("q") String city, @Query("appid") String apiKey, @Query("units") String units);

        @GET("weather")
        Call<WeatherResponse> getCurrentWeatherByLatLon(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String apiKey, @Query("units") String units);

        @GET("forecast")
        Call<ForecastResponse> getHourlyForecastByLatLon(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String apiKey, @Query("units") String units);
    }

    // Mô hình dữ liệu cho thời tiết hiện tại
    static class WeatherResponse {
        public String name;
        public Main main;
        public List<Weather> weather;
        public Wind wind;


        static class Main {
            public float temp;
            public float humidity;
            public float pressure;
            public float feelsLike;
        }
        static class Wind {
            public float speed;
        }

        static class Weather {
            public String description;
            public String icon;
        }
    }

    // Mô hình dữ liệu cho dự báo
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

    // Mô hình dữ liệu cho dự báo hàng giờ
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

    // Adapter cho RecyclerView
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
                Log.d("WeatherApp", "Loading hourly forecast icon: " + forecast.iconUrl);
                Picasso.get()
                        .load(forecast.iconUrl)
                        .into(holder.imageWeatherIcon, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                Log.d("WeatherApp", "Hourly forecast icon loaded successfully: " + forecast.iconUrl);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("WeatherApp", "Error loading hourly forecast icon: " + e.getMessage());
                                holder.imageWeatherIcon.setImageDrawable(null); // Để ImageView trống
                            }
                        });
            } else {
                Log.e("WeatherApp", "Hourly forecast icon URL is empty");
                holder.imageWeatherIcon.setImageDrawable(null); // Để ImageView trống
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