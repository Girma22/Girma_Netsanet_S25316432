package com.example.girma_netsanet_s2531643.util;

import android.location.Location;

import com.example.girma_netsanet_s2531643.model.Situation;

public class DistanceFilter {

    // Fixed Glasgow reference location
    public static final double GLASGOW_LAT = 55.8642;
    public static final double GLASGOW_LON = -4.2518;

    /**
     * Find the nearest available endpoint of a situation.
     * Returns distance in kilometres.
     */
    public static double getNearestDistanceKm(
            Situation situation,
            double currentLatitude,
            double currentLongitude) {

        if (situation == null) {
            return Double.MAX_VALUE;
        }

        double nearestDistance = Double.MAX_VALUE;

        // Check starting coordinates
        if (situation.hasFromPoint()) {

            double distance = calculateDistance(
                    currentLatitude,
                    currentLongitude,
                    situation.getFromLat(),
                    situation.getFromLon()
            );

            nearestDistance = Math.min(
                    nearestDistance, distance
            );
        }

        // Check ending coordinates
        if (situation.hasToPoint()) {

            double distance = calculateDistance(
                    currentLatitude,
                    currentLongitude,
                    situation.getToLat(),
                    situation.getToLon()
            );

            nearestDistance = Math.min(
                    nearestDistance, distance
            );
        }

        // No valid coordinates: exclude the situation
        return nearestDistance;
    }

    /**
     * Calculate distance between two GPS coordinates.
     * Android returns the distance in metres.
     */
    public static double calculateDistance(
            double startLatitude,
            double startLongitude,
            double endLatitude,
            double endLongitude) {

        float[] results = new float[1];

        Location.distanceBetween(
                startLatitude,
                startLongitude,
                endLatitude,
                endLongitude,
                results
        );

        return results[0] / 1000.0;
    }
}
