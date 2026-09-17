package com.example.techfix_mobiel_app.activities.member2.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member1.database.ProfileActivity;
import com.example.techfix_mobiel_app.activities.member2.database.dao.AppointmentDao;
import com.example.techfix_mobiel_app.activities.member2.database.entities.AppointmentEntity;
import com.example.techfix_mobiel_app.database.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class RepairHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private TextView tabHistoryAll, tabHistoryCollected, tabHistoryCancelled;
    private AppointmentDao appointmentDao;
    private List<AppointmentEntity> masterHistoryList = new ArrayList<>();
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_history);

        appointmentDao = AppDatabase.getInstance(this).appointmentDao();

        findViewById(R.id.btnBackHistory).setOnClickListener(v -> finish());
        findViewById(R.id.btnProfileHistory).setOnClickListener(v -> startActivity(new Intent(RepairHistoryActivity.this, ProfileActivity.class)));

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        tabHistoryAll = findViewById(R.id.tabHistoryAll);
        tabHistoryCollected = findViewById(R.id.tabHistoryCollected);
        tabHistoryCancelled = findViewById(R.id.tabHistoryCancelled);

        tabHistoryAll.setOnClickListener(v -> { currentFilter = "All"; updateTabs(); filterList(); });
        tabHistoryCollected.setOnClickListener(v -> { currentFilter = "Collected"; updateTabs(); filterList(); });
        tabHistoryCancelled.setOnClickListener(v -> { currentFilter = "Cancelled"; updateTabs(); filterList(); });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryData();
    }

    private void loadHistoryData() {
        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
        int currentCustomerId = prefs.getInt("logged_customer_id", 1);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<AppointmentEntity> allAppointments = appointmentDao.getAppointmentsForUser(currentCustomerId);
            masterHistoryList = new ArrayList<>();

            for (AppointmentEntity appt : allAppointments) {
                String status = appt.status != null ? appt.status : "";
                // HISTORY RULE: Only show items that are fully completed (paid) or closed/rejected by the customer.
                // "Ready" is strictly live status, so it won't show up here prematurely anymore.
                if (status.equalsIgnoreCase("Completed") ||
                        status.equalsIgnoreCase("Closed") ||
                        status.equalsIgnoreCase("Rejected") ||
                        status.equalsIgnoreCase("Cancelled")) {
                    masterHistoryList.add(appt);
                }
            }

            runOnUiThread(this::filterList);
        });
    }

    private void updateTabs() {
        TextView[] tabs = {tabHistoryAll, tabHistoryCollected, tabHistoryCancelled};
        for (TextView tab : tabs) {
            tab.setBackgroundColor(Color.TRANSPARENT);
            tab.setTextColor(Color.parseColor("#888888"));
            tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        TextView active = tabHistoryAll;
        if (currentFilter.equals("Collected")) active = tabHistoryCollected;
        else if (currentFilter.equals("Cancelled")) active = tabHistoryCancelled;

        active.setBackgroundColor(Color.parseColor("#BB86FC"));
        active.setTextColor(Color.parseColor("#000000"));
        active.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void filterList() {
        List<AppointmentEntity> filtered = new ArrayList<>();
        for (AppointmentEntity item : masterHistoryList) {
            String status = item.status != null ? item.status : "";
            if (currentFilter.equals("All")) {
                filtered.add(item);
            } else if (currentFilter.equals("Collected")) {
                if (status.equalsIgnoreCase("Completed")) {
                    filtered.add(item);
                }
            } else if (currentFilter.equals("Cancelled")) {
                if (status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Closed") || status.equalsIgnoreCase("Cancelled")) {
                    filtered.add(item);
                }
            }
        }

        HistoryAdapter adapter = new HistoryAdapter(filtered);
        recyclerViewHistory.setAdapter(adapter);
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<AppointmentEntity> list;

        public HistoryAdapter(List<AppointmentEntity> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_repair_history_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppointmentEntity item = list.get(position);

            holder.tvDeviceName.setText(item.deviceName);
            holder.tvIssueDesc.setText(item.issueType);
            holder.tvRepairId.setText("TF-000" + item.id);
            holder.tvDate.setText(item.requestDate);
            holder.tvPrice.setText("$" + (int) item.price);

            String status = item.status != null ? item.status : "";
            if (status.equalsIgnoreCase("Rejected") || status.equalsIgnoreCase("Closed") || status.equalsIgnoreCase("Cancelled")) {
                holder.tvStatusBadge.setText("Cancelled");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#CF6679"));
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#3B1C23"));
            } else {
                holder.tvStatusBadge.setText("Completed");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#4CAF50"));
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#1E3324"));
            }
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStatusBadge, tvRepairId, tvDeviceName, tvIssueDesc, tvDate, tvPrice;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStatusBadge = itemView.findViewById(R.id.tvHistoryStatusBadge);
                tvRepairId = itemView.findViewById(R.id.tvHistoryRepairId);
                tvDeviceName = itemView.findViewById(R.id.tvHistoryDeviceName);
                tvIssueDesc = itemView.findViewById(R.id.tvHistoryIssueDesc);
                tvDate = itemView.findViewById(R.id.tvHistoryDate);
                tvPrice = itemView.findViewById(R.id.tvHistoryPrice);
            }
        }
    }
}