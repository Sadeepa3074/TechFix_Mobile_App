package com.example.techfix_mobiel_app.activities.member2.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techfix_mobiel_app.activities.member1.database.ProfileActivity;
import com.example.techfix_mobiel_app.activities.member3.database.CustomerReviewActivity;
import com.google.android.material.button.MaterialButton;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member2.database.dao.AppointmentDao;
import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;
import com.example.techfix_mobiel_app.database.AppDatabase;

import java.util.List;
import java.util.concurrent.Executors;

public class TrackStatusActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRepairs;
    private AppointmentDao appointmentDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_status);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnProfile).setOnClickListener(v -> startActivity(new Intent(TrackStatusActivity.this, ProfileActivity.class)));

        recyclerViewRepairs = findViewById(R.id.recyclerViewRepairs);
        recyclerViewRepairs.setLayoutManager(new LinearLayoutManager(this));

        appointmentDao = AppDatabase.getInstance(this).appointmentDao();

        loadActiveBookingData();

        findViewById(R.id.btnCallColombo).setOnClickListener(v -> makePhoneCall("0112345678"));
        findViewById(R.id.btnCallGalle).setOnClickListener(v -> makePhoneCall("0912234567"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveBookingData();
    }

    private void loadActiveBookingData() {
        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
        int currentCustomerId = prefs.getInt("logged_customer_id", 1);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<AppointmentEntity> activeBookings = appointmentDao.getActiveAppointmentsForUser(currentCustomerId);

            runOnUiThread(() -> {
                if (activeBookings != null && !activeBookings.isEmpty()) {
                    recyclerViewRepairs.setVisibility(View.VISIBLE);
                    ActiveRepairsAdapter adapter = new ActiveRepairsAdapter(this, activeBookings, new ActiveRepairsAdapter.OnItemClickListener() {
                        @Override
                        public void onCardClick(AppointmentEntity booking) {
                            Intent intent = new Intent(TrackStatusActivity.this, CustomerBookingDetailActivity.class);
                            intent.putExtra("BOOKING_ID", booking.id);
                            startActivity(intent);
                        }

                        @Override
                        public void onDirectionsClick(AppointmentEntity booking) {
                            if (booking.assignedBranchId == 2) {
                                openGoogleMaps("No. 78, Wakwella Road, Galle 80000, Sri Lanka");
                            } else {
                                openGoogleMaps("No. 245, Galle Road, Colombo 03, Sri Lanka");
                            }
                        }

                        @Override
                        public void onCollectClick(AppointmentEntity booking) {
                            Intent intent = new Intent(TrackStatusActivity.this, CustomerReviewActivity.class);
                            intent.putExtra("BOOKING_ID", booking.id);
                            startActivity(intent);
                        }

                        @Override
                        public void onCloseRejectedClick(AppointmentEntity booking) {
                            Executors.newSingleThreadExecutor().execute(() -> {
                                booking.status = "Closed"; // Move rejected card to history
                                appointmentDao.update(booking);
                                runOnUiThread(() -> {
                                    Toast.makeText(TrackStatusActivity.this, "Moved to history", Toast.LENGTH_SHORT).show();
                                    loadActiveBookingData();
                                });
                            });
                        }

                        @Override
                        public void onConfirmPriceClick(AppointmentEntity booking) {
                            // Directly approve the price and advance progress to "In Repair"
                            Executors.newSingleThreadExecutor().execute(() -> {
                                booking.status = "In Repair";
                                appointmentDao.update(booking);
                                runOnUiThread(() -> {
                                    Toast.makeText(TrackStatusActivity.this, "Price confirmed! Moving to repair stage.", Toast.LENGTH_SHORT).show();
                                    loadActiveBookingData(); // Refresh tracker & move to admin approved tab
                                });
                            });
                        }
                    });
                    recyclerViewRepairs.setAdapter(adapter);
                } else {
                    recyclerViewRepairs.setVisibility(View.GONE);
                    Toast.makeText(this, "No active bookings found", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void openGoogleMaps(String destinationAddress) {
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destinationAddress));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=" + Uri.encode(destinationAddress)));
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open maps", Toast.LENGTH_SHORT).show();
        }
    }

    private void makePhoneCall(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open phone dialer", Toast.LENGTH_SHORT).show();
        }
    }

    private static class ActiveRepairsAdapter extends RecyclerView.Adapter<ActiveRepairsAdapter.RepairViewHolder> {

        private final android.content.Context context;
        private List<AppointmentEntity> list;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onCardClick(AppointmentEntity appointment);
            void onDirectionsClick(AppointmentEntity appointment);
            void onCollectClick(AppointmentEntity appointment);
            void onCloseRejectedClick(AppointmentEntity appointment);
            void onConfirmPriceClick(AppointmentEntity appointment); // Added handler
        }

        public ActiveRepairsAdapter(android.content.Context context, List<AppointmentEntity> repairList, OnItemClickListener listener) {
            this.context = context;
            this.list = repairList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public RepairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_repair_card, parent, false);
            return new RepairViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RepairViewHolder holder, int position) {
            AppointmentEntity item = list.get(position);

            holder.tvDeviceName.setText(item.deviceName);
            holder.tvIssueDesc.setText(item.issueType);
            holder.tvBranchName.setText(item.assignedBranchId == 2 ? "TechFix - Galle Branch" : "TechFix - Colombo Branch");
            holder.tvBookingDate.setText(item.requestDate);
            holder.tvRepairId.setText("TF-000" + item.id);

            String statusDisplay = item.status != null ? item.status.toUpperCase() : "RECEIVED";
            if (item.price > 0 && !item.status.equalsIgnoreCase("Rejected")) {
                statusDisplay += " ($" + (int) item.price + ")";
            }
            holder.tvStatusText.setText(statusDisplay);

            if (item.status != null) {
                if (item.status.equalsIgnoreCase("Rejected")) {
                    holder.tvStatusText.setTextColor(Color.parseColor("#CF6679"));
                } else if (item.status.equalsIgnoreCase("Ready")) {
                    holder.tvStatusText.setTextColor(Color.parseColor("#4CAF50"));
                } else if (item.status.equalsIgnoreCase("Awaiting Customer") || item.status.equalsIgnoreCase("Pending") || item.status.equalsIgnoreCase("In Repair")) {
                    holder.tvStatusText.setTextColor(Color.parseColor("#FFB74D"));
                } else {
                    holder.tvStatusText.setTextColor(Color.parseColor("#BB86FC"));
                }
            }

            updateProgressBar(holder, item.status);

            // Handle button states based on status cleanly without crashes
            if (item.status != null && item.status.equalsIgnoreCase("Ready")) {
                holder.btnDeviceCollected.setText("Review Photo & Pay");
                holder.btnDeviceCollected.setBackgroundColor(Color.parseColor("#228B22"));
                holder.btnDeviceCollected.setVisibility(View.VISIBLE);
                holder.btnDeviceCollected.setOnClickListener(v -> listener.onCollectClick(item));
            } else if (item.status != null && item.status.equalsIgnoreCase("Rejected")) {
                holder.btnDeviceCollected.setText("Close");
                holder.btnDeviceCollected.setBackgroundColor(Color.parseColor("#CF6679"));
                holder.btnDeviceCollected.setVisibility(View.VISIBLE);
                holder.btnDeviceCollected.setOnClickListener(v -> listener.onCloseRejectedClick(item));
            } else if (item.status != null && item.status.equalsIgnoreCase("Awaiting Customer")) {
                holder.btnDeviceCollected.setText("Confirm Price");
                holder.btnDeviceCollected.setBackgroundColor(Color.parseColor("#FFB74D"));
                holder.btnDeviceCollected.setVisibility(View.VISIBLE);
                holder.btnDeviceCollected.setOnClickListener(v -> listener.onConfirmPriceClick(item)); // Direct action handler
            } else {
                holder.btnDeviceCollected.setVisibility(View.GONE);
                holder.btnDeviceCollected.setOnClickListener(null);
            }

            holder.btnDirections.setOnClickListener(v -> listener.onDirectionsClick(item));
            holder.itemView.setOnClickListener(v -> listener.onCardClick(item));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class RepairViewHolder extends RecyclerView.ViewHolder {
            TextView tvDeviceName, tvIssueDesc, tvBranchName, tvBookingDate, tvRepairId, tvStatusText, btnDirections;
            ImageView step1Icon, step2Icon, step3Icon, step4Icon;
            View line1, line2, line3;
            MaterialButton btnDeviceCollected;

            public RepairViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
                tvIssueDesc = itemView.findViewById(R.id.tvIssueDesc);
                tvBranchName = itemView.findViewById(R.id.tvBranchName);
                tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
                tvRepairId = itemView.findViewById(R.id.tvRepairId);
                tvStatusText = itemView.findViewById(R.id.tvStatusText);
                btnDirections = itemView.findViewById(R.id.btnDirections);

                step1Icon = itemView.findViewById(R.id.step1Icon);
                step2Icon = itemView.findViewById(R.id.step2Icon);
                step3Icon = itemView.findViewById(R.id.step3Icon);
                step4Icon = itemView.findViewById(R.id.step4Icon);

                line1 = itemView.findViewById(R.id.line1);
                line2 = itemView.findViewById(R.id.line2);
                line3 = itemView.findViewById(R.id.line3);

                btnDeviceCollected = itemView.findViewById(R.id.btnDeviceCollected);
            }
        }

        private void updateProgressBar(RepairViewHolder holder, String status) {
            int activeColor = Color.parseColor("#BB86FC");
            int inactiveColor = Color.parseColor("#444444");
            int inactiveLineColor = Color.parseColor("#333333");

            holder.step1Icon.setColorFilter(inactiveColor);
            holder.step2Icon.setColorFilter(inactiveColor);
            holder.step3Icon.setColorFilter(inactiveColor);
            holder.step4Icon.setColorFilter(inactiveColor);
            holder.line1.setBackgroundColor(inactiveLineColor);
            holder.line2.setBackgroundColor(inactiveLineColor);
            holder.line3.setBackgroundColor(inactiveLineColor);

            if (status == null) return;

            if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Awaiting Customer") ||
                    status.equalsIgnoreCase("Approved") || status.equalsIgnoreCase("Received") ||
                    status.equalsIgnoreCase("Diagnosed") || status.equalsIgnoreCase("In Repair") ||
                    status.equalsIgnoreCase("Ready")) {
                holder.step1Icon.setColorFilter(activeColor);
            }

            if (status.equalsIgnoreCase("Diagnosed") || status.equalsIgnoreCase("In Repair") || status.equalsIgnoreCase("Ready")) {
                holder.step2Icon.setColorFilter(activeColor);
                holder.line1.setBackgroundColor(activeColor);
            }

            if (status.equalsIgnoreCase("In Repair") || status.equalsIgnoreCase("Ready")) {
                holder.step3Icon.setColorFilter(activeColor);
                holder.line2.setBackgroundColor(activeColor);
            }

            if (status.equalsIgnoreCase("Ready")) {
                holder.step4Icon.setColorFilter(activeColor);
                holder.line3.setBackgroundColor(activeColor);
            }
        }
    }
}