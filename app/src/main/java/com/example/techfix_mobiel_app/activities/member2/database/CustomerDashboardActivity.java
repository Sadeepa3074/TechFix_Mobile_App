package com.example.techfix_mobiel_app.activities.member2.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member1.database.LoginActivity;
import com.example.techfix_mobiel_app.activities.member1.database.ProfileActivity;
import com.example.techfix_mobiel_app.activities.member2.database.dao.AppointmentDao;
import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;
import com.example.techfix_mobiel_app.activities.member3.database.AboutUsActivity;
import com.example.techfix_mobiel_app.activities.member3.database.CustomerSparePartsActivity;
import com.example.techfix_mobiel_app.database.AppDatabase;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.concurrent.Executors;

public class CustomerDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private AppointmentDao appointmentDao;
    private String loggedInUsername;
    private int activeBookingId = -1;

    private TextView tvDeviceName, tvIssueDesc, tvSideMenuUserName, tvUserInitials, tvDashActiveStatus;
    private MaterialCardView cardActiveRepair;
    private ImageView dashStep1, dashStep2, dashStep3, dashStep4;
    private View dashLine1, dashLine2, dashLine3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        appointmentDao = AppDatabase.getInstance(this).appointmentDao();

        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
        loggedInUsername = prefs.getString("logged_username", "User");

        // UI Bindings for Dashboard Cards & Drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvIssueDesc = findViewById(R.id.tvIssueDesc);
        tvSideMenuUserName = findViewById(R.id.tvSideMenuUserName);
        tvUserInitials = findViewById(R.id.tvUserInitials);

        cardActiveRepair = findViewById(R.id.cardActiveRepair);
        tvDashActiveStatus = findViewById(R.id.tvActiveLabel);

        dashStep1 = findViewById(R.id.dashStep1);
        dashStep2 = findViewById(R.id.dashStep2);
        dashStep3 = findViewById(R.id.dashStep3);
        dashStep4 = findViewById(R.id.dashStep4);
        dashLine1 = findViewById(R.id.dashLine1);
        dashLine2 = findViewById(R.id.dashLine2);
        dashLine3 = findViewById(R.id.dashLine3);

        tvSideMenuUserName.setText(loggedInUsername);
        if (!loggedInUsername.isEmpty()) {
            tvUserInitials.setText(loggedInUsername.substring(0, Math.min(2, loggedInUsername.length())).toUpperCase());
        }

        // Top Menu & Profile Actions
        findViewById(R.id.btnMenu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        findViewById(R.id.btnCloseMenu).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        findViewById(R.id.btnProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Main Dashboard Card Interactions
        findViewById(R.id.cardBookRepair).setOnClickListener(v -> startActivity(new Intent(this, BookRepairActivity.class)));
        findViewById(R.id.cardTrack).setOnClickListener(v -> startActivity(new Intent(this, TrackStatusActivity.class)));
        findViewById(R.id.cardHistory).setOnClickListener(v -> startActivity(new Intent(this, RepairHistoryActivity.class)));

        View cardSpareParts = findViewById(R.id.cardSpareParts);
        if (cardSpareParts != null) {
            cardSpareParts.setOnClickListener(v -> startActivity(new Intent(this, CustomerSparePartsActivity.class)));
        }

        // Active Repair Card: Tapping it opens the quotation/acceptance detail view
        if (cardActiveRepair != null) {
            cardActiveRepair.setOnClickListener(v -> {
                if (activeBookingId != -1) {
                    Intent intent = new Intent(this, CustomerBookingDetailActivity.class);
                    intent.putExtra("BOOKING_ID", activeBookingId);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No active repair to view", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Side Navigation Menu Clicks
        findViewById(R.id.nav_home).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        findViewById(R.id.nav_book_repair).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, BookRepairActivity.class));
        });
        findViewById(R.id.nav_track_status).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, TrackStatusActivity.class));
        });
        findViewById(R.id.nav_history).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, RepairHistoryActivity.class));
        });
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, ProfileActivity.class));
        });
        findViewById(R.id.nav_about_us).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AboutUsActivity.class));
        });
        findViewById(R.id.nav_sign_out).setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveBookingSummary();
    }

    private void loadActiveBookingSummary() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<AppointmentEntity> bookings = appointmentDao.getAppointmentsByCustomerName(loggedInUsername);

            AppointmentEntity latestActive = null;
            if (bookings != null) {
                for (AppointmentEntity b : bookings) {
                    String status = b.status != null ? b.status : "";
                    // Filter out completed, ready, rejected, cancelled, or collected statuses so inactive/rejected items never hijack the card
                    if (!status.equalsIgnoreCase("Completed") &&
                            !status.equalsIgnoreCase("Ready") &&
                            !status.equalsIgnoreCase("Rejected") &&
                            !status.equalsIgnoreCase("Cancelled") &&
                            !status.equalsIgnoreCase("Collected")) {
                        latestActive = b;
                        break;
                    }
                }
            }

            final AppointmentEntity activeBooking = latestActive;
            runOnUiThread(() -> {
                if (activeBooking != null) {
                    activeBookingId = activeBooking.id;
                    if (cardActiveRepair != null) {
                        cardActiveRepair.setVisibility(View.VISIBLE);
                    }
                    tvDeviceName.setText(activeBooking.deviceName);
                    tvIssueDesc.setText(activeBooking.issueType + " (" + activeBooking.status + ")");

                    if (tvDashActiveStatus != null) {
                        tvDashActiveStatus.setText("ACTIVE REPAIR");
                        tvDashActiveStatus.setTextColor(Color.parseColor("#BB86FC"));
                    }

                    updateDashboardProgressBar(activeBooking.status);
                } else {
                    activeBookingId = -1;
                    if (cardActiveRepair != null) {
                        cardActiveRepair.setVisibility(View.GONE);
                    }
                    tvDeviceName.setText("No Active Repairs");
                    tvIssueDesc.setText("Book a repair to get started");
                }
            });
        });
    }

    private void updateDashboardProgressBar(String status) {
        if (dashStep1 == null || dashStep2 == null || dashStep3 == null || dashStep4 == null) return;

        int activeColor = Color.parseColor("#BB86FC");
        int inactiveColor = Color.parseColor("#444444");
        int inactiveLineColor = Color.parseColor("#333333");

        dashStep1.setColorFilter(inactiveColor);
        dashStep2.setColorFilter(inactiveColor);
        dashStep3.setColorFilter(inactiveColor);
        dashStep4.setColorFilter(inactiveColor);
        if (dashLine1 != null) dashLine1.setBackgroundColor(inactiveLineColor);
        if (dashLine2 != null) dashLine2.setBackgroundColor(inactiveLineColor);
        if (dashLine3 != null) dashLine3.setBackgroundColor(inactiveLineColor);

        if (status == null) return;

        if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Awaiting Customer") ||
                status.equalsIgnoreCase("Approved") || status.equalsIgnoreCase("Received") ||
                status.equalsIgnoreCase("Diagnosed") || status.equalsIgnoreCase("In Repair")) {
            dashStep1.setColorFilter(activeColor);
        }

        if (status.equalsIgnoreCase("Diagnosed") || status.equalsIgnoreCase("In Repair")) {
            dashStep2.setColorFilter(activeColor);
            if (dashLine1 != null) dashLine1.setBackgroundColor(activeColor);
        }

        if (status.equalsIgnoreCase("In Repair")) {
            dashStep3.setColorFilter(activeColor);
            if (dashLine2 != null) dashLine2.setBackgroundColor(activeColor);
        }
    }
}