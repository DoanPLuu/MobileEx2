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

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder> {

    private List<HourlyForecastItem> hourlyList;
    private boolean isCelsius;

    public HourlyForecastAdapter(List<HourlyForecastItem> hourlyList, boolean isCelsius) {
        this.hourlyList = hourlyList;
        this.isCelsius = isCelsius;
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly_forecast, parent, false);
        return new HourlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        HourlyForecastItem item = hourlyList.get(position);
        holder.hourText.setText(item.getTime());
        
        // Chuyển đổi nhiệt độ nếu cần
        double tempToShow = isCelsius ? item.getTemp() : celsiusToFahrenheit(item.getTemp());
        String unit = isCelsius ? "°C" : "°F";
        holder.hourlyTempText.setText(String.format("%.1f%s", tempToShow, unit));
        
        holder.hourlyConditionText.setText(item.getCondition());
        holder.hourlyRainText.setText(item.getRainMm() + " mm");
        holder.hourlyWindText.setText(item.getWindSpeed() + " km/h");

        Picasso.get().load(item.getIconUrl()).into(holder.hourlyIconImage);
    }

    @Override
    public int getItemCount() {
        return hourlyList.size();
    }
    
    public void setTemperatureUnit(boolean celsius) {
        this.isCelsius = celsius;
    }
    
    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9/5) + 32;
    }

    static class HourlyViewHolder extends RecyclerView.ViewHolder {
        TextView hourText, hourlyTempText, hourlyConditionText, hourlyRainText, hourlyWindText;
        ImageView hourlyIconImage;

        public HourlyViewHolder(@NonNull View itemView) {
            super(itemView);
            hourText = itemView.findViewById(R.id.hourText);
            hourlyTempText = itemView.findViewById(R.id.hourlyTempText);
            hourlyConditionText = itemView.findViewById(R.id.hourlyConditionText);
            hourlyRainText = itemView.findViewById(R.id.hourlyRainText);
            hourlyWindText = itemView.findViewById(R.id.hourlyWindText);
            hourlyIconImage = itemView.findViewById(R.id.hourlyIconImage);
        }
    }
}
