package com.example.sgps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
    TextView lat, lon, altitude, acc, speed, address, updates, sensor, count;
    Switch sw_location_updates, sw_gps_save_power;
    Button bt_new_waypoint, bt_new_waypoint_list, bt_show_map;

    // this variable will keep track of whether or not we are checking the location
    boolean checking;

    // configure our location request
    LocationRequest locationRequest;

    // this app will depend mostly on the functionality provided by this class
    FusedLocationProviderClient fusedLocationProviderClient;

    // for location updates
    LocationCallback locationCallback;

    // keeping track of my location
    Location currentLocation;

    // list of locations
    List<Location> savedLocations;

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
        count = findViewById(R.id.crumbs_count);
        bt_new_waypoint = findViewById(R.id.bt_new_way_point);
        bt_new_waypoint_list = findViewById(R.id.bt_new_way_point_list);
        bt_show_map = findViewById(R.id.bt_show_map);


        // initializing locationRequest and how often it is updated
        locationRequest = new LocationRequest.Builder(1000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MILLIS)
                .setMaxUpdateDelayMillis(MAX_UPDATE_DELAY_MILLIS)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build();

        bt_new_waypoint_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, show_saved_locations.class);
                startActivity(i);
            }
        });

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

        bt_new_waypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication myApplication = (MyApplication) getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentLocation);
            }
        });

        updateLocation();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    updateLocation();
                else {
                    Toast.makeText(this, "this app requires permission to be granted for it to work properly", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void stopUpdateLocation() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        lat.setText("Not tracking location");
        lon.setText("Not tracking location");
        altitude.setText("Not tracking location");
        acc.setText("Not tracking location");
        speed.setText("Not tracking location");
        address.setText("Not tracking location");
        updates.setText("Not tracking location");
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        updates.setText("Tracking location");
        checking = true;
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

        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        count.setText(Integer.toString(savedLocations.size()));

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
                    currentLocation = location;
                }
            });
        }
        else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }
}