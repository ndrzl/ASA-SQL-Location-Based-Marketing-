package com.example.myapplication;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ApiClient {

    // ⚠️ Replace with your actual Linux server IP
    private static final String BASE_URL = "http://172.20.10.3:3000";

    public interface PromotionsCallback {
        void onSuccess(List<Promotion> promotions);

        void onError(String error);
    }

    public static void getNearbyPromotions(double lat, double lon, PromotionsCallback callback) {
        new Thread(() -> {
            try {
                // Change radius=5 to radius=500 (500 km) just for testing
                URL url = new URL(BASE_URL + "/api/nearby-promotions?lat=" + lat + "&lon=" + lon + "&radius=500");
                // URL url = new URL(BASE_URL + "/api/nearby-promotions?lat=" + lat + "&lon=" +
                // lon + "&radius=5");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    sb.append(line);
                reader.close();

                JSONObject response = new JSONObject(sb.toString());
                JSONArray arr = response.getJSONArray("promotions");

                List<Promotion> list = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    Promotion p = new Promotion(
                            obj.getString("shop_name"),
                            obj.getString("address"),
                            obj.getString("promo_title"),
                            obj.getString("promo_description"),
                            obj.getString("discount"),
                            obj.getString("valid_until"),
                            obj.getDouble("distance_km"));
                    list.add(p);
                }
                callback.onSuccess(list);

            } catch (Exception e) {
                Log.e("ApiClient", "Error: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public static void sendLocation(String userId, double lat, double lon) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/api/location");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("userId", userId);
                body.put("lat", lat);
                body.put("lon", lon);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.close();
                conn.getResponseCode(); // fire and forget

            } catch (Exception e) {
                Log.e("ApiClient", "Location send error: " + e.getMessage());
            }
        }).start();
    }
}