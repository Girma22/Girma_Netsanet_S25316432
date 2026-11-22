package com.example.girma_netsanet_s2531643;

// Android imports
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

// AndroidX imports
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

// Application imports
import com.example.girma_netsanet_s2531643.adapter.SituationAdapter;
import com.example.girma_netsanet_s2531643.model.Situation;
import com.example.girma_netsanet_s2531643.ui.DetailActivity;
import com.example.girma_netsanet_s2531643.ui.MapActivity;
import com.example.girma_netsanet_s2531643.util.DistanceFilter;
import com.example.girma_netsanet_s2531643.viewmodel.SituationViewModel;

// Java imports
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    // Adapter displays traffic situations in the ListView
    private SituationAdapter adapter;

    // ViewModel manages downloading and storing traffic data
    private SituationViewModel viewModel;

    // Main screen components
    private EditText searchInput;
    private TextView statusText;
    private Spinner spinnerDistance;

    // Stores all downloaded traffic situations
    private List<Situation> allSituations = new ArrayList<>();

    // Selected distance radius in kilometres
    // Null means distance filtering is disabled
    private Double selectedRadiusKm = null;


    // -----------------------------------------
    // AUTOMATIC DATA REFRESH
    // -----------------------------------------

    private final Handler updateHandler =
            new Handler(Looper.getMainLooper());

    // Refresh traffic information every five minutes
    private static final long UPDATE_INTERVAL =
            5 * 60 * 1000L;

    // Automatically refresh traffic information
    private final Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {

            if (viewModel != null) {
                viewModel.refresh();
            }

            // Schedule the next automatic refresh
            updateHandler.postDelayed(
                    this,
                    UPDATE_INTERVAL
            );
        }
    };


    // -----------------------------------------
    // CREATE TOOLBAR MENU
    // -----------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(
                R.menu.main_menu,
                menu
        );

        return true;
    }


    // -----------------------------------------
    // HANDLE TOOLBAR MENU OPTIONS
    // -----------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            viewModel.refresh();
            Toast.makeText(this, "Refreshing traffic information...",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.menu_clear) {
            selectedRadiusKm = null;
            searchInput.setText("");
            applyFilters();

            Toast.makeText(this, "Search and filters cleared",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.menu_about) {
            new AlertDialog.Builder(this)
                    .setTitle("About ScotTraffic")
                    .setMessage("ScotTraffic provides roadworks and traffic information from Traffic Scotland.")
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // -----------------------------------------
    // ACTIVITY CREATION
    // -----------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Load the main screen layout
        setContentView(R.layout.activity_main);
        // -----------------------------------------
        // CONNECT THE TOOLBAR
        // -----------------------------------------
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        // Set the visible application title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ScotTraffic");
        }
        // -----------------------------------------
        // CONNECT XML COMPONENTS
        // -----------------------------------------
        ListView listView =
                findViewById(R.id.situationList);

        Button refreshButton =
                findViewById(R.id.startButton);

        Button mapButton =
                findViewById(R.id.mapButton);

        searchInput =
                findViewById(R.id.searchInput);

        statusText =
                findViewById(R.id.txtStatus);

        spinnerDistance =
                findViewById(R.id.spinnerDistance);

        Button btnSearchDistance =
                findViewById(R.id.btnSearchDistance);


        // -----------------------------------------
        // CREATE AND CONNECT THE ADAPTER
        // -----------------------------------------

        adapter = new SituationAdapter(this);

        listView.setAdapter(adapter);


        // -----------------------------------------
        // CREATE THE VIEWMODEL
        // -----------------------------------------

        viewModel = new ViewModelProvider(this)
                .get(SituationViewModel.class);


        // -----------------------------------------
        // OBSERVE DOWNLOADED TRAFFIC SITUATIONS
        // -----------------------------------------

        viewModel.getSituations().observe(this, situations -> {

            if (situations != null) {

                // Keep a copy of all downloaded situations
                allSituations = new ArrayList<>(situations);

                // Apply current search and distance filters
                applyFilters();
            }
        });


        // -----------------------------------------
        // OBSERVE DOWNLOAD STATUS
        // -----------------------------------------

        viewModel.getStatus().observe(this, status -> {

            if (status != null) {
                statusText.setText(status);
            }
        });


        // -----------------------------------------
        // MANUAL REFRESH BUTTON
        // -----------------------------------------

        refreshButton.setOnClickListener(v -> {

            viewModel.refresh();

        });


        // -----------------------------------------
        // SEARCH NEARBY TRAFFIC SITUATIONS
        // -----------------------------------------

        btnSearchDistance.setOnClickListener(v -> {

            if (spinnerDistance.getSelectedItem() == null) {
                return;
            }

            String selectedDistance =
                    spinnerDistance.getSelectedItem().toString();

            try {


// Extract the numerical distance from the Spinner text

                String numericDistance = selectedDistance
                        .replace("Distance Within", "")
                        .replace("km", "")
                        .trim();

// Convert the extracted distance into a number
                double radiusKm = Double.parseDouble(numericDistance);
                // Activate distance filtering
                selectedRadiusKm = radiusKm;

                // Apply distance and text filters together
                applyFilters();

            } catch (NumberFormatException e) {

                statusText.setText(
                        "Please select a valid distance."
                );
            }
        });


        // -----------------------------------------
        // OPEN GENERAL GOOGLE TRAFFIC MAP
        // -----------------------------------------

        mapButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    MapActivity.class
            );

            // Send all traffic situations to the map
            intent.putExtra(
                    "situations",
                    new ArrayList<>(allSituations)
            );

            // Open map without closing MainActivity
            startActivity(intent);
        });


        // -----------------------------------------
        // OPEN SELECTED ROAD DETAILS
        // -----------------------------------------

        listView.setOnItemClickListener(
                (parent, view, position, id) -> {

                    Situation selected =
                            adapter.getItem(position);

                    if (selected == null) {
                        return;
                    }

                    Intent intent = new Intent(
                            MainActivity.this,
                            DetailActivity.class
                    );

                    // Send the selected traffic situation
                    intent.putExtra(
                            "situation",
                            selected
                    );

                    // Open the details screen
                    startActivity(intent);
                }
        );


        // -----------------------------------------
        // ROAD-NAME AND TRAFFIC SEARCH
        // -----------------------------------------

        searchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                // Reapply all active filters
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        // -----------------------------------------
        // INITIAL DOWNLOAD
        // -----------------------------------------

        // Download traffic information when app opens
        viewModel.refresh();
    }


    // -----------------------------------------
    // APPLY SEARCH AND DISTANCE FILTERS
    // -----------------------------------------

    private void applyFilters() {

        if (searchInput == null || adapter == null) {
            return;
        }

        // Get the search text entered by the user
        String query = searchInput.getText()
                .toString()
                .toLowerCase(Locale.ROOT)
                .trim();

        // Store matching traffic situations
        List<Situation> filtered = new ArrayList<>();


        // Check every downloaded traffic situation
        for (Situation situation : allSituations) {

            if (situation == null) {
                continue;
            }

            // First apply text search
            if (!matchesSearch(situation, query)) {
                continue;
            }

            // Then apply distance filtering if enabled
            if (selectedRadiusKm != null) {

                double distance =
                        DistanceFilter.getNearestDistanceKm(
                                situation,
                                DistanceFilter.GLASGOW_LAT,
                                DistanceFilter.GLASGOW_LON
                        );

                if (distance > selectedRadiusKm) {
                    continue;
                }
            }

            // Add matching situation to results
            filtered.add(situation);
        }


        // Update the ListView adapter
        adapter.setItems(filtered);


        // Display number of matching situations
        if (statusText != null) {

            if (selectedRadiusKm != null) {

                statusText.setText(
                        filtered.size()
                                + " situations found within "
                                + selectedRadiusKm
                                + " km of Glasgow"
                );

            } else {

                statusText.setText(
                        filtered.size()
                                + " situations found"
                );
            }
        }
    }


    // -----------------------------------------
    // CHECK WHETHER A SITUATION MATCHES SEARCH
    // -----------------------------------------

    private boolean matchesSearch(
            Situation situation,
            String query) {

        // An empty search matches every situation
        if (query.isEmpty()) {
            return true;
        }

        // Read traffic information safely
        String road =
                safeText(situation.getRoadName());

        String comment =
                safeText(situation.getComment());

        String type =
                safeText(situation.getDisplayType());

        String recordType =
                safeText(situation.getType());

        String trafficType =
                safeText(situation.getTrafficType());

        String direction =
                safeText(situation.getDirection());

        String delayType =
                safeText(situation.getDelayType());

        String delayValue =
                safeText(situation.getDelayValue());


        // Search across available traffic information
        return road.contains(query)
                || comment.contains(query)
                || type.contains(query)
                || recordType.contains(query)
                || trafficType.contains(query)
                || direction.contains(query)
                || delayType.contains(query)
                || delayValue.contains(query);
    }


    // -----------------------------------------
    // SAFELY HANDLE NULL SEARCH VALUES
    // -----------------------------------------

    private String safeText(String value) {

        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT).trim();
    }


    // -----------------------------------------
    // START AUTOMATIC UPDATES
    // -----------------------------------------

    @Override
    protected void onStart() {

        super.onStart();

        // Avoid scheduling duplicate refresh tasks
        updateHandler.removeCallbacks(updateRunnable);

        // Schedule automatic refresh after five minutes
        updateHandler.postDelayed(
                updateRunnable,
                UPDATE_INTERVAL
        );
    }


    // -----------------------------------------
    // STOP AUTOMATIC UPDATES
    // -----------------------------------------

    @Override
    protected void onStop() {

        super.onStop();

        // Stop scheduled updates when activity is not visible
        updateHandler.removeCallbacks(updateRunnable);
    }
}
