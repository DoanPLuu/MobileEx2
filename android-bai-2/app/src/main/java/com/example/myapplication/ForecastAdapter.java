package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<ForecastItem> forecastList;
    private boolean isCelsius;

    public ForecastAdapter(List<ForecastItem> forecastList, boolean isCelsius) {
        this.forecastList = forecastList;
        this.isCelsius = isCelsius;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastItem item = forecastList.get(position);
        holder.dateText.setText(item.date);
        
        // Chuyển đổi nhiệt độ nếu cần
        double maxTempToShow = isCelsius ? item.maxTemp : celsiusToFahrenheit(item.maxTemp);
        double minTempToShow = isCelsius ? item.minTemp : celsiusToFahrenheit(item.minTemp);
        String unit = isCelsius ? "°C" : "°F";
        
        holder.tempText.setText(String.format("⬆ %.1f%s ⬇ %.1f%s", 
                                             maxTempToShow, unit, 
                                             minTempToShow, unit));
        
        holder.conditionText.setText(item.conditionText);

        Picasso.get().load("https:" + item.iconUrl).into(holder.iconImage);
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }
    
    public void setTemperatureUnit(boolean celsius) {
        this.isCelsius = celsius;
    }
    
    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9/5) + 32;
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, tempText, conditionText;
        ImageView iconImage;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            tempText = itemView.findViewById(R.id.tempText);
            conditionText = itemView.findViewById(R.id.conditionText);
            iconImage = itemView.findViewById(R.id.iconImage);
        }
    }
}
