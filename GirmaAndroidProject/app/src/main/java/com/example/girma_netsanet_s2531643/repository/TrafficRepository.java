
package com.example.girma_netsanet_s2531643.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.example.girma_netsanet_s2531643.BuildConfig;
import com.example.girma_netsanet_s2531643.model.Situation;
import com.example.girma_netsanet_s2531643.parser.TrafficPullParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrafficRepository {

    public interface DataCallback {
        void onSuccess(List<Situation> situations, String message);
        void onError(String message);
    }

    private static final String TAG = "TrafficRepository";

    private static final String PREFS = "traffic_cache";

    private static final String CACHE_UNPLANNED = "unplanned_xml";
    private static final String CACHE_ROADWORKS = "roadworks_xml";

    private static final String ASSET_UNPLANNED =
            "unplannedContentExample.xml";

    private static final String ASSET_ROADWORKS =
            "roadworksContentExample.xml";

    private final Context context;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final TrafficPullParser parser =
            new TrafficPullParser();

    public TrafficRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    // Refresh traffic information
    public void refresh(DataCallback callback) {

        executor.execute(() -> {

            // FIRST: Try online Traffic Scotland XML
            try {

                Log.d(TAG, "Trying online traffic information");

                String unplannedXml =
                        download(BuildConfig.TRAFFIC_UNPLANNED_URL);

                String roadworksXml =
                        download(BuildConfig.TRAFFIC_ROADWORKS_URL);

                List<Situation> all = new ArrayList<>();

                all.addAll(parser.parse(unplannedXml));
                all.addAll(parser.parse(roadworksXml));

                saveCache(unplannedXml, roadworksXml);

                Log.d(TAG, "Online traffic information loaded");

                callback.onSuccess(
                        all,
                        "Online traffic information updated"
                );

                return;

            } catch (Exception networkError) {

                Log.e(TAG, "Online refresh failed", networkError);
            }

            // SECOND: Try previously saved XML cache
            try {

                Log.d(TAG, "Trying saved traffic cache");

                List<Situation> cached = loadCachedData();

                if (!cached.isEmpty()) {

                    callback.onSuccess(
                            cached,
                            "Showing saved traffic information - offline"
                    );

                    return;
                }

            } catch (Exception cacheError) {

                Log.e(TAG, "Cache loading failed", cacheError);
            }

            // THIRD: Try bundled offline XML assets
            try {

                Log.d(TAG, "Trying offline XML assets");

                List<Situation> offlineData = loadAssetData();

                if (!offlineData.isEmpty()) {

                    callback.onSuccess(
                            offlineData,
                            "Showing offline sample traffic information"
                    );

                    return;
                }

            } catch (Exception assetError) {

                Log.e(TAG, "Assets loading failed", assetError);
            }

            // FOURTH: All sources failed
            callback.onError(
                    "Traffic information unavailable. " +
                            "Online, cached and offline XML data could not be loaded."
            );
        });
    }

    // Download online XML
    private String download(String address) throws Exception {

        HttpURLConnection connection = null;

        try {

            URL url = new URL(address);

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);

            String credentials =
                    BuildConfig.TRAFFIC_CLIENT_ID + ":" +
                            BuildConfig.TRAFFIC_CLIENT_KEY;

            String encoded = Base64.encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8),
                    Base64.NO_WRAP
            );

            connection.setRequestProperty(
                    "Authorization",
                    encoded
            );

            connection.setRequestProperty(
                    "Accept",
                    "application/xml"
            );

            int responseCode = connection.getResponseCode();

            if (responseCode < 200 || responseCode >= 300) {

                throw new Exception(
                        "HTTP error: " + responseCode
                );
            }

            String xml = readStream(connection.getInputStream());

            if (xml.trim().isEmpty()) {

                throw new Exception(
                        "The server returned an empty XML response"
                );
            }

            return xml;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Read XML from an input stream
    private String readStream(InputStream stream) throws Exception {

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        stream,
                        StandardCharsets.UTF_8
                ))) {

            String line;

            while ((line = reader.readLine()) != null) {

                result.append(line).append('\n');
            }
        }

        return result.toString();
    }

    // Save downloaded XML in SharedPreferences
    private void saveCache(String unplanned, String roadworks) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(CACHE_UNPLANNED, unplanned)
                .putString(CACHE_ROADWORKS, roadworks)
                .apply();

        Log.d(TAG, "Traffic XML cache saved");
    }

    // Load previously saved XML independently
    private List<Situation> loadCachedData() {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );

        String unplanned =
                preferences.getString(CACHE_UNPLANNED, "");

        String roadworks =
                preferences.getString(CACHE_ROADWORKS, "");

        List<Situation> all = new ArrayList<>();

        if (unplanned != null && !unplanned.trim().isEmpty()) {

            try {

                all.addAll(parser.parse(unplanned));

            } catch (Exception e) {

                Log.e(TAG, "Cached unplanned XML failed", e);
            }
        }

        if (roadworks != null && !roadworks.trim().isEmpty()) {

            try {

                all.addAll(parser.parse(roadworks));

            } catch (Exception e) {

                Log.e(TAG, "Cached roadworks XML failed", e);
            }
        }

        Log.d(TAG, "Cached traffic records: " + all.size());

        return all;
    }

    // Load both bundled XML files independently
    private List<Situation> loadAssetData() throws Exception {

        List<Situation> all = new ArrayList<>();

        // Try unplanned traffic XML
        try {

            Log.d(TAG, "Loading unplanned XML asset");

            List<Situation> unplanned =
                    parser.parseAsset(context, ASSET_UNPLANNED);

            all.addAll(unplanned);

            Log.d(
                    TAG,
                    "Unplanned asset records: " + unplanned.size()
            );

        } catch (Exception e) {

            Log.e(TAG, "Unplanned XML asset failed", e);
        }

        // Try roadworks XML even if unplanned XML failed
        try {

            Log.d(TAG, "Loading roadworks XML asset");

            List<Situation> roadworks =
                    parser.parseAsset(context, ASSET_ROADWORKS);

            all.addAll(roadworks);

            Log.d(
                    TAG,
                    "Roadworks asset records: " + roadworks.size()
            );

        } catch (Exception e) {

            Log.e(TAG, "Roadworks XML asset failed", e);
        }

        Log.d(
                TAG,
                "Total offline asset records: " + all.size()
        );

        if (all.isEmpty()) {

            throw new Exception(
                    "Neither offline XML asset produced any traffic records. " +
                            "Check asset filenames, XML contents and parser errors in Logcat."
            );
        }

        return all;
    }
}
