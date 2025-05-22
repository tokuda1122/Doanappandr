package com.example.doanapp2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map; // Import Map nếu bạn định dùng weatherTranslations ở đây

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForecastFragment extends Fragment {

    private RecyclerView fiveDayForecastRecyclerView;
    private FiveDayForecastAdapter forecastAdapter;
    private HomeFragment.OpenWeatherMapApi weatherApi;
    private String currentCity;
    private TextView textCityNameForecast;
    private Map<String, String> weatherTranslations;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo weatherTranslations nếu bạn muốn dịch mô tả thời tiết trong fragment này
        // weatherTranslations = new HashMap<>(); // Tương tự HomeFragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);

        textCityNameForecast = view.findViewById(R.id.textCityNameForecast);
        fiveDayForecastRecyclerView = view.findViewById(R.id.fiveDayForecastRecyclerView);
        fiveDayForecastRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        forecastAdapter = new FiveDayForecastAdapter(new ArrayList<>());
        fiveDayForecastRecyclerView.setAdapter(forecastAdapter);
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
        weatherApi = retrofit.create(HomeFragment.OpenWeatherMapApi.class);


        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("city")) {
            currentCity = bundle.getString("city");
            if (currentCity != null && !currentCity.isEmpty()) {
                fetchFiveDayForecast(currentCity);
            } else {
                textCityNameForecast.setText("Chưa chọn thành phố");
                Toast.makeText(getContext(), "Chưa có thành phố nào được chọn.", Toast.LENGTH_SHORT).show();
            }
        } else {
            textCityNameForecast.setText("Chưa chọn thành phố");
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

    private void fetchFiveDayForecast(String city) {
        if (city == null || city.isEmpty() || weatherApi == null || getContext() == null) {
            Toast.makeText(getContext(), "Không thể tải dự báo.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (textCityNameForecast != null) { // Kiểm tra null
            textCityNameForecast.setText("Dự báo cho: " + city);
        }


        String apiKey = BuildConfig.WEATHER_API_KEY;
        String tempUnitApi = getTemperatureUnitPreference();

        Call<HomeFragment.ForecastResponse> call = weatherApi.getHourlyForecast(city, apiKey, tempUnitApi);
        call.enqueue(new Callback<HomeFragment.ForecastResponse>() {
            @Override
            public void onResponse(Call<HomeFragment.ForecastResponse> call, Response<HomeFragment.ForecastResponse> response) {
                if (getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    HomeFragment.ForecastResponse forecast = response.body();
                    updateFiveDayForecastUI(forecast);
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải dự báo 5 ngày! Mã: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HomeFragment.ForecastResponse> call, Throwable t) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Lỗi mạng (dự báo 5 ngày): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFiveDayForecastUI(HomeFragment.ForecastResponse forecast) {
        if (getContext() == null || forecast == null || forecast.list == null) {
            Log.e("ForecastFragment", "Forecast data incomplete for UI update.");
            return;
        }
        List<FiveDayForecastItem> forecastList = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, dd/MM", Locale.getDefault());

        String tempUnitPref = getTemperatureUnitPreference();
        String tempSuffix = getTemperatureDisplaySuffix(tempUnitPref);

        if (forecast.list.isEmpty()) {
            Toast.makeText(getContext(), "Không có dữ liệu dự báo.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < forecast.list.size(); i += 8) {
            if (i < forecast.list.size()) {
                HomeFragment.ForecastResponse.ForecastItem item = forecast.list.get(i);
                if(item.main == null || item.weather == null || item.weather.isEmpty()) continue;

                String day = dayFormat.format(new Date(item.dt * 1000L));
                String temperature = String.format(Locale.getDefault(), "%.0f%s", item.main.temp, tempSuffix);
                String iconCode = item.weather.get(0).icon;
                String iconUrl = (!iconCode.isEmpty()) ? "https://openweathermap.org/img/wn/" + iconCode + "@2x.png" : "";
                String description = item.weather.get(0).description;
                // Nếu bạn đã khởi tạo weatherTranslations, bạn có thể dịch mô tả ở đây:
                description = weatherTranslations.getOrDefault(description.toLowerCase(), description);
                forecastList.add(new FiveDayForecastItem(day, temperature, iconUrl, description));
            }
            if (forecastList.size() >= 5) break;
        }
        forecastAdapter.updateData(forecastList);
    }

    public void updateCity(String newCity) {
        currentCity = newCity;
        if (isAdded() && getView() != null && newCity != null && !newCity.isEmpty()) {
            fetchFiveDayForecast(newCity);
        } else if (newCity != null && !newCity.isEmpty()){
            Bundle args = getArguments();
            if (args == null) args = new Bundle();
            args.putString("city", newCity);
            setArguments(args);
        }
    }

    static class FiveDayForecastAdapter extends RecyclerView.Adapter<FiveDayForecastAdapter.ViewHolder> {
        private List<FiveDayForecastItem> forecastList;

        public FiveDayForecastAdapter(List<FiveDayForecastItem> forecastList) {
            this.forecastList = forecastList;
        }

        public void updateData(List<FiveDayForecastItem> newForecastList) {
            this.forecastList = newForecastList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.five_day_forecast_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FiveDayForecastItem item = forecastList.get(position);
            holder.textDay.setText(item.day);
            holder.textTemperature.setText(item.temperature);
            holder.textWeatherDescription.setText(item.description);
            if (item.iconUrl != null && !item.iconUrl.isEmpty()) {
                Picasso.get().load(item.iconUrl).error(R.drawable.ic_weather_placeholder).into(holder.imageWeatherIcon);
            } else {
                holder.imageWeatherIcon.setImageResource(R.drawable.ic_weather_placeholder);
            }
        }

        @Override
        public int getItemCount() {
            return forecastList.size();
        }

        // ViewHolder đã được sửa để khớp với ID trong five_day_forecast_item.xml của bạn
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textDay, textTemperature, textWeatherDescription;
            ImageView imageWeatherIcon;

            ViewHolder(View itemView) {
                super(itemView);
                textDay = itemView.findViewById(R.id.textDay);
                imageWeatherIcon = itemView.findViewById(R.id.imageWeatherIcon);       // Khớp với XML
                textTemperature = itemView.findViewById(R.id.textTemperature);     // Khớp với XML
                textWeatherDescription = itemView.findViewById(R.id.textWeatherDescription); // Khớp với XML
            }
        }
    }

    static class FiveDayForecastItem {
        String day, temperature, iconUrl, description;

        FiveDayForecastItem(String day, String temperature, String iconUrl, String description) {
            this.day = day;
            this.temperature = temperature;
            this.iconUrl = iconUrl;
            this.description = description;
        }
    }
}