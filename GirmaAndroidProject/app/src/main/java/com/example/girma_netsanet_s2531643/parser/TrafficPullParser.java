package com.example.girma_netsanet_s2531643.parser;

import android.content.Context;
import android.util.Xml;

import com.example.girma_netsanet_s2531643.model.Situation;
import com.example.girma_netsanet_s2531643.util.AssetXmlReader;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class TrafficPullParser {

    private static final String XSI_NAMESPACE =
            "http://www.w3.org/2001/XMLSchema-instance";


    // 1. Parse XML received from Traffic Scotland
    public List<Situation> parse(String xml) throws Exception {

        List<Situation> situations = new ArrayList<>();

        if (xml == null || xml.trim().isEmpty()) {
            throw new Exception("Traffic Scotland returned empty XML.");
        }

        XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(
                XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                true
        );

        parser.setInput(new StringReader(xml));

        Situation current = null;

        boolean insideComment = false;
        boolean insideLocationName = false;

        String locationSide = "";

        int event = parser.getEventType();

        while (event != XmlPullParser.END_DOCUMENT) {

            // START TAG
            if (event == XmlPullParser.START_TAG) {

                String tag = parser.getName();

                // Start a new traffic situation
                if ("situationRecord".equals(tag)) {

                    current = new Situation();

                    current.setType(getRecordType(parser));
                    current.setId(getAttribute(parser, "id"));

                    insideComment = false;
                    insideLocationName = false;
                    locationSide = "";
                }

                else if (current != null) {

                    // General public comment
                    if ("generalPublicComment".equals(tag)) {
                        insideComment = true;
                    }

                    // Starting location
                    else if ("from".equals(tag)) {
                        locationSide = "from";
                    }

                    // Ending location
                    else if ("to".equals(tag)) {
                        locationSide = "to";
                    }

                    // Road location name
                    else if ("name".equals(tag)) {
                        insideLocationName = true;
                    }

                    // Extract text values
                    else if ("value".equals(tag)) {

                        String value = readElementText(parser);

                        if (insideComment) {

                            if (!value.isEmpty()) {
                                current.setComment(value);
                            }
                        }

                        else if (insideLocationName
                                && !locationSide.isEmpty()
                                && current.getRoadName().isEmpty()) {

                            if (!value.isEmpty()) {
                                current.setRoadName(value);
                            }
                        }
                    }

                    // Source identification
                    else if ("sourceIdentification".equals(tag)) {

                        current.setSource(
                                readElementText(parser)
                        );
                    }

                    // Situation start time
                    else if ("overallStartTime".equals(tag)) {

                        current.setStartTime(
                                readElementText(parser)
                        );
                    }

                    // Traffic delay type
                    else if ("delaysType".equals(tag)) {

                        String delay = readElementText(parser);

                        current.setDelayType(delay);
                        current.setDelayValue(formatDelay(delay));
                    }

                    // Traffic direction
                    else if ("tpegDirection".equals(tag)) {

                        current.setDirection(
                                readElementText(parser)
                        );
                    }

                    // Road maintenance information
                    else if ("roadMaintenanceType".equals(tag)) {

                        current.setMaintenanceType(
                                readElementText(parser)
                        );
                    }

                    // Abnormal traffic information
                    else if ("abnormalTrafficType".equals(tag)) {

                        current.setTrafficType(
                                readElementText(parser)
                        );
                    }

                    // Road management information
                    else if ("roadOrCarriagewayOrLaneManagementType".equals(tag)) {

                        current.setManagementType(
                                readElementText(parser)
                        );
                    }

                    // Operational lanes
                    else if ("numberOfOperationalLanes".equals(tag)) {

                        current.setLaneImpact(
                                "Operational lanes: " +
                                        readElementText(parser)
                        );
                    }

                    // Restricted lanes
                    else if ("numberOfLanesRestricted".equals(tag)) {

                        current.setLaneImpact(
                                "Restricted lanes: " +
                                        readElementText(parser)
                        );
                    }

                    // Starting or ending latitude
                    else if ("latitude".equals(tag)) {

                        setLatitude(
                                current,
                                locationSide,
                                readElementText(parser)
                        );
                    }

                    // Starting or ending longitude
                    else if ("longitude".equals(tag)) {

                        setLongitude(
                                current,
                                locationSide,
                                readElementText(parser)
                        );
                    }
                }
            }

            // END TAG
            else if (event == XmlPullParser.END_TAG) {

                String tag = parser.getName();

                if ("generalPublicComment".equals(tag)) {
                    insideComment = false;
                }

                else if ("name".equals(tag)) {
                    insideLocationName = false;
                }

                else if ("from".equals(tag) || "to".equals(tag)) {
                    locationSide = "";
                }

                // Finish a traffic situation
                else if ("situationRecord".equals(tag)
                        && current != null) {

                    situations.add(current);
                    current = null;
                }
            }

            event = parser.next();
        }

        return situations;
    }


    // 2. Read XML text safely
    private String readElementText(
            XmlPullParser parser
    ) throws Exception {

        StringBuilder result = new StringBuilder();

        int depth = 1;

        while (depth > 0) {

            int event = parser.next();

            if (event == XmlPullParser.START_TAG) {
                depth++;
            }

            else if (event == XmlPullParser.END_TAG) {
                depth--;
            }

            else if (event == XmlPullParser.TEXT
                    || event == XmlPullParser.CDSECT) {

                result.append(parser.getText());
            }

            else if (event == XmlPullParser.END_DOCUMENT) {

                throw new Exception(
                        "Unexpected end of XML while reading an element."
                );
            }
        }

        return result.toString().trim();
    }


    // 3. Convert delay information into readable text
    private String formatDelay(String delay) {

        if (delay == null || delay.trim().isEmpty()) {
            return "";
        }

        switch (delay.trim()) {

            case "longDelays":
                return "Long delays";

            case "veryLongDelays":
                return "Very long delays";

            case "shortDelays":
                return "Short delays";

            case "noDelays":
                return "No delays";

            case "negligibleDelays":
                return "Negligible delays";

            case "delaysOfUncertainDuration":
                return "Delays of uncertain duration";

            default:
                return delay;
        }
    }


    // 4. Parse local XML asset
    public List<Situation> parseAsset(
            Context context,
            String assetFileName
    ) throws Exception {

        String xml = AssetXmlReader.readXml(
                context,
                assetFileName
        );

        return parse(xml);
    }


    // 5. Extract situation record type
    private String getRecordType(XmlPullParser parser) {

        String type = parser.getAttributeValue(
                XSI_NAMESPACE,
                "type"
        );

        if (type != null) {
            return type;
        }

        for (int i = 0; i < parser.getAttributeCount(); i++) {

            if ("type".equals(parser.getAttributeName(i))) {
                return parser.getAttributeValue(i);
            }
        }

        return "";
    }


    // 6. Extract XML attribute
    private String getAttribute(
            XmlPullParser parser,
            String name
    ) {

        for (int i = 0; i < parser.getAttributeCount(); i++) {

            if (name.equals(parser.getAttributeName(i))) {
                return parser.getAttributeValue(i);
            }
        }

        return "";
    }


    // 7. Set starting or ending latitude
    private void setLatitude(
            Situation situation,
            String side,
            String value
    ) {

        try {

            double coordinate = Double.parseDouble(value.trim());

            // Validate latitude
            if (!Double.isFinite(coordinate)
                    || coordinate < -90
                    || coordinate > 90) {
                return;
            }

            if ("from".equals(side)) {
                situation.setFromLat(coordinate);
            }

            else if ("to".equals(side)) {
                situation.setToLat(coordinate);
            }

        } catch (NumberFormatException ignored) {
            // Ignore invalid coordinates
        }
    }


    // 8. Set starting or ending longitude
    private void setLongitude(
            Situation situation,
            String side,
            String value
    ) {

        try {

            double coordinate = Double.parseDouble(value.trim());

            // Validate longitude
            if (!Double.isFinite(coordinate)
                    || coordinate < -180
                    || coordinate > 180) {
                return;
            }

            if ("from".equals(side)) {
                situation.setFromLon(coordinate);
            }

            else if ("to".equals(side)) {
                situation.setToLon(coordinate);
            }

        } catch (NumberFormatException ignored) {
            // Ignore invalid coordinates
        }
    }

}
