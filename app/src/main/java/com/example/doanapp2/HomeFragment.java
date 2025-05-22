package com.example.doanapp2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView; // Đã import
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
    private ScrollView scrollViewHome;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        weatherTranslations.put("moderate rain", "Mưa vừa");
        weatherTranslations.put("heavy intensity rain", "Mưa nhiều");
        weatherTranslations.put("very heavy rain", "Mưa rất nhiều");
        weatherTranslations.put("extreme rain", "Mưa cực đại");
        weatherTranslations.put("freezing rain", "Mưa đá");
        weatherTranslations.put("light intensity shower rain", "Mưa nhẹ");
        weatherTranslations.put("moderate intensity shower rain", "Mưa vừa");
        weatherTranslations.put("heavy intensity shower rain", "Mưa nhiều");
        weatherTranslations.put("ragged shower rain", "Mưa nhiễu");
        weatherTranslations.put("thunderstorm with light rain", "Dông nhẹ");
        weatherTranslations.put("thunderstorm with rain", "Dông vừa");
        weatherTranslations.put("thunderstorm with heavy rain", "Dông nhiều");
        weatherTranslations.put("light thunderstorm", "Dông nhẹ");
        weatherTranslations.put("thunderstorm", "Dông vừa");
        weatherTranslations.put("heavy thunderstorm", "Dông nhiều");
        weatherTranslations.put("ragged thunderstorm", "Dông nhiễu");
        weatherTranslations.put("thunderstorm with light drizzle", "Dông nhẹ");
        weatherTranslations.put("thunderstorm with drizzle", "Dông vừa");
        weatherTranslations.put("thunderstorm with heavy drizzle", "Dông nhiều");
        weatherTranslations.put("light intensity drizzle", "Mưa nhẹ");
        weatherTranslations.put("drizzle", "Mưa");
        weatherTranslations.put("heavy intensity drizzle", "Mưa nhiều");
        weatherTranslations.put("light intensity drizzle rain", "Mưa nhẹ");
        weatherTranslations.put("drizzle rain", "Mưa vừa");
        weatherTranslations.put("heavy intensity drizzle rain", "Mưa nhiều");
        weatherTranslations.put("shower rain and drizzle", "Mưa và mưa rào");
        weatherTranslations.put("heavy shower rain and drizzle", "Mưa và mưa rào nhiều");
        weatherTranslations.put("freezing drizzle", "Mưa đá");
        weatherTranslations.put("light rain and snow", "Mưa nhẹ và tuyết");
        weatherTranslations.put("rain and snow", "Mưa và tuyết");

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

        inputCity = view.findViewById(R.id.inputCity);
        textCityName = view.findViewById(R.id.textCityName);
        textTemperature = view.findViewById(R.id.textTemperature);
        textWeatherDescription = view.findViewById(R.id.textWeatherDescription);
        weatherIcon = view.findViewById(R.id.imageWeatherIcon);
        hourlyForecastRecyclerView = view.findViewById(R.id.hourlyForecastRecyclerView);
        scrollViewHome = view.findViewById(R.id.scrollView); // Lấy tham chiếu ScrollView

        hourlyForecastRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        hourlyForecastAdapter = new HourlyForecastAdapter(new ArrayList<>());
        hourlyForecastRecyclerView.setAdapter(hourlyForecastAdapter);

        // Xử lý sự kiện cho nút Tìm kiếm
        view.findViewById(R.id.buttonSearch).setOnClickListener(v -> {
            // Logic của buttonSearch sẽ không kiểm tra actionId
            String city = inputCity.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeatherDataByCity(city);
                hideKeyboard(v); // Gọi hàm ẩn bàn phím
                inputCity.clearFocus(); // Xóa focus khỏi EditText
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên thành phố", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện nhấn nút "Search" trên bàn phím
        inputCity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String city = inputCity.getText().toString().trim();
                if (!city.isEmpty()) {
                    fetchWeatherDataByCity(city);
                    hideKeyboard(v); // Gọi hàm ẩn bàn phím
                    inputCity.clearFocus(); // Xóa focus khỏi EditText
                } else {
                    Toast.makeText(getContext(), "Vui lòng nhập tên thành phố", Toast.LENGTH_SHORT).show();
                }
                return true; // Đã xử lý sự kiện
            }
            return false;
        });

        // Xử lý sự kiện chạm vào ScrollView để ẩn bàn phím
        if (scrollViewHome != null) {
            scrollViewHome.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboard(v);
                    if (inputCity != null) {
                        inputCity.clearFocus();
                    }
                }
                return false; // Trả về false để các sự kiện chạm khác (như scroll) vẫn hoạt động
            });
        }

        // Load dữ liệu ban đầu
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("latitude") && bundle.containsKey("longitude")) {
                double latitude = bundle.getDouble("latitude");
                double longitude = bundle.getDouble("longitude");
                fetchWeatherDataByLocation(latitude, longitude);
            } else if (bundle.containsKey("city")) {
                String city = bundle.getString("city");
                fetchWeatherDataByCity(city);
            } else {
                SharedPreferences appPrefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_APP, Context.MODE_PRIVATE);
                String lastCity = appPrefs.getString(MainActivity.CITY_KEY, "Lào Cai");
                fetchWeatherDataByCity(lastCity);
            }
        } else {
            SharedPreferences appPrefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_APP, Context.MODE_PRIVATE);
            String lastCity = appPrefs.getString(MainActivity.CITY_KEY, "Lào Cai");
            fetchWeatherDataByCity(lastCity);
        }
        return view;
    }

    // Định nghĩa hàm hideKeyboard ở đây, bên ngoài onCreateView nhưng vẫn trong class HomeFragment
    private void hideKeyboard(View viewContext) {
        if (getContext() != null && viewContext != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(viewContext.getWindowToken(), 0);
            }
        }
    }

    public void setCitySelectedListener(OnCitySelectedListener listener) {
        this.citySelectedListener = listener;
    }

    private String getTemperatureUnitPreference() {
        if (getContext() == null) return "metric";
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(SettingsFragment.SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(SettingsFragment.TEMP_UNIT_KEY, "metric");
    }

    private String getTemperatureDisplaySuffix(String unitPreference) {
        return "imperial".equals(unitPreference) ? "°F" : "°C";
    }


    public void fetchWeatherDataByCity(String city) {
        if (citySelectedListener != null) {
            citySelectedListener.onCitySelected(city);
        }
        String apiKey = BuildConfig.WEATHER_API_KEY;
        String tempUnitApi = getTemperatureUnitPreference();

        Call<WeatherResponse> currentWeatherCall = weatherApi.getCurrentWeather(city, apiKey, tempUnitApi);
        currentWeatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if(getContext() == null) return; // Kiểm tra context
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    updateCurrentWeatherUI(weather);
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy thông tin cho thành phố này.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                if(getContext() == null) return;
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Call<ForecastResponse> forecastCall = weatherApi.getHourlyForecast(city, apiKey, tempUnitApi);
        forecastCall.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if(getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    updateHourlyForecastUI(response.body());
                }
            }
            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                if(getContext() == null) return;
                Log.e("HomeFragment", "Forecast call failed: " + t.getMessage());
            }
        });
    }

    public void fetchWeatherDataByLocation(double lat, double lon) {
        String apiKey = BuildConfig.WEATHER_API_KEY;
        String tempUnitApi = getTemperatureUnitPreference();

        Call<WeatherResponse> currentWeatherCall = weatherApi.getCurrentWeatherByLatLon(lat, lon, apiKey, tempUnitApi);
        currentWeatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if(getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    updateCurrentWeatherUI(weather);
                    if (citySelectedListener != null && weather.name != null) {
                        citySelectedListener.onCitySelected(weather.name);
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu thời tiết từ vị trí.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                if(getContext() == null) return;
                Toast.makeText(getContext(), "Lỗi mạng (vị trí): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Call<ForecastResponse> forecastCall = weatherApi.getHourlyForecastByLatLon(lat, lon, apiKey, tempUnitApi);
        forecastCall.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if(getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    updateHourlyForecastUI(response.body());
                }
            }
            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                if(getContext() == null) return;
                Log.e("HomeFragment", "Location forecast call failed: " + t.getMessage());
            }
        });
    }

    private void updateCurrentWeatherUI(WeatherResponse weather) {
        if (getContext() == null || weather == null || weather.main == null || weather.weather == null || weather.weather.isEmpty()) {
            Log.e("HomeFragment", "Weather data incomplete for UI update.");
            return;
        }
        textCityName.setText(weather.name);
        String tempUnitPref = getTemperatureUnitPreference();
        String tempSuffix = getTemperatureDisplaySuffix(tempUnitPref);
        textTemperature.setText(String.format(Locale.getDefault(), "%.0f%s", weather.main.temp, tempSuffix));

        String description = weather.weather.get(0).description;
        textWeatherDescription.setText(weatherTranslations.getOrDefault(description.toLowerCase(), description));

        String iconCode = weather.weather.get(0).icon;
        if (iconCode != null && !iconCode.isEmpty()) {
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            Picasso.get().load(iconUrl).error(R.drawable.ic_weather_placeholder).into(weatherIcon);
        } else {
            weatherIcon.setImageResource(R.drawable.ic_weather_placeholder);
        }
    }

    private void updateHourlyForecastUI(ForecastResponse forecast) {
        if (getContext() == null || forecast == null || forecast.list == null) {
            Log.e("HomeFragment", "Forecast data incomplete for UI update.");
            return;
        }
        List<HourlyForecast> hourlyForecasts = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String tempUnitPref = getTemperatureUnitPreference();
        String tempSuffix = getTemperatureDisplaySuffix(tempUnitPref);

        for (int i = 0; i < Math.min(12, forecast.list.size()); i++) {
            ForecastResponse.ForecastItem item = forecast.list.get(i);
            if(item.main == null || item.weather == null || item.weather.isEmpty()) continue;

            String time = sdf.format(new Date(item.dt * 1000L));
            String temperature = String.format(Locale.getDefault(), "%.0f%s", item.main.temp, tempSuffix);
            String iconCode = item.weather.get(0).icon;
            String iconUrl = (iconCode != null && !iconCode.isEmpty()) ? "https://openweathermap.org/img/wn/" + iconCode + "@2x.png" : "";
            hourlyForecasts.add(new HourlyForecast(time, temperature, iconUrl));
        }
        if (hourlyForecastAdapter != null) { // Kiểm tra adapter không null
            hourlyForecastAdapter.updateData(hourlyForecasts);
        }
    }

    // --- Interface API, Model Lớp và Adapter ---
    // (Giữ nguyên các phần này)
    public interface OpenWeatherMapApi {
        @GET("weather")
        Call<WeatherResponse> getCurrentWeather(@Query("q") String city, @Query("appid") String apiKey, @Query("units") String units);

        @GET("forecast")
        Call<ForecastResponse> getHourlyForecast(@Query("q") String city, @Query("appid") String apiKey, @Query("units") String units);

        @GET("weather")
        Call<WeatherResponse> getCurrentWeatherByLatLon(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String apiKey, @Query("units") String units);

        @GET("forecast")
        Call<ForecastResponse> getHourlyForecastByLatLon(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String apiKey, @Query("units") String units);
    }

    public static class WeatherResponse {
        public String name;
        public Main main;
        public List<Weather> weather;
        public Wind wind;
        public static class Main {
            public float temp;
            public float humidity;
            public float pressure;
            public float feels_like;
        }
        public static class Wind {
            public float speed;
        }
        public static class Weather {
            public String description;
            public String icon;
        }
    }

    public static class ForecastResponse {
        public List<ForecastItem> list;
        public static class ForecastItem {
            public long dt;
            public Main main;
            public List<Weather> weather;
            public static class Main {
                public float temp;
            }
            public static class Weather {
                public String description;
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
        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hourly_forecast_item, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HourlyForecast forecast = forecastList.get(position);
            holder.textTime.setText(forecast.time);
            holder.textTemperature.setText(forecast.temperature);
            if (forecast.iconUrl != null && !forecast.iconUrl.isEmpty()) {
                // Sử dụng R.drawable.ic_weather_placeholder_small như đã thống nhất
                Picasso.get().load(forecast.iconUrl).error(R.drawable.ic_weather_placeholder).into(holder.imageWeatherIcon);
            } else {
                holder.imageWeatherIcon.setImageResource(R.drawable.ic_weather_placeholder);
            }
        }
        @Override public int getItemCount() { return forecastList.size(); }
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