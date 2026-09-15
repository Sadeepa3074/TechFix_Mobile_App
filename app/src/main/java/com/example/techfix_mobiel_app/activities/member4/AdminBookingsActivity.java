package com.example.techfix_mobiel_app.activities.member4;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member1.database.LoginActivity;
import com.example.techfix_mobiel_app.activities.member2.database.dao.AppointmentDao;
import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;
import com.example.techfix_mobiel_app.database.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AdminBookingsActivity extends AppCompatActivity {

    private TextView tvAdminPendingCount, tvAdminApprovedCount, tvAdminRevenue;
    private TextView tabAll, tabPending, tabApproved, tabRejected;
    private RecyclerView recyclerAdminBookings;

    private AppointmentDao appointmentDao;
    private List<AppointmentEntity> masterBookingList = new ArrayList<>();
    private AdminBookingsAdapter adapter;

    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_bookings);

        appointmentDao = AppDatabase.getInstance(this).appointmentDao();

        tvAdminPendingCount = findViewById(R.id.tvAdminPendingCount);
        tvAdminApprovedCount = findViewById(R.id.tvAdminApprovedCount);
        tvAdminRevenue = findViewById(R.id.tvAdminRevenue);

        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabApproved = findViewById(R.id.tabApproved);
        tabRejected = findViewById(R.id.tabRejected);

        recyclerAdminBookings = findViewById(R.id.recyclerAdminBookings);
        recyclerAdminBookings.setLayoutManager(new LinearLayoutManager(this));

        // Bind persistent bottom navigation listeners
        setupBottomNavigation("Bookings");

        findViewById(R.id.tvAdminLogout).setOnClickListener(v -> {
            getSharedPreferences("TechFixPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(AdminBookingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        tabAll.setOnClickListener(v -> { currentFilter = "All"; updateTabStyles(); filterList(); });
        tabPending.setOnClickListener(v -> { currentFilter = "Pending"; updateTabStyles(); filterList(); });
        tabApproved.setOnClickListener(v -> { currentFilter = "Approved"; updateTabStyles(); filterList(); });
        tabRejected.setOnClickListener(v -> { currentFilter = "Rejected"; updateTabStyles(); filterList(); });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminData();
    }

    private void loadAdminData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            masterBookingList = appointmentDao.getAllAppointments();
            double revenue = appointmentDao.getTotalRevenue();

            int pendingCount = 0;
            int approvedCount = 0;

            for (AppointmentEntity booking : masterBookingList) {
                String status = booking.status != null ? booking.status : "";
                if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Awaiting Customer")) {
                    pendingCount++;
                } else if (status.equalsIgnoreCase("In Repair") || status.equalsIgnoreCase("Ready") || status.equalsIgnoreCase("Approved")) {
                    approvedCount++;
                }
            }

            final int finalPending = pendingCount;
            final int finalApproved = approvedCount;

            runOnUiThread(() -> {
                tvAdminPendingCount.setText(String.valueOf(finalPending));
                tvAdminApprovedCount.setText(String.valueOf(finalApproved));
                tvAdminRevenue.setText("$" + (int) revenue);

                filterList();
            });
        });
    }

    private void updateTabStyles() {
        TextView[] tabs = {tabAll, tabPending, tabApproved, tabRejected};
        for (TextView tab : tabs) {
            tab.setTextColor(Color.parseColor("#888888"));
            tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        TextView activeTab = tabAll;
        if (currentFilter.equals("Pending")) activeTab = tabPending;
        else if (currentFilter.equals("Approved")) activeTab = tabApproved;
        else if (currentFilter.equals("Rejected")) activeTab = tabRejected;

        activeTab.setTextColor(Color.parseColor("#FFFFFF"));
        activeTab.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void filterList() {
        List<AppointmentEntity> filteredList = new ArrayList<>();
        for (AppointmentEntity booking : masterBookingList) {
            String status = booking.status != null ? booking.status : "";

            if (currentFilter.equals("All")) {
                filteredList.add(booking);
            } else if (currentFilter.equalsIgnoreCase("Pending")) {
                if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Awaiting Customer")) {
                    filteredList.add(booking);
                }
            } else if (currentFilter.equalsIgnoreCase("Approved")) {
                if (status.equalsIgnoreCase("Approved") ||
                        status.equalsIgnoreCase("In Repair") ||
                        status.equalsIgnoreCase("Ready")) {
                    filteredList.add(booking);
                }
            } else if (currentFilter.equalsIgnoreCase("Rejected")) {
                if (status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Closed")) {
                    filteredList.add(booking);
                }
            }
        }

        adapter = new AdminBookingsAdapter(filteredList, new AdminBookingsAdapter.AdminActionListener() {
            @Override
            public void onActionClick(AppointmentEntity booking) {
                Intent intent = new Intent(AdminBookingsActivity.this, AdminDetailBookingActivity.class);
                intent.putExtra("BOOKING_ID", booking.id);
                startActivity(intent);
            }

            @Override
            public void onRejectClick(AppointmentEntity booking) {
                booking.status = "Rejected";
                Executors.newSingleThreadExecutor().execute(() -> {
                    appointmentDao.update(booking);
                    runOnUiThread(() -> {
                        Toast.makeText(AdminBookingsActivity.this, "Booking rejected successfully.", Toast.LENGTH_SHORT).show();
                        loadAdminData();
                    });
                });
            }
        });
        recyclerAdminBookings.setAdapter(adapter);
    }

    private static class AdminBookingsAdapter extends RecyclerView.Adapter<AdminBookingsAdapter.ViewHolder> {
        private List<AppointmentEntity> list;
        private AdminActionListener listener;

        public interface AdminActionListener {
            void onActionClick(AppointmentEntity appointment);
            void onRejectClick(AppointmentEntity appointment);
        }

        public AdminBookingsAdapter(List<AppointmentEntity> list, AdminActionListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_booking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppointmentEntity item = list.get(position);

            holder.tvAdminDevice.setText(item.deviceName);

            String branchInfo = item.branchName != null && !item.branchName.isEmpty() ? item.branchName :
                    (item.assignedBranchId == 2 ? "Branch: TechFix - Galle Branch" : "Branch: TechFix - Colombo Branch");
            holder.tvDetailBranch.setText(branchInfo);

            String customerName = item.customerName != null && !item.customerName.isEmpty() ? item.customerName : "Customer #" + item.customerId;
            holder.tvAdminCustomer.setText("Customer: " + customerName);

            holder.tvAdminIssue.setText(item.issueType);
            holder.tvAdminRepairId.setText("TF-000" + item.id);

            String status = item.status != null ? item.status : "";
            holder.tvAdminStatus.setText(status);
            if (status.equalsIgnoreCase("Awaiting Customer") || status.equalsIgnoreCase("Pending")) {
                holder.tvAdminStatus.setTextColor(Color.parseColor("#FFEB3B"));
            } else if (status.equalsIgnoreCase("Approved") || status.equalsIgnoreCase("Completed")) {
                holder.tvAdminStatus.setTextColor(Color.parseColor("#4CAF50"));
            } else if (status.equalsIgnoreCase("Rejected")) {
                holder.tvAdminStatus.setTextColor(Color.parseColor("#CF6679"));
            } else {
                holder.tvAdminStatus.setTextColor(Color.parseColor("#FFFFFF"));
            }

            String payment = item.paymentStatus != null ? item.paymentStatus : "Unpaid";
            holder.tvAdminPayment.setText(payment);
            if (payment.equalsIgnoreCase("Paid")) {
                holder.tvAdminPayment.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                holder.tvAdminPayment.setTextColor(Color.parseColor("#CF6679"));
            }

            holder.tvAdminPrice.setText("$" + (int) item.price);
            holder.tvAdminDate.setText(item.requestDate);

            if (status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Closed")) {
                holder.btnAdminAction.setVisibility(View.GONE);
                holder.btnAdminReject.setVisibility(View.GONE);
            } else {
                holder.btnAdminAction.setVisibility(View.VISIBLE);

                if (status.equalsIgnoreCase("Pending")) {
                    holder.btnAdminAction.setText("Set Price");
                } else if (status.equalsIgnoreCase("Awaiting Customer")) {
                    holder.btnAdminAction.setText("Price Set (View)");
                } else {
                    holder.btnAdminAction.setText("Manage");
                }

                if (status.equalsIgnoreCase("Ready") ||
                        status.equalsIgnoreCase("In Repair") ||
                        status.equalsIgnoreCase("Approved")) {
                    holder.btnAdminReject.setVisibility(View.GONE);
                } else {
                    holder.btnAdminReject.setVisibility(View.VISIBLE);
                    holder.btnAdminReject.setOnClickListener(v -> listener.onRejectClick(item));
                }
            }

            holder.btnAdminAction.setOnClickListener(v -> listener.onActionClick(item));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAdminDevice, tvAdminCustomer, tvAdminIssue, tvAdminRepairId, tvAdminStatus, tvAdminPayment, tvAdminPrice, tvAdminDate, tvDetailBranch;
            MaterialButton btnAdminAction, btnAdminReject;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAdminDevice = itemView.findViewById(R.id.tvAdminDevice);
                tvAdminCustomer = itemView.findViewById(R.id.tvAdminCustomer);
                tvAdminIssue = itemView.findViewById(R.id.tvAdminIssue);
                tvAdminRepairId = itemView.findViewById(R.id.tvAdminRepairId);
                tvAdminStatus = itemView.findViewById(R.id.tvAdminStatus);
                tvAdminPayment = itemView.findViewById(R.id.tvAdminPayment);
                tvAdminPrice = itemView.findViewById(R.id.tvAdminPrice);
                tvAdminDate = itemView.findViewById(R.id.tvAdminDate);
                tvDetailBranch = itemView.findViewById(R.id.tvDetailBranch);
                btnAdminAction = itemView.findViewById(R.id.btnAdminAction);
                btnAdminReject = itemView.findViewById(R.id.btnAdminReject);
            }
        }
    }

    private void setupBottomNavigation(String currentScreen) {
        View navOverview = findViewById(R.id.navOverview);
        View navSpareParts = findViewById(R.id.navSpareParts);
        View navInventory = findViewById(R.id.navInventory);
        View navBookings = findViewById(R.id.navBookings);

        if (navOverview != null) {
            navOverview.setOnClickListener(v -> {
                if (!currentScreen.equalsIgnoreCase("Overview")) {
                    Intent intent = new Intent(this, AdminDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }

        if (navSpareParts != null) {
            navSpareParts.setOnClickListener(v -> {
                if (!currentScreen.equalsIgnoreCase("SpareParts")) {
                    Intent intent = new Intent(this, AdminSparePartsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }

        if (navInventory != null) {
            navInventory.setOnClickListener(v -> {
                if (!currentScreen.equalsIgnoreCase("Inventory")) {
                    Intent intent = new Intent(this, AdminInventoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }

        if (navBookings != null) {
            navBookings.setOnClickListener(v -> {
                if (!currentScreen.equalsIgnoreCase("Bookings")) {
                    Intent intent = new Intent(this, AdminBookingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });
        }
    }
}