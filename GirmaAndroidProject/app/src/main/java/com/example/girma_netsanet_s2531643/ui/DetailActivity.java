package com.example.girma_netsanet_s2531643.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.girma_netsanet_s2531643.R;
import com.example.girma_netsanet_s2531643.model.Situation;


public class DetailActivity extends AppCompatActivity {

    // Intent key used to receive the selected traffic situation
    private static final String EXTRA_SITUATION = "situation";

    // Stores the selected traffic situation
    private Situation situation;


    // ------------------------------------------------
    // ACTIVITY CREATION
    // ------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the road details layout
        setContentView(R.layout.activity_detail);


        // ------------------------------------------------
        // NEW CODE: SET UP TOOLBAR AND BACK ARROW
        // ------------------------------------------------

        Toolbar toolbar = findViewById(R.id.toolbar);

        // Set the Toolbar title
        toolbar.setTitle("Road Details");

        // Display the back-arrow icon
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

        // Return to the previous screen when arrow is clicked
        toolbar.setNavigationOnClickListener(v -> finish());


        // ------------------------------------------------
        // CONNECT XML COMPONENTS
        // ------------------------------------------------

        TextView txtHeading =
                findViewById(R.id.txtHeading);

        TextView txtDetails =
                findViewById(R.id.txtDetails);

        Button btnShowMap =
                findViewById(R.id.btnShowMap);


        // ------------------------------------------------
        // RETRIEVE SELECTED TRAFFIC SITUATION
        // ------------------------------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            situation = getIntent().getSerializableExtra(
                    EXTRA_SITUATION,
                    Situation.class
            );

        } else {

            situation = (Situation) getIntent()
                    .getSerializableExtra(EXTRA_SITUATION);
        }


        // Check whether a situation was received
        if (situation == null) {

            finish();
            return;
        }


        // ------------------------------------------------
        // DISPLAY ROAD HEADING
        // ------------------------------------------------

        String road = safe(situation.getRoadName());

        if (road.isEmpty()) {
            road = "Unknown road";
        }

        String displayType = safe(situation.getDisplayType());

        if (displayType.isEmpty()) {
            displayType = "Traffic situation";
        }

        txtHeading.setText(
                displayType + " - " + road
        );


        // ------------------------------------------------
        // DISPLAY TRAFFIC SITUATION INFORMATION
        // ------------------------------------------------

        SpannableStringBuilder details =
                new SpannableStringBuilder();


        // General situation information
        addRow(details, "Type", situation.getDisplayType());

        addRow(details, "Road", situation.getRoadName());

        addRow(details, "Description", situation.getComment());

        addRow(details, "Direction", situation.getDirection());

        addRow(details, "Start time", situation.getStartTime());


        // ------------------------------------------------
        // DISPLAY DELAY INFORMATION
        // ------------------------------------------------

        String delay = safe(situation.getDelayValue());

        if (delay.isEmpty()) {
            delay = safe(situation.getDelayType());
        }

        addDelayRow(details, delay);


        // ------------------------------------------------
        // DISPLAY ADDITIONAL ROAD INFORMATION
        // ------------------------------------------------

        addRow(details, "Lane impact", situation.getLaneImpact());

        addRow(details, "Maintenance", situation.getMaintenanceType());

        addRow(details, "Traffic information", situation.getTrafficType());

        addRow(details, "Road management", situation.getManagementType());

        addRow(details, "Source", situation.getSource());


        // ------------------------------------------------
        // DISPLAY START COORDINATES
        // ------------------------------------------------

        if (situation.hasFromPoint()) {

            addRow(
                    details,
                    "Start latitude",
                    String.valueOf(situation.getFromLat())
            );

            addRow(
                    details,
                    "Start longitude",
                    String.valueOf(situation.getFromLon())
            );
        }


        // ------------------------------------------------
        // DISPLAY END COORDINATES
        // ------------------------------------------------

        if (situation.hasToPoint()) {

            addRow(
                    details,
                    "End latitude",
                    String.valueOf(situation.getToLat())
            );

            addRow(
                    details,
                    "End longitude",
                    String.valueOf(situation.getToLon())
            );
        }


        // Display all collected details
        txtDetails.setText(details);


        // ------------------------------------------------
        // OPEN GOOGLE MAP FOR SELECTED SITUATION
        // ------------------------------------------------

        btnShowMap.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DetailActivity.this,
                    MapActivity.class
            );

            // Send the selected situation to MapActivity
            intent.putExtra(
                    EXTRA_SITUATION,
                    situation
            );

            // Open the selected situation map
            // Do not finish this Activity:
            // it must remain underneath the map.
            startActivity(intent);
        });
    }


    // ------------------------------------------------
    // ADD A NORMAL INFORMATION ROW
    // ------------------------------------------------

    private void addRow(
            SpannableStringBuilder builder,
            String label,
            String value) {

        value = safe(value);

        // Do not display empty information
        if (value.isEmpty()) {
            return;
        }

        int start = builder.length();

        // Add the information label
        builder.append(label).append(": ");

        // Give the label a purple colour
        builder.setSpan(
                new ForegroundColorSpan(Color.rgb(103, 80, 164)),
                start,
                builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Make the label bold
        builder.setSpan(
                new StyleSpan(Typeface.BOLD),
                start,
                builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Add the actual information
        builder.append(value).append("\n\n");
    }


    // ------------------------------------------------
    // ADD COLOURED DELAY INFORMATION
    // ------------------------------------------------

    private void addDelayRow(
            SpannableStringBuilder builder,
            String delay) {

        if (delay == null || delay.trim().isEmpty()) {
            delay = "Information unavailable";
        }

        int start = builder.length();

        // Add delay label
        builder.append("Delay: ");

        // Colour the label purple
        builder.setSpan(
                new ForegroundColorSpan(Color.rgb(103, 80, 164)),
                start,
                builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Make delay label bold
        builder.setSpan(
                new StyleSpan(Typeface.BOLD),
                start,
                builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );


        // ------------------------------------------------
        // CHOOSE DELAY COLOUR
        // ------------------------------------------------

        int valueStart = builder.length();

        builder.append(delay);

        String lower = delay.toLowerCase();

        int delayColor;

        // Red: long or very long delays
        if (lower.contains("long") || lower.contains("very")) {

            delayColor = Color.rgb(198, 40, 40);

            // Orange: short or normal delays
        } else if (lower.contains("short")
                || lower.contains("normal")) {

            delayColor = Color.rgb(230, 126, 34);

            // Green: no or negligible delays
        } else if (lower.contains("no")
                || lower.contains("negligible")) {

            delayColor = Color.rgb(46, 125, 50);

        } else {

            delayColor = Color.DKGRAY;
        }


        // Apply the delay colour
        builder.setSpan(
                new ForegroundColorSpan(delayColor),
                valueStart,
                builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Make delay information bold
        builder.setSpan(
                new StyleSpan(Typeface.BOLD),
                valueStart,
                builder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        builder.append("\n\n");
    }


    // ------------------------------------------------
    // HANDLE NULL VALUES SAFELY
    // ------------------------------------------------

    private String safe(String value) {

        return value == null ? "" : value.trim();
    }
}
