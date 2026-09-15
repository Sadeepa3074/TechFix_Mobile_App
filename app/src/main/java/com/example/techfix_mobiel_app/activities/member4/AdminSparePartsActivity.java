package com.example.techfix_mobiel_app.activities.member4;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member1.database.LoginActivity;
import com.example.techfix_mobiel_app.activities.member4.database.entities.SparePartEntity;
import com.example.techfix_mobiel_app.network.ApiService;
import com.example.techfix_mobiel_app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSparePartsActivity extends AppCompatActivity {

    private EditText etSearchParts;
    private MaterialButton btnAddSparePart;
    private MaterialCardView cardStatTotal, cardStatInStock, cardStatLowOut;
    private TextView tvStatTotalParts, tvStatInStock, tvStatLowOut;
    private RecyclerView recyclerSpareParts;

    private List<SparePartEntity> masterSparePartsList = new ArrayList<>();
    private AdminSparePartsAdapter adapter;

    // Filters: "ALL", "IN_STOCK", "LOW_STOCK"
    private String selectedFilter = "ALL";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_spare_parts);

        // Bind persistent bottom navigation listeners
        setupBottomNavigation("SpareParts");

        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            getSharedPreferences("TechFixPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        etSearchParts = findViewById(R.id.etSearchParts);
        btnAddSparePart = findViewById(R.id.btnAddSparePart);

        cardStatTotal = findViewById(R.id.cardStatTotal);
        cardStatInStock = findViewById(R.id.cardStatInStock);
        cardStatLowOut = findViewById(R.id.cardStatLowOut);

        tvStatTotalParts = findViewById(R.id.tvStatTotalParts);
        tvStatInStock = findViewById(R.id.tvStatInStock);
        tvStatLowOut = findViewById(R.id.tvStatLowOut);

        recyclerSpareParts = findViewById(R.id.recyclerSpareParts);
        recyclerSpareParts.setLayoutManager(new LinearLayoutManager(this));

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSparePartsData();
    }

    private void setupListeners() {
        etSearchParts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                filterAndCalculateStats();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnAddSparePart.setOnClickListener(v -> {
            Intent intent = new Intent(AdminSparePartsActivity.this, ManageSparePartActivity.class);
            startActivity(intent);
        });

        // Filter Card Click Listeners
        cardStatTotal.setOnClickListener(v -> {
            selectedFilter = "ALL";
            updateCardBorderStyles();
            filterAndCalculateStats();
        });

        cardStatInStock.setOnClickListener(v -> {
            selectedFilter = "IN_STOCK";
            updateCardBorderStyles();
            filterAndCalculateStats();
        });

        cardStatLowOut.setOnClickListener(v -> {
            selectedFilter = "LOW_STOCK";
            updateCardBorderStyles();
            filterAndCalculateStats();
        });
    }

    private void updateCardBorderStyles() {
        int activeColor = Color.parseColor("#BB86FC");
        int inactiveColor = Color.parseColor("#222224");

        cardStatTotal.setStrokeColor(selectedFilter.equals("ALL") ? activeColor : inactiveColor);
        cardStatInStock.setStrokeColor(selectedFilter.equals("IN_STOCK") ? activeColor : inactiveColor);
        cardStatLowOut.setStrokeColor(selectedFilter.equals("LOW_STOCK") ? activeColor : inactiveColor);
    }

    private void loadSparePartsData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getSpareParts().enqueue(new Callback<List<SparePartEntity>>() {
            @Override
            public void onResponse(Call<List<SparePartEntity>> call, Response<List<SparePartEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    masterSparePartsList = response.body();
                    filterAndCalculateStats();
                } else {
                    Toast.makeText(AdminSparePartsActivity.this, "Failed to load spare parts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SparePartEntity>> call, Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(AdminSparePartsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndCalculateStats() {
        List<SparePartEntity> displayList = new ArrayList<>();

        int totalCount = masterSparePartsList.size();
        int inStockCount = 0;
        int lowOutCount = 0;

        for (SparePartEntity item : masterSparePartsList) {
            // Quantity < 10 is considered Low/Out stock
            if (item.quantity >= 10) {
                inStockCount++;
            } else {
                lowOutCount++;
            }

            // Apply stock filter
            boolean matchesStockFilter = false;
            if (selectedFilter.equals("ALL")) {
                matchesStockFilter = true;
            } else if (selectedFilter.equals("IN_STOCK") && item.quantity >= 10) {
                matchesStockFilter = true;
            } else if (selectedFilter.equals("LOW_STOCK") && item.quantity < 10) {
                matchesStockFilter = true;
            }

            // Apply search filter
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    (item.partName != null && item.partName.toLowerCase().contains(currentSearchQuery)) ||
                    (item.category != null && item.category.toLowerCase().contains(currentSearchQuery));

            if (matchesStockFilter && matchesSearch) {
                displayList.add(item);
            }
        }

        // Update Stat Card Counters
        tvStatTotalParts.setText(String.valueOf(totalCount));
        tvStatInStock.setText(String.valueOf(inStockCount));
        tvStatLowOut.setText(String.valueOf(lowOutCount));

        adapter = new AdminSparePartsAdapter(displayList, new AdminSparePartsAdapter.SparePartActionListener() {
            @Override
            public void onEditClick(SparePartEntity item) {
                Intent intent = new Intent(AdminSparePartsActivity.this, ManageSparePartActivity.class);
                intent.putExtra("PART_ID", item.id);
                intent.putExtra("PART_NAME", item.partName);
                intent.putExtra("PART_CATEGORY", item.category);
                intent.putExtra("PART_QTY", item.quantity);
                intent.putExtra("PART_PRICE", item.price);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(SparePartEntity item) {
                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                apiService.deleteSparePart(item.id).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        Toast.makeText(AdminSparePartsActivity.this, "Deleted " + item.partName, Toast.LENGTH_SHORT).show();
                        loadSparePartsData();
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(AdminSparePartsActivity.this, "Failed to delete from server", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        recyclerSpareParts.setAdapter(adapter);
    }

    private static class AdminSparePartsAdapter extends RecyclerView.Adapter<AdminSparePartsAdapter.ViewHolder> {
        private final List<SparePartEntity> list;
        private final SparePartActionListener listener;

        public interface SparePartActionListener {
            void onEditClick(SparePartEntity item);
            void onDeleteClick(SparePartEntity item);
        }

        public AdminSparePartsAdapter(List<SparePartEntity> list, SparePartActionListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spare_part, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SparePartEntity item = list.get(position);

            holder.tvPartName.setText(item.partName);
            holder.tvPartCategory.setText(item.category != null ? item.category : "General");
            holder.tvPartQty.setText(String.valueOf(item.quantity));
            holder.tvPartPrice.setText("$" + (int) item.price);

            // Quantity status logic
            if (item.quantity == 0) {
                holder.tvPartStatus.setText("Out of Stock");
                holder.tvPartStatus.setTextColor(Color.parseColor("#FF5252"));
                holder.tvPartStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFF5252")));
            } else if (item.quantity < 10) {
                holder.tvPartStatus.setText("Low Stock");
                holder.tvPartStatus.setTextColor(Color.parseColor("#FFB74D"));
                holder.tvPartStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFFB74D")));
            } else {
                holder.tvPartStatus.setText("In Stock");
                holder.tvPartStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.tvPartStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A4CAF50")));
            }

            holder.btnEditPart.setOnClickListener(v -> listener.onEditClick(item));
            holder.btnDeletePart.setOnClickListener(v -> listener.onDeleteClick(item));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPartName, tvPartCategory, tvPartStatus, tvPartQty, tvPartPrice;
            MaterialCardView btnEditPart, btnDeletePart;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPartName = itemView.findViewById(R.id.tvPartName);
                tvPartCategory = itemView.findViewById(R.id.tvPartCategory);
                tvPartStatus = itemView.findViewById(R.id.tvPartStatus);
                tvPartQty = itemView.findViewById(R.id.tvPartQty);
                tvPartPrice = itemView.findViewById(R.id.tvPartPrice);
                btnEditPart = itemView.findViewById(R.id.btnEditPart);
                btnDeletePart = itemView.findViewById(R.id.btnDeletePart);
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