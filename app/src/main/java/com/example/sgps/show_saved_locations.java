package com.example.sgps;

import androidx.appcompat.app.AppCompatActivity;

import android.database.DataSetObserver;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;

public class show_saved_locations extends AppCompatActivity {

    ListView lvSavedLocations

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_locations_list);

        lvSavedLocations = findViewById(R.id.lv_saved_locaations);

        MyApplication myApplication = (MyApplication) getApplicationContext();
        List<Location> savedLocations = myApplication.getMyLocations();

        lvSavedLocations.setAdapter(new ArrayAdapter<Location>(this, android.R.layout.simple_list_item_1, savedLocations ));
    }
}