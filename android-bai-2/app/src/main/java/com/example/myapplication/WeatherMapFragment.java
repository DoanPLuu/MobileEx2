package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class WeatherMapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private Switch cloudLayerSwitch;
    private TileOverlay cloudTileOverlay;
    private double currentLat = 0;
    private double currentLon = 0;
    private static final String OPENWEATHERMAP_KEY = "c4090fb2694aa848fe88b3d88a37e6af";

    public WeatherMapFragment() {
        // Required empty public constructor
    }

    public static WeatherMapFragment newInstance() {
        return new WeatherMapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_map, container, false);
        mapView = view.findViewById(R.id.mapView);
        cloudLayerSwitch = view.findViewById(R.id.cloudLayerSwitch);
        
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        
        cloudLayerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (googleMap != null) {
                toggleCloudLayer(isChecked);
            }
        });
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        
        // Initialize the map with the same settings as in MainActivity
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).initializeMap(googleMap);
        }
    }

    private void toggleCloudLayer(boolean showCloud) {
        if (googleMap == null) return;
        
        if (showCloud) {
            if (cloudTileOverlay == null) {
                TileProvider cloudTileProvider = createCloudTileProvider();
                cloudTileOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(cloudTileProvider));
            } else {
                cloudTileOverlay.setVisible(true);
            }
        } else if (cloudTileOverlay != null) {
            cloudTileOverlay.setVisible(false);
        }
    }

    private TileProvider createCloudTileProvider() {
        return new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String url = String.format(Locale.US,
                        "https://tile.openweathermap.org/map/clouds_new/%d/%d/%d.png?appid=%s",
                        zoom, x, y, OPENWEATHERMAP_KEY);
                try {
                    return new URL(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    public void updateLocation(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
