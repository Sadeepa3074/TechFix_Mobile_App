package com.example.techfix_mobiel_app.activities.member2.database;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;
import com.example.techfix_mobiel_app.network.ApiService;
import com.example.techfix_mobiel_app.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerBookingDetailActivity extends AppCompatActivity {

    private TextView tvDetailDevice, tvDetailIssue, tvDetailDescription, tvDetailStatus, tvDetailPrice, tvDetailBranch;
    private MaterialButton btnApproveRepair;

    private int bookingId = -1;
    private AppointmentEntity currentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_booking_detail);

        bookingId = getIntent().getIntExtra("BOOKING_ID", -1);

        tvDetailDevice = findViewById(R.id.tvDetailDevice);
        tvDetailIssue = findViewById(R.id.tvDetailIssue);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailBranch = findViewById(R.id.tvDetailBranch);
        btnApproveRepair = findViewById(R.id.btnApproveRepair);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadBookingDetails();

        btnApproveRepair.setOnClickListener(v -> {
            if (currentBooking != null) {
                currentBooking.status = "In Repair";

                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                apiService.updateAppointment(currentBooking).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> result = response.body();
                            if ("success".equals(result.get("status"))) {
                                Toast.makeText(CustomerBookingDetailActivity.this, "Repair quotation approved! Moving to repair stage.", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(CustomerBookingDetailActivity.this, "Failed to update appointment", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e("API_ERROR", "Error: " + t.getMessage());
                        Toast.makeText(CustomerBookingDetailActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadBookingDetails() {
        if (bookingId == -1) return;

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getAppointmentById(bookingId).enqueue(new Callback<AppointmentEntity>() {
            @Override
            public void onResponse(Call<AppointmentEntity> call, Response<AppointmentEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentBooking = response.body();

                    tvDetailDevice.setText(currentBooking.deviceName);
                    tvDetailIssue.setText(currentBooking.issueType);
                    tvDetailDescription.setText(currentBooking.description != null ? currentBooking.description : "No description provided.");
                    tvDetailStatus.setText("Status: " + currentBooking.status);

                    String branchInfo = currentBooking.branchName != null ? currentBooking.branchName :
                            (currentBooking.assignedBranchId == 2 ? "TechFix - Galle Branch" : "TechFix - Colombo Branch");
                    tvDetailBranch.setText("Branch: " + branchInfo);

                    if ("Awaiting Customer".equalsIgnoreCase(currentBooking.status) && currentBooking.price > 0) {
                        tvDetailPrice.setText("Quotation Amount: $" + currentBooking.price);
                        tvDetailPrice.setVisibility(View.VISIBLE);
                        btnApproveRepair.setVisibility(View.VISIBLE);
                    } else {
                        if (currentBooking.price > 0) {
                            tvDetailPrice.setText("Quotation Amount: $" + currentBooking.price);
                            tvDetailPrice.setVisibility(View.VISIBLE);
                        } else {
                            tvDetailPrice.setText("Quotation Amount: Pending Admin Review");
                            tvDetailPrice.setVisibility(View.VISIBLE);
                        }
                        btnApproveRepair.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(CustomerBookingDetailActivity.this, "Failed to load booking details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AppointmentEntity> call, Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(CustomerBookingDetailActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}