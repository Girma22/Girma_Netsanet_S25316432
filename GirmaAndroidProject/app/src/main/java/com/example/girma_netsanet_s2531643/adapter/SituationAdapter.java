package com.example.girma_netsanet_s2531643.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.girma_netsanet_s2531643.R;
import com.example.girma_netsanet_s2531643.model.Situation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SituationAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;

    private List<Situation> originalItems = new ArrayList<>();
    private List<Situation> items = new ArrayList<>();

    public SituationAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setItems(List<Situation> situations) {
        originalItems = new ArrayList<>(situations);
        items = new ArrayList<>(originalItems);
        notifyDataSetChanged();
    }

    public void filter(String searchText) {

        String query = searchText == null
                ? ""
                : searchText.trim().toLowerCase(Locale.ROOT);

        if (query.isEmpty()) {
            items = new ArrayList<>(originalItems);
            notifyDataSetChanged();
            return;
        }

        List<Situation> filteredList = new ArrayList<>();

        for (Situation situation : originalItems) {

            String road = safe(situation.getRoadName())
                    .toLowerCase(Locale.ROOT);

            String type = safe(situation.getDisplayType())
                    .toLowerCase(Locale.ROOT);

            String description = safe(situation.getComment())
                    .toLowerCase(Locale.ROOT);

            String direction = safe(situation.getDirection())
                    .toLowerCase(Locale.ROOT);

            String delayType = safe(situation.getDelayType())
                    .toLowerCase(Locale.ROOT);

            String delayValue = safe(situation.getDelayValue())
                    .toLowerCase(Locale.ROOT);

            String situationType = safe(situation.getType())
                    .toLowerCase(Locale.ROOT);

            if (road.contains(query)
                    || type.contains(query)
                    || description.contains(query)
                    || direction.contains(query)
                    || delayType.contains(query)
                    || delayValue.contains(query)
                    || situationType.contains(query)) {

                filteredList.add(situation);
            }
        }

        items = filteredList;
        notifyDataSetChanged();
    }

    public void resetFilter() {
        items = new ArrayList<>(originalItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Situation getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        if (row == null) {
            row = inflater.inflate(
                    R.layout.row_situation,
                    parent,
                    false
            );
        }

        Situation situation = getItem(position);

        ImageView icon = row.findViewById(R.id.imgSituationIcon);
        TextView title = row.findViewById(R.id.txtSituationTitle);
        TextView description = row.findViewById(R.id.txtSituationDescription);
        TextView delay = row.findViewById(R.id.txtSituationDelay);

        // ---------------------------------------
        // 1. Display road name
        // ---------------------------------------

        String road = safe(situation.getRoadName());

        if (road.isEmpty()) {
            road = "Road location not supplied";
        }

        title.setText(
                road + " – " + safe(situation.getDisplayType())
        );

        // ---------------------------------------
        // 2. Display description
        // ---------------------------------------

        String comment = safe(situation.getComment());

        if (comment.isEmpty()) {
            comment = "No public description supplied";
        }

        description.setText(comment);

        // ---------------------------------------
        // 3. Read traffic situation information
        // ---------------------------------------

        String type = safe(situation.getType());

        String trafficType = safe(situation.getTrafficType());

        String displayType = safe(situation.getDisplayType());

        String normalisedType = normalise(type);

        String normalisedTrafficType = normalise(trafficType);

        String normalisedDisplayType = normalise(displayType);

        String normalisedComment = normalise(comment);

        // ---------------------------------------
        // 4. Reset icon before selecting a new one
        // Important when ListView recycles rows
        // ---------------------------------------

        icon.setVisibility(View.GONE);

        // ---------------------------------------
        // 5. Accident detection
        // Check all available descriptive fields
        // ---------------------------------------

        boolean isAccident =
                containsAccident(normalisedType)
                        || containsAccident(normalisedTrafficType)
                        || containsAccident(normalisedDisplayType)
                        || containsAccident(normalisedComment);

        // ---------------------------------------
        // 6. Vehicle obstruction detection
        // Check both XML type and traffic type
        // ---------------------------------------

        boolean isVehicleObstruction =
                normalisedType.contains("vehicleobstruction")
                        || normalisedTrafficType.contains("vehicleobstruction")
                        || normalisedDisplayType.contains("vehicleobstruction");

        // ---------------------------------------
        // 7. Select the correct traffic icon
        // ---------------------------------------

        if (isAccident) {

            icon.setImageResource(R.drawable.ic_accident);
            icon.setVisibility(View.VISIBLE);

        } else if (isVehicleObstruction) {

            icon.setImageResource(R.drawable.ic_vehicle_obstruction);
            icon.setVisibility(View.VISIBLE);

        } else if (normalisedType.equals("maintenanceworks")) {

            icon.setImageResource(R.drawable.ic_roadworks);
            icon.setVisibility(View.VISIBLE);

        } else if (normalisedType.equals(
                "abnormaltraffic")) {

            icon.setImageResource(R.drawable.ic_incident);
            icon.setVisibility(View.VISIBLE);

        } else if (normalisedType.equals(
                "roadorcarriagewayorlane management".replace(" ", ""))) {

            icon.setImageResource(R.drawable.ic_road_management);
            icon.setVisibility(View.VISIBLE);

        } else if (normalisedType.equals("generalobstruction")) {

            icon.setImageResource(R.drawable.ic_obstruction);
            icon.setVisibility(View.VISIBLE);

        }

        // ---------------------------------------
        // 8. Display delay information
        // ---------------------------------------

        String delayStatus = safe(situation.getDelayValue());

        if (delayStatus.isEmpty()) {
            delayStatus = safe(situation.getDelayType());
        }

        delay.setText(getDelayLabel(delayStatus));

        // ---------------------------------------
        // 9. Keep existing delay colours
        // ---------------------------------------

        delay.setTextColor(
                situation.getDelayColor(delayStatus)
        );

        return row;
    }

    // ---------------------------------------
    // Normalise traffic type strings
    // ---------------------------------------

    private String normalise(String value) {

        return safe(value)
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");
    }

    // ---------------------------------------
    // Identify accident descriptions
    // ---------------------------------------

    private boolean containsAccident(String value) {

        return value.contains("accident")
                || value.contains("collision");
    }

    // ---------------------------------------
    // Display readable delay information
    // ---------------------------------------

    private String getDelayLabel(String delayStatus) {

        if (delayStatus == null || delayStatus.trim().isEmpty()) {
            return "Delay information unavailable";
        }

        String value = delayStatus.trim();

        switch (value) {

            case "longDelays":
                return "Delay: Long delays";

            case "veryLongDelays":
                return "Delay: Very long delays";

            case "shortDelays":
                return "Delay: Short delays";

            case "noDelays":
                return "Delay: No delays";

            case "negligibleDelays":
                return "Delay: Negligible delays";

            case "delaysOfUncertainDuration":
                return "Delay: Delays of uncertain duration";

            default:
                return "Delay: " + value;
        }
    }

    // ---------------------------------------
    // Handle missing XML values safely
    // ---------------------------------------

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
