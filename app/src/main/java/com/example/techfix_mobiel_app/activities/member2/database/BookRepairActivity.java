package com.example.techfix_mobiel_app.activities.member2.database;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member2.database.dao.AppointmentDao;
import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;
import com.example.techfix_mobiel_app.database.AppDatabase;
import com.example.techfix_mobiel_app.network.ApiService;
import com.example.techfix_mobiel_app.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookRepairActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private MaterialCardView selectedDeviceCard = null;
    private MaterialCardView selectedIssueCard = null;
    private String selectedDevice = "";
    private String selectedIssue = "";

    private MaterialButton btnSubmitBooking;
    private EditText etIssueDescription;

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String assignedBranch = "Colombo Branch";

    private AppointmentDao appointmentDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_repair);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchUserLocation();

        btnSubmitBooking = findViewById(R.id.btnSubmitBooking);
        etIssueDescription = findViewById(R.id.etIssueDescription);

        appointmentDao = AppDatabase.getInstance(this).appointmentDao();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupDeviceChip(R.id.chipSmartphone, "Smartphone");
        setupDeviceChip(R.id.chipLaptop, "Laptop");
        setupDeviceChip(R.id.chipTablet, "Tablet");
        setupDeviceChip(R.id.chipDesktop, "Desktop");
        setupDeviceChip(R.id.chipConsole, "Gaming Console");
        setupDeviceChip(R.id.chipDeviceOther, "Other");

        setupIssueChip(R.id.chipScreen, "Screen Damage");
        setupIssueChip(R.id.chipBattery, "Battery Issue");
        setupIssueChip(R.id.chipWater, "Water Damage");
        setupIssueChip(R.id.chipSoftware, "Software Problem");
        setupIssueChip(R.id.chipCharging, "Charging Issue");
        setupIssueChip(R.id.chipSpeaker, "Speaker / Mic");
        setupIssueChip(R.id.chipCamera, "Camera Damage");
        setupIssueChip(R.id.chipIssueOther, "Other");

        btnSubmitBooking.setOnClickListener(v -> {
            if (selectedDevice.isEmpty() || selectedIssue.isEmpty()) {
                Toast.makeText(this, "Please select both a device type and issue type", Toast.LENGTH_SHORT).show();
                return;
            }

            String notes = etIssueDescription.getText() != null ? etIssueDescription.getText().toString().trim() : "";
            String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

            SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
            int customerId = prefs.getInt("logged_customer_id", 1);
            String customerName = prefs.getString("logged_username", "Customer User");

            int assignedBranchId = assignedBranch.contains("Galle") ? 2 : 1;
            int serviceId = 1;
            int assignedTechId = 1;

            AppointmentEntity appointment = new AppointmentEntity(
                    customerId,
                    serviceId,
                    assignedBranchId,
                    assignedTechId,
                    selectedDevice,
                    selectedIssue,
                    notes,
                    assignedBranch,
                    "Pending",
                    "Unpaid",
                    currentDate,
                    0.0,
                    customerName
            );

            Executors.newSingleThreadExecutor().execute(() -> {
                // 1. Save locally to SQLite/Room so local UI views update instantly
                appointmentDao.insert(appointment);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Booking confirmed! Auto-routed to: " + assignedBranch, Toast.LENGTH_LONG).show();
                    finish();
                });

                // 2. Sync to MySQL server in the background via Retrofit
                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                apiService.createAppointment(appointment).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        // Successfully synced to server
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e("API_ERROR", "Server sync failed: " + t.getMessage());
                    }
                });
            });
        });
    }

    private void setupDeviceChip(int cardId, String deviceName) {
        MaterialCardView card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> {
                if (selectedDeviceCard != null) {
                    selectedDeviceCard.setCardBackgroundColor(android.graphics.Color.parseColor("#161618"));
                    selectedDeviceCard.setStrokeColor(android.graphics.Color.parseColor("#333333"));
                }
                selectedDeviceCard = card;
                selectedDeviceCard.setCardBackgroundColor(android.graphics.Color.parseColor("#261C33"));
                selectedDeviceCard.setStrokeColor(android.graphics.Color.parseColor("#BB86FC"));
                selectedDevice = deviceName;
                updateButtonState();
            });
        }
    }

    private void setupIssueChip(int cardId, String issueName) {
        MaterialCardView card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> {
                if (selectedIssueCard != null) {
                    selectedIssueCard.setCardBackgroundColor(android.graphics.Color.parseColor("#161618"));
                    selectedIssueCard.setStrokeColor(android.graphics.Color.parseColor("#333333"));
                }
                selectedIssueCard = card;
                selectedIssueCard.setCardBackgroundColor(android.graphics.Color.parseColor("#261C33"));
                selectedIssueCard.setStrokeColor(android.graphics.Color.parseColor("#BB86FC"));
                selectedIssue = issueName;
                updateButtonState();
            });
        }
    }

    private void updateButtonState() {
        if (!selectedDevice.isEmpty() && !selectedIssue.isEmpty()) {
            btnSubmitBooking.setText("Confirm Booking (" + assignedBranch + ")");
            btnSubmitBooking.setTextColor(android.graphics.Color.parseColor("#000000"));
            btnSubmitBooking.setBackgroundColor(android.graphics.Color.parseColor("#BB86FC"));
        }
    }

    private void fetchUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    assignedBranch = calculateNearestBranch(currentLatitude, currentLongitude);
                } else {
                    assignedBranch = "TechFix - Colombo Branch";
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation();
            } else {
                assignedBranch = "TechFix - Colombo Branch";
            }
        }
    }

    String calculateNearestBranch(double lat, double lng) {
        if (lat < 6.3) {
            return "TechFix - Galle Branch";
        } else {
            return "TechFix - Colombo Branch";
        }
    }
}