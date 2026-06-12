package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView latLonText;
    private LinearLayout promotionsContainer;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private double currentLat = 0, currentLon = 0;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) { allGranted = false; break; }
                }
                if (allGranted) { checkLocationSettings(); }
            });

    private final ActivityResultLauncher<IntentSenderRequest> gpsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    startTrackingService();
                    startLocalLocationUpdates();
                } else {
                    Toast.makeText(this, "Location must be enabled.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        latLonText = findViewById(R.id.latLonText);
        promotionsContainer = findViewById(R.id.promotionsContainer);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ImageButton btnEat   = findViewById(R.id.btnEat);
        ImageButton btnPlay  = findViewById(R.id.btnPlay);
        ImageButton btnVisit = findViewById(R.id.btnVisit);
        View btnSettings     = findViewById(R.id.btnSettings);

        btnEat.setOnClickListener(v -> fetchAndDisplayPromotions());
        btnPlay.setOnClickListener(v -> fetchAndDisplayPromotions());
        btnVisit.setOnClickListener(v -> fetchAndDisplayPromotions());

        btnSettings.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HomeActivity.this, btnSettings);
            popup.getMenu().add("View Profile");
            popup.getMenu().add("Logout");
            popup.setOnMenuItemClickListener(item -> {
                if ("View Profile".equals(item.getTitle())) {
                    startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                } else if ("Logout".equals(item.getTitle())) {
                    getSharedPreferences("AppSession", MODE_PRIVATE)
                            .edit().remove("currentUser").apply();
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
                return true;
            });
            popup.show();
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                android.location.Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLon = location.getLongitude();

                    latLonText.setText("Lat: " + String.format("%.4f", currentLat)
                            + ", Lon: " + String.format("%.4f", currentLon));

                    SharedPreferences pref = getSharedPreferences("AppSession", MODE_PRIVATE);
                    String userId = pref.getString("currentUser", "unknown");
                    ApiClient.sendLocation(userId, currentLat, currentLon);

                    fetchAndDisplayPromotions();
                }
            }
        };

        checkLocationRequirements();
    }

    private void fetchAndDisplayPromotions() {
        if (currentLat == 0 && currentLon == 0) {
            Toast.makeText(this, "Waiting for location...", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getNearbyPromotions(currentLat, currentLon, new ApiClient.PromotionsCallback() {
            @Override
            public void onSuccess(List<Promotion> promotions) {
                runOnUiThread(() -> displayPromotions(promotions));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(HomeActivity.this, "Failed to load promos: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void displayPromotions(List<Promotion> promotions) {
        promotionsContainer.removeAllViews();

        if (promotions.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No promotions nearby.");
            promotionsContainer.addView(empty);
            return;
        }

        for (Promotion p : promotions) {
            View card = getLayoutInflater().inflate(R.layout.item_promotion, promotionsContainer, false);

            ((TextView) card.findViewById(R.id.tvShopName)).setText(p.shopName);
            ((TextView) card.findViewById(R.id.tvPromoTitle)).setText(p.promoTitle);
            ((TextView) card.findViewById(R.id.tvPromoDesc)).setText(p.promoDescription);
            ((TextView) card.findViewById(R.id.tvDiscount)).setText(p.discount);
            ((TextView) card.findViewById(R.id.tvDistance)).setText(String.format("%.1f km away", p.distanceKm));
            ((TextView) card.findViewById(R.id.tvValidUntil)).setText("Valid until: " + p.validUntil);

            promotionsContainer.addView(card);
        }
    }

    private void checkLocationRequirements() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        boolean allGranted = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) { checkLocationSettings(); }
        else { requestPermissionLauncher.launch(permissions); }
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            startTrackingService();
            startLocalLocationUpdates();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    IntentSenderRequest intentSenderRequest =
                            new IntentSenderRequest.Builder(resolvable.getResolution()).build();
                    gpsLauncher.launch(intentSenderRequest);
                } catch (Exception sendEx) {
                    // Ignore
                }
            }
        });
    }

    private void startLocalLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10000).build();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }

    private void startTrackingService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}