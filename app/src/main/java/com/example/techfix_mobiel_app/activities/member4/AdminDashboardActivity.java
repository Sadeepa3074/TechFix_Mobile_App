package com.example.techfix_mobiel_app.activities.member4;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member1.database.LoginActivity;
import com.example.techfix_mobiel_app.activities.member2.database.dao.AppointmentDao;
import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;
import com.example.techfix_mobiel_app.activities.member4.database.entities.SparePartEntity;
import com.example.techfix_mobiel_app.database.AppDatabase;
import com.example.techfix_mobiel_app.network.ApiService;
import com.example.techfix_mobiel_app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvActiveRepairs, tvPendingApproval, tvTodayRevenue, tvLowStockParts;
    private LinearLayout navOverview, navSpareParts, navInventory, navBookings;
    private LinearLayout containerRecentBookings, containerReorderParts;
    private View btnAdminLogout;

    private AppointmentDao appointmentDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        AppDatabase db = AppDatabase.getInstance(this);
        appointmentDao = db.appointmentDao();

        tvActiveRepairs = findViewById(R.id.tvActiveRepairs);
        tvPendingApproval = findViewById(R.id.tvPendingApproval);
        tvTodayRevenue = findViewById(R.id.tvTodayRevenue);
        tvLowStockParts = findViewById(R.id.tvLowStockParts);

        containerRecentBookings = findViewById(R.id.containerRecentBookings);
        containerReorderParts = findViewById(R.id.containerReorderParts);

        navOverview = findViewById(R.id.navOverview);
        navSpareParts = findViewById(R.id.navSpareParts);
        navInventory = findViewById(R.id.navInventory);
        navBookings = findViewById(R.id.navBookings);

        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void setupListeners() {
        if (btnAdminLogout != null) {
            btnAdminLogout.setOnClickListener(v -> {
                getSharedPreferences("TechFixPrefs", MODE_PRIVATE).edit().clear().apply();
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (navSpareParts != null) {
            navSpareParts.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminSparePartsActivity.class)));
        }

        if (navInventory != null) {
            navInventory.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminInventoryActivity.class)));
        }

        if (navBookings != null) {
            navBookings.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminBookingsActivity.class)));
        }
    }

    private void loadDashboardData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Appointments Data
            List<AppointmentEntity> allBookings = appointmentDao.getAllAppointments();
            int activeCount = 0;
            int pendingCount = 0;
            double appointmentRevenue = 0.0;

            List<AppointmentEntity> recentList = new ArrayList<>();

            if (allBookings != null) {
                for (int i = allBookings.size() - 1; i >= 0; i--) {
                    AppointmentEntity b = allBookings.get(i);
                    String status = b.status != null ? b.status : "";

                    if (status.equalsIgnoreCase("In Repair") || status.equalsIgnoreCase("Diagnosed")) {
                        activeCount++;
                    } else if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Awaiting Customer")) {
                        pendingCount++;
                    }

                    if ("Paid".equalsIgnoreCase(b.paymentStatus)) {
                        appointmentRevenue += b.price;
                    }

                    if (recentList.size() < 4) {
                        recentList.add(b);
                    }
                }
            }

            // 2. Read spare parts revenue from persistent TechFixRevenuePrefs
            float sparePartsRevenue = getSharedPreferences("TechFixRevenuePrefs", MODE_PRIVATE)
                    .getFloat("TOTAL_SPARE_PARTS_REVENUE", 0f);
            double grandTotalRevenue = appointmentRevenue + sparePartsRevenue;

            // 3. Fetch spare parts from MySQL server via Retrofit
            List<SparePartEntity> partsList = new ArrayList<>();
            try {
                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                Response<List<SparePartEntity>> response = apiService.getSpareParts().execute();
                if (response.isSuccessful() && response.body() != null) {
                    partsList = response.body();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<SparePartEntity> lowStockList = new ArrayList<>();
            if (partsList != null) {
                for (SparePartEntity p : partsList) {
                    if (p.quantity < 10) {
                        lowStockList.add(p);
                    }
                }
            }

            final int finalActive = activeCount;
            final int finalPending = pendingCount;
            final double finalRevenue = grandTotalRevenue;
            final int finalLowStock = lowStockList.size();

            runOnUiThread(() -> {
                if (tvActiveRepairs != null) tvActiveRepairs.setText(String.valueOf(finalActive));
                if (tvPendingApproval != null) tvPendingApproval.setText(String.valueOf(finalPending));
                if (tvTodayRevenue != null) tvTodayRevenue.setText("$" + (int) finalRevenue);
                if (tvLowStockParts != null) tvLowStockParts.setText(String.valueOf(finalLowStock));

                renderRecentBookingsUI(recentList);
                renderLowStockPartsUI(lowStockList);
            });
        });
    }

    private void renderRecentBookingsUI(List<AppointmentEntity> bookings) {
        if (containerRecentBookings == null) return;
        containerRecentBookings.removeAllViews();

        if (bookings.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No recent bookings");
            tvEmpty.setTextColor(Color.parseColor("#666666"));
            tvEmpty.setTextSize(12);
            containerRecentBookings.addView(tvEmpty);
            return;
        }

        for (AppointmentEntity b : bookings) {
            RelativeLayout row = new RelativeLayout(this);
            row.setPadding(0, 0, 0, 24);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(b.deviceName != null ? b.deviceName : "Device");
            tvTitle.setTextColor(Color.WHITE);
            tvTitle.setTextSize(13);

            TextView tvSub = new TextView(this);
            tvSub.setText(b.customerName != null ? b.customerName : "Customer");
            tvSub.setTextColor(Color.parseColor("#666666"));
            tvSub.setTextSize(11);

            textCol.addView(tvTitle);
            textCol.addView(tvSub);

            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            textParams.addRule(RelativeLayout.ALIGN_PARENT_START);

            TextView tvStatus = new TextView(this);
            tvStatus.setText(b.status != null ? b.status : "Pending");
            tvStatus.setTextSize(10);
            tvStatus.setPadding(20, 8, 20, 8);
            tvStatus.setBackgroundResource(R.drawable.bg_rounded_dark);

            if ("Completed".equalsIgnoreCase(b.status) || "Approved".equalsIgnoreCase(b.status)) {
                tvStatus.setTextColor(Color.parseColor("#4CAF50"));
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A4CAF50")));
            } else {
                tvStatus.setTextColor(Color.parseColor("#FFB74D"));
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFFB74D")));
            }

            RelativeLayout.LayoutParams statusParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            statusParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            statusParams.addRule(RelativeLayout.CENTER_VERTICAL);

            row.addView(textCol, textParams);
            row.addView(tvStatus, statusParams);

            containerRecentBookings.addView(row);
        }
    }

    private void renderLowStockPartsUI(List<SparePartEntity> lowParts) {
        if (containerReorderParts == null) return;
        containerReorderParts.removeAllViews();

        if (lowParts.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("All parts sufficiently stocked");
            tvEmpty.setTextColor(Color.parseColor("#4CAF50"));
            tvEmpty.setTextSize(12);
            containerReorderParts.addView(tvEmpty);
            return;
        }

        for (SparePartEntity p : lowParts) {
            RelativeLayout row = new RelativeLayout(this);
            row.setPadding(0, 0, 0, 24);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(p.partName);
            tvTitle.setTextColor(Color.WHITE);
            tvTitle.setTextSize(13);

            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            titleParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            titleParams.addRule(RelativeLayout.CENTER_VERTICAL);

            LinearLayout rightCol = new LinearLayout(this);
            rightCol.setOrientation(LinearLayout.HORIZONTAL);
            rightCol.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvCount = new TextView(this);
            tvCount.setText(p.quantity + " left");
            tvCount.setTextColor(Color.parseColor("#666666"));
            tvCount.setTextSize(11);
            tvCount.setPadding(0, 0, 16, 0);

            TextView tvTag = new TextView(this);
            tvTag.setTextSize(10);
            tvTag.setPadding(20, 8, 20, 8);
            tvTag.setBackgroundResource(R.drawable.bg_rounded_dark);

            if (p.quantity == 0) {
                tvTag.setText("Out");
                tvTag.setTextColor(Color.parseColor("#FF5252"));
                tvTag.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFF5252")));
            } else {
                tvTag.setText("Low");
                tvTag.setTextColor(Color.parseColor("#FFB74D"));
                tvTag.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFFB74D")));
            }

            rightCol.addView(tvCount);
            rightCol.addView(tvTag);

            RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rightParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            rightParams.addRule(RelativeLayout.CENTER_VERTICAL);

            row.addView(tvTitle, titleParams);
            row.addView(rightCol, rightParams);

            containerReorderParts.addView(row);
        }
    }
}