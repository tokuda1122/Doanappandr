package com.example.doanapp2;

import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForecastFragment extends Fragment {

    private RecyclerView fiveDayForecastRecyclerView;
    private FiveDayForecastAdapter forecastAdapter;
    private OpenWeatherMapApi weatherApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);

        // Khởi tạo UI
        fiveDayForecastRecyclerView = view.findViewById(R.id.fiveDayForecastRecyclerView);
        fiveDayForecastRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        forecastAdapter = new FiveDayForecastAdapter(new ArrayList<>());
        fiveDayForecastRecyclerView.setAdapter(forecastAdapter);

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
            fetchFiveDayForecast(city);
        } else {
            Toast.makeText(getContext(), "Vui lòng chọn thành phố trước!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void fetchFiveDayForecast(String city) {
        String apiKey = BuildConfig.WEATHER_API_KEY;

        Call<ForecastResponse> call = weatherApi.getFiveDayForecast(city, apiKey, "metric");
        call.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ForecastResponse forecast = response.body();
                    updateFiveDayForecast(forecast);
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải dự báo!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFiveDayForecast(ForecastResponse forecast) {
        List<FiveDayForecastItem> forecastList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (int i = 0; i < Math.min(40, forecast.list.size()); i += 8) { // Lấy 5 ngày (8 mục mỗi 3 giờ)
            ForecastResponse.ForecastItem item = forecast.list.get(i);
            String day = dateFormat.format(new Date(item.dt * 1000L));
            String time = timeFormat.format(new Date(item.dt * 1000L));
            String temperature = String.format("%.0f°C", item.main.temp);
            String iconUrl = item.weather.get(0).icon != null && !item.weather.get(0).icon.isEmpty() ?
                    "https://openweathermap.org/img/wn/" + item.weather.get(0).icon + "@2x.png" : "";
            String description = item.weather.get(0).description;
            forecastList.add(new FiveDayForecastItem(day, time, temperature, iconUrl, description)); // Cập nhật với 5 tham số
        }

        forecastAdapter.updateData(forecastList);
    }

    // Interface API
    interface OpenWeatherMapApi {
        @retrofit2.http.GET("forecast")
        Call<ForecastResponse> getFiveDayForecast(
                @retrofit2.http.Query("q") String city,
                @retrofit2.http.Query("appid") String apiKey,
                @retrofit2.http.Query("units") String units);
    }

    // Class model cho dự báo 5 ngày
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
                public String description;
            }
        }
    }

    // Adapter cho RecyclerView
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
            holder.textDay.setText(item.day + " - " + item.time); // Kết hợp ngày và thời gian
            holder.textTemperature.setText(item.temperature);
            holder.textWeatherDescription.setText(item.description);
            if (!item.iconUrl.isEmpty()) {
                Picasso.get().load(item.iconUrl).into(holder.imageWeatherIcon);
            }
        }

        @Override
        public int getItemCount() {
            return forecastList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textDay, textTemperature, textWeatherDescription;
            ImageView imageWeatherIcon;

            ViewHolder(View itemView) {
                super(itemView);
                textDay = itemView.findViewById(R.id.textDay);
                textTemperature = itemView.findViewById(R.id.textTemperature);
                textWeatherDescription = itemView.findViewById(R.id.textWeatherDescription);
                imageWeatherIcon = itemView.findViewById(R.id.imageWeatherIcon);
            }
        }
    }

    // Model cho từng mục dự báo (cập nhật constructor)
    static class FiveDayForecastItem {
        String day, time, temperature, iconUrl, description;

        FiveDayForecastItem(String day, String time, String temperature, String iconUrl, String description) {
            this.day = day;
            this.time = time;
            this.temperature = temperature;
            this.iconUrl = iconUrl;
            this.description = description;
        }
    }
}