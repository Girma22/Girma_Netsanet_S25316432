package com.example.girma_netsanet_s2531643.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.girma_netsanet_s2531643.R;
import com.example.girma_netsanet_s2531643.model.Situation;

public class DetailFragment extends Fragment {

    private static final String ARG_SITUATION = "situation";

    public static DetailFragment newInstance(Situation situation) {

        DetailFragment fragment = new DetailFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_SITUATION, situation);

        fragment.setArguments(arguments);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_detail,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        Situation situation = null;

        Bundle arguments = getArguments();

        if (arguments != null) {
            situation = (Situation) arguments.getSerializable(ARG_SITUATION);
        }

        if (situation == null) {
            return;
        }

        final Situation selectedSituation = situation;

        TextView details = view.findViewById(R.id.txtDetails);
        Button mapButton = view.findViewById(R.id.btnShowMap);

        // Build the selected road incident information
        StringBuilder output = new StringBuilder();

        output.append("Type: ")
                .append(safe(selectedSituation.getDisplayType()))
                .append("\n\n");

        output.append("Road: ")
                .append(safe(selectedSituation.getRoadName()))
                .append("\n\n");

        output.append("Description: ")
                .append(safe(selectedSituation.getComment()))
                .append("\n\n");

        output.append("Direction: ")
                .append(safe(selectedSituation.getDirection()))
                .append("\n\n");

        output.append("Start time: ")
                .append(safe(selectedSituation.getStartTime()))
                .append("\n\n");

        // Prefer readable delay information
        String delay = selectedSituation.getDelayValue();

        if (delay == null || delay.trim().isEmpty()) {
            delay = selectedSituation.getDelayType();
        }

        output.append("Delay: ")
                .append(safe(delay))
                .append("\n\n");

        output.append("Lane impact: ")
                .append(safe(selectedSituation.getLaneImpact()))
                .append("\n\n");

        output.append("Source: ")
                .append(safe(selectedSituation.getSource()));

        details.setText(output.toString());

        // Google Maps is optional
        if (mapButton != null) {

            mapButton.setEnabled(
                    selectedSituation.hasAnyLocation()
            );

            mapButton.setOnClickListener(v -> {

                if (!selectedSituation.hasAnyLocation()) {
                    return;
                }

                Intent intent = new Intent(
                        requireContext(),
                        MapActivity.class
                );

                intent.putExtra(
                        "fromLat",
                        selectedSituation.getFromLat()
                );

                intent.putExtra(
                        "fromLon",
                        selectedSituation.getFromLon()
                );

                intent.putExtra(
                        "toLat",
                        selectedSituation.getToLat()
                );

                intent.putExtra(
                        "toLon",
                        selectedSituation.getToLon()
                );

                startActivity(intent);
            });
        }
    }

    // Handle missing text safely
    private String safe(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Not available";
        }

        return value;
    }
}
