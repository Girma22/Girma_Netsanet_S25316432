package com.example.girma_netsanet_s2531643.ui;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.girma_netsanet_s2531643.R;
import com.example.girma_netsanet_s2531643.model.Situation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;


public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    // Stores all traffic situations received from MainActivity
    private ArrayList<Situation> situations;

    // Stores one selected situation received from DetailActivity
    private Situation selectedSituation;


    // ------------------------------------------------
    // ACTIVITY CREATION
    // ------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the Google Maps layout
        setContentView(R.layout.activity_map);


        // ------------------------------------------------
        // RECEIVE SELECTED TRAFFIC SITUATION
        // ------------------------------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            selectedSituation = getIntent().getSerializableExtra(
                    "situation",
                    Situation.class
            );

        } else {

            selectedSituation = (Situation) getIntent()
                    .getSerializableExtra("situation");
        }


        // ------------------------------------------------
        // RECEIVE ALL TRAFFIC SITUATIONS
        // ------------------------------------------------

        Serializable receivedSituations;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            receivedSituations = getIntent().getSerializableExtra(
                    "situations",
                    ArrayList.class
            );

        } else {

            receivedSituations = getIntent()
                    .getSerializableExtra("situations");
        }

        situations = new ArrayList<>();

        if (receivedSituations instanceof ArrayList<?>) {

            for (Object item : (ArrayList<?>) receivedSituations) {

                if (item instanceof Situation) {
                    situations.add((Situation) item);
                }
            }
        }


        // ------------------------------------------------
        // NEW CODE: SET UP TOOLBAR AND BACK ARROW
        // ------------------------------------------------

        Toolbar toolbar = findViewById(R.id.mapToolbar);

        // Set the back-arrow image
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        // Choose the correct title for the current map
        if (selectedSituation != null) {

            toolbar.setTitle("Selected Situation");

        } else {

            toolbar.setTitle("Traffic Map");
        }

        // Return to the previous screen when arrow is clicked
        toolbar.setNavigationOnClickListener(v -> finish());


        // ------------------------------------------------
        // LOAD GOOGLE MAP
        // ------------------------------------------------

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {

            mapFragment.getMapAsync(this);
        }
    }


    // ------------------------------------------------
    // GOOGLE MAP READY
    // ------------------------------------------------

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        LatLngBounds.Builder boundsBuilder =
                new LatLngBounds.Builder();

        int markerCount = 0;


        // ------------------------------------------------
        // DISPLAY SELECTED SITUATION FIRST
        // ------------------------------------------------

        if (selectedSituation != null) {

            markerCount += addSituationMarker(
                    googleMap,
                    boundsBuilder,
                    selectedSituation
            );

        } else if (situations != null) {

            // Otherwise display all traffic situations
            for (Situation situation : situations) {

                markerCount += addSituationMarker(
                        googleMap,
                        boundsBuilder,
                        situation
                );
            }
        }


        // ------------------------------------------------
        // ADJUST MAP CAMERA TO DISPLAY MARKERS
        // ------------------------------------------------

        if (markerCount > 1) {

            // Zoom out to display all markers
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                            boundsBuilder.build(),
                            100
                    )
            );

        } else if (markerCount == 1) {

            // Zoom in when there is only one marker
            googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            boundsBuilder.build().getCenter(),
                            13f
                    )
            );

        } else {

            // Default location: Glasgow
            LatLng glasgow =
                    new LatLng(55.8642, -4.2518);

            googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            glasgow,
                            9f
                    )
            );
        }
    }


    // ------------------------------------------------
    // ADD TRAFFIC SITUATION MARKERS TO GOOGLE MAP
    // ------------------------------------------------

    private int addSituationMarker(
            GoogleMap googleMap,
            LatLngBounds.Builder boundsBuilder,
            Situation situation) {

        if (situation == null) {
            return 0;
        }

        int count = 0;


        // ------------------------------------------------
        // GET ROAD NAME
        // ------------------------------------------------

        String road = safe(situation.getRoadName());

        if (road.isEmpty()) {
            road = "Unknown road";
        }


        // ------------------------------------------------
        // GET SITUATION TYPE
        // ------------------------------------------------

        String type = safe(situation.getDisplayType());

        if (type.isEmpty()) {
            type = "Road situation";
        }


        // ------------------------------------------------
        // GET DESCRIPTION
        // ------------------------------------------------

        String comment = safe(situation.getComment());

        if (comment.isEmpty()) {
            comment = "No additional description available";
        }


        // ------------------------------------------------
        // GET DELAY INFORMATION
        // ------------------------------------------------

        String delay = safe(situation.getDelayValue());

        if (delay.isEmpty()) {
            delay = safe(situation.getDelayType());
        }

        if (delay.isEmpty()) {
            delay = "Information unavailable";
        }


        // ------------------------------------------------
        // BUILD MARKER INFORMATION
        // ------------------------------------------------

        String details =
                "Type: " + type
                        + "\nDescription: " + comment
                        + "\nDelay: " + delay;


        // Diagnostic information in Android Logcat
        Log.d(
                "MAP_DEBUG",
                "Road: " + road
                        + " | Type: " + type
                        + " | Comment: " + comment
                        + " | Delay: " + delay
        );


        // ------------------------------------------------
        // ADD STARTING LOCATION MARKER
        // ------------------------------------------------

        if (situation.hasFromPoint()) {

            LatLng location = new LatLng(
                    situation.getFromLat(),
                    situation.getFromLon()
            );

            googleMap.addMarker(
                    new MarkerOptions()
                            .position(location)
                            .title(road)
                            .snippet(details)
            );

            boundsBuilder.include(location);

            count++;
        }


        // ------------------------------------------------
        // ADD ENDING LOCATION MARKER
        // ------------------------------------------------

        if (situation.hasToPoint()) {

            LatLng location = new LatLng(
                    situation.getToLat(),
                    situation.getToLon()
            );

            googleMap.addMarker(
                    new MarkerOptions()
                            .position(location)
                            .title(road)
                            .snippet(details)
            );

            boundsBuilder.include(location);

            count++;
        }

        return count;
    }


    // ------------------------------------------------
    // HANDLE NULL VALUES SAFELY
    // ------------------------------------------------

    private String safe(String value) {

        return value == null ? "" : value.trim();
    }
}
