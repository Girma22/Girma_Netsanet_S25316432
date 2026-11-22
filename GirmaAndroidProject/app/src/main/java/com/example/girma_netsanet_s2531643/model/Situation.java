package com.example.girma_netsanet_s2531643.model;

import java.io.Serializable;
import android.graphics.Color;

public class Situation implements Serializable {

    private static final long serialVersionUID = 1L;

    // Basic situation information
    private String id = "";
    private String type = "";
    private String comment = "";
    private String direction = "";
    private String roadName = "";
    private String source = "";
    private String startTime = "";

    // Traffic delay information
    private String delayType = "";
    private String delayValue = "";

    // Situation categories
    private String maintenanceType = "";
    private String trafficType = "";
    private String managementType = "";
    private String laneImpact = "";

    // Location coordinates
    // NaN means that coordinates have not been provided
    private double fromLat = Double.NaN;
    private double fromLon = Double.NaN;
    private double toLat = Double.NaN;
    private double toLon = Double.NaN;


    // ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = safe(id);
    }


    // Situation type
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = safe(type);
    }


    // Comment / description
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = safe(comment);
    }


    // Direction
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = safe(direction);
    }


    // Road name
    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = safe(roadName);
    }


    // Source
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = safe(source);
    }


    // Start time
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = safe(startTime);
    }


    // Delay type
    public String getDelayType() {
        return delayType;
    }

    public void setDelayType(String delayType) {
        this.delayType = safe(delayType);
    }


    // Delay value
    public String getDelayValue() {
        return delayValue;
    }

    public void setDelayValue(String delayValue) {
        this.delayValue = safe(delayValue);
    }


    // Maintenance type
    public String getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(String value) {
        this.maintenanceType = safe(value);
    }


    // Traffic type
    public String getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(String value) {
        this.trafficType = safe(value);
    }


    // Management type
    public String getManagementType() {
        return managementType;
    }

    public void setManagementType(String value) {
        this.managementType = safe(value);
    }


    // Lane impact
    public String getLaneImpact() {
        return laneImpact;
    }

    public void setLaneImpact(String value) {
        this.laneImpact = safe(value);
    }


    // Starting latitude
    public double getFromLat() {
        return fromLat;
    }

    public void setFromLat(double value) {
        fromLat = value;
    }


    // Starting longitude
    public double getFromLon() {
        return fromLon;
    }

    public void setFromLon(double value) {
        fromLon = value;
    }


    // Ending latitude
    public double getToLat() {
        return toLat;
    }

    public void setToLat(double value) {
        toLat = value;
    }


    // Ending longitude
    public double getToLon() {
        return toLon;
    }

    public void setToLon(double value) {
        toLon = value;
    }


    // Check whether starting coordinates exist
    public boolean hasFromPoint() {
        return isValidCoordinate(fromLat, fromLon);
    }


    // Check whether ending coordinates exist
    public boolean hasToPoint() {
        return isValidCoordinate(toLat, toLon);
    }


    // Check whether at least one location exists
    public boolean hasAnyLocation() {
        return hasFromPoint() || hasToPoint();
    }


    // Validate latitude and longitude
    private boolean isValidCoordinate(
            double latitude,
            double longitude) {

        return Double.isFinite(latitude)
                && Double.isFinite(longitude)
                && latitude >= -90
                && latitude <= 90
                && longitude >= -180
                && longitude <= 180;
    }


    // Display a readable situation type
    public String getDisplayType() {

        if (type == null || type.trim().isEmpty()) {
            return "Road situation";
        }
        if (trafficType.toLowerCase(java.util.Locale.ROOT)
                .contains("vehicleobstruction")) {
            return "Vehicle obstruction";
        }

        if (type.contains("AbnormalTraffic")) {
            return "Abnormal traffic";
        }

        if (type.contains("MaintenanceWorks")) {
            return "Roadworks";
        }

        if (type.contains("RoadOrCarriagewayOrLaneManagement")) {
            return "Road management";
        }

        return type;
    }


    // Prevent null values and remove unnecessary spaces
    private String safe(String value) {
        return value == null ? "" : value.trim();
    }


    // Determine delay colour
    public int getDelayColor(String delayStatus) {

        if (delayStatus == null || delayStatus.trim().isEmpty()) {
            return Color.GRAY;
        }

        // Convert delay value into a standard format
        String delay = delayStatus.trim()
                .replace(" ", "")
                .toLowerCase(java.util.Locale.ROOT);

        // Very long delays = RED
        if (delay.contains("verylongdelays")) {
            return Color.RED;
        }

        // Long delays = RED
        if (delay.contains("longdelays")) {
            return Color.RED;
        }

        // Short delays = ORANGE
        if (delay.contains("shortdelays")) {
            return Color.rgb(255, 165, 0);
        }

        // Normal delays = ORANGE
        if ("delays".equals(delay)) {
            return Color.rgb(255, 165, 0);
        }
        // No delays or negligible delays = GREEN
        if (delay.contains("nodelays")
                || delay.contains("negligibledelays")) {
            return Color.GREEN;
        }

        // Unknown information = GREY
        return Color.GRAY;
    }

}
