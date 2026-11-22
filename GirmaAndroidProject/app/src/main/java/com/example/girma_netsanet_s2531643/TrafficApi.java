package com.example.girma_netsanet_s2531643;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrafficApi {

    // Background thread for downloading XML
    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    // Main thread handler for returning results to the UI
    private static final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    // Callback to return downloaded XML or an error
    public interface DownloadCallback {
        void onSuccess(String xml);
        void onError(String error);
    }

    // Download unplanned traffic events
    public static void downloadUnplannedEvents(
            DownloadCallback callback) {

        downloadXml(
                BuildConfig.TRAFFIC_UNPLANNED_URL,
                callback
        );
    }

    // Download current roadworks
    public static void downloadCurrentRoadworks(
            DownloadCallback callback) {

        downloadXml(
                BuildConfig.TRAFFIC_ROADWORKS_URL,
                callback
        );
    }

    // Common XML downloading method
    private static void downloadXml(
            String urlString,
            DownloadCallback callback) {

        if (callback == null) {
            return;
        }

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                // Check URL
                if (urlString == null || urlString.trim().isEmpty()) {
                    throw new IOException("Traffic XML URL is missing.");
                }

                // Open connection
                URL url = new URL(urlString);

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoInput(true);

                connection.setRequestProperty(
                        "Accept",
                        "application/xml, text/xml"
                );

                // Get server response
                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException(
                            "HTTP Error: " + responseCode
                    );
                }

                // Read XML response
                StringBuilder xml = new StringBuilder();

                try (
                        InputStream input = connection.getInputStream();

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                        input,
                                        StandardCharsets.UTF_8
                                )
                        )
                ) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        xml.append(line).append('\n');
                    }
                }

                // Check that XML was received
                if (xml.length() == 0) {
                    throw new IOException("The server returned empty XML.");
                }

                // Return successful result on the main thread
                String downloadedXml = xml.toString();

                mainHandler.post(() ->
                        callback.onSuccess(downloadedXml)
                );

            } catch (Exception e) {

                // Return error on the main thread
                String errorMessage =
                        "Download failed: " + e.getMessage();

                mainHandler.post(() ->
                        callback.onError(errorMessage)
                );

            } finally {

                // Close connection
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // Shut down the executor when it is no longer needed
    public static void shutdown() {
        executor.shutdown();
    }
}
