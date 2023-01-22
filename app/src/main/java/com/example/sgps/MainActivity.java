package com.example.sgps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.security.Permission;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int MIN_UPDATE_INTERVAL_MILLIS = 500;
    public static final int MAX_UPDATE_DELAY_MILLIS = 2000;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView lat, lon, altitude, acc, speed, address, updates, sensor;
    Switch sw_location_updates, sw_gps_save_power;

    // this variable will keep track of whether or not we are checking the location
    boolean checking;

    // configure our location request
    LocationRequest locationRequest;

    // this app will depend mostly on the functionality provided by this class
    FusedLocationProviderClient fusedLocationProviderClient;

    // for location updates
    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lat = findViewById(R.id.lat);
        lon = findViewById(R.id.lon);
        altitude = findViewById(R.id.altitude);
        acc = findViewById(R.id.acc);
        speed = findViewById(R.id.speed);
        address = findViewById(R.id.address);
        updates = findViewById(R.id.updates);
        sensor = findViewById(R.id.sensor);
        sw_gps_save_power = findViewById(R.id.sw_gps_save_power);
        sw_location_updates = findViewById(R.id.sw_location_updates);

        // initializing locationRequest and how often it is updated
        locationRequest = new LocationRequest.Builder(1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MILLIS)
                .setMaxUpdateDelayMillis(MAX_UPDATE_DELAY_MILLIS)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build();

        sw_gps_save_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps_save_power.isChecked()) {
                    locationRequest = new LocationRequest.Builder(locationRequest)
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .build();
                    sensor.setText("Using GPS + Cell tower + Wifi");
                } else {
                    locationRequest = new LocationRequest.Builder(locationRequest)
                            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                            .build();
                    sensor.setText("Using Cell tower + Wifi");
                }
            }

        });

        sw_location_updates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_location_updates.isChecked()) {
                    startUpdateLocation();
                } else {
                    stopUpdateLocation();
                }
            }
        });

        //this event is triggered whenever the update interval is made
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null)
                    return;
                for (Location l : locationResult.getLocations()
                )
                    updateUI(l);

            }
        };

        updateLocation();


    }

    private void stopUpdateLocation() {
    }

    private void startUpdateLocation() {
    }

    private void updateUI(Location location) {
        // update all the view elements with new location data
        lat.setText(String.valueOf(location.getLatitude()));
        lon.setText(String.valueOf(location.getLongitude()));
        acc.setText(String.valueOf(location.getAccuracy()));

        if (location.hasSpeed())
            speed.setText(String.valueOf(location.getSpeed()));
        if (location.hasAltitude())
            altitude.setText(String.valueOf(location.getAltitude()));

        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address.setText(addresses.get(0).getAddressLine(0));
        } catch (IOException e) {
            address.setText("could not get address");
        }

    }

    private void updateLocation() {

        //get permission from user to track gps
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            // initialise fusedLocationProviderClient
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

            // let's get location and update UI
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUI(location);
                }
            });
        }
        else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }
}