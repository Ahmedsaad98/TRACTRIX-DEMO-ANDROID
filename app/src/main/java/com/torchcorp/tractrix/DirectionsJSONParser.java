package com.torchcorp.tractrix;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsJSONParser {

    // Parse the JSON response and return a list of routes
    public List<List<HashMap<String, String>>> parse(JSONObject jsonObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<>();

        try {
            JSONArray jsonRoutes = jsonObject.getJSONArray("routes");

            // Traverse all routes
            for (int i = 0; i < jsonRoutes.length(); i++) {
                JSONArray jsonLegs = ((JSONObject) jsonRoutes.get(i)).getJSONArray("legs");

                List<HashMap<String, String>> path = new ArrayList<>();

                // Traverse all legs
                for (int j = 0; j < jsonLegs.length(); j++) {
                    JSONArray jsonSteps = ((JSONObject) jsonLegs.get(j)).getJSONArray("steps");

                    // Traverse all steps
                    for (int k = 0; k < jsonSteps.length(); k++) {
                        String polyline = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> decodedPolyline = decodePolyline(polyline);

                        // Traverse all points in the decoded polyline
                        for (LatLng point : decodedPolyline) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString(point.latitude));
                            hm.put("lng", Double.toString(point.longitude));
                            path.add(hm);
                        }
                    }
                }

                routes.add(path);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return routes;
    }

    // Decode the polyline string into a list of LatLng points
    private List<LatLng> decodePolyline(String encodedPolyline) {
        List<LatLng> polyPoints = new ArrayList<>();
        int index = 0;
        int len = encodedPolyline.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;

            do {
                b = encodedPolyline.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;

            do {
                b = encodedPolyline.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng point = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            polyPoints.add(point);
        }

        return polyPoints;
    }
}
