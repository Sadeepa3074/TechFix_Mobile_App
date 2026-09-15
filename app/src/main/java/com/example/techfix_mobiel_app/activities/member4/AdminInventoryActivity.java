package com.example.techfix_mobiel_app.activities.member4;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.techfix_mobiel_app.database.AppDatabase;
import com.example.techfix_mobiel_app.activities.member4.database.dao.InventoryDao;
import com.example.techfix_mobiel_app.activities.member4.database.entities.InventoryEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AdminInventoryActivity extends AppCompatActivity {

    private EditText etSearchInventory;
    private MaterialButton btnAddInventory;
    private TextView tvStatTotalItems, tvStatGoodCondition, tvStatNeedsAttention;
    private TextView tabAll, tabTools, tabEquipment, tabConsumables;
    private RecyclerView recyclerInventory;

    private InventoryDao inventoryDao;
    private List<InventoryEntity> masterInventoryList = new ArrayList<>();
    private AdminInventoryAdapter adapter;

    private int currentBranchId = 1; // 1 = Colombo, 2 = Galle
    private String currentCategory = "All";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_inventory);

        inventoryDao = AppDatabase.getInstance(this).inventoryDao();

        // Bind persistent bottom navigation listeners
        setupBottomNavigation("Inventory");

        findViewById(R.id.tvAdminLogout).setOnClickListener(v -> {
            getSharedPreferences("TechFixPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        etSearchInventory = findViewById(R.id.etSearchInventory);
        btnAddInventory = findViewById(R.id.btnAddInventory);

        tvStatTotalItems = findViewById(R.id.tvStatTotalItems);
        tvStatGoodCondition = findViewById(R.id.tvStatGoodCondition);
        tvStatNeedsAttention = findViewById(R.id.tvStatNeedsAttention);

        tabAll = findViewById(R.id.tabAll);
        tabTools = findViewById(R.id.tabTools);
        tabEquipment = findViewById(R.id.tabEquipment);
        tabConsumables = findViewById(R.id.tabConsumables);

        recyclerInventory = findViewById(R.id.recyclerInventory);
        recyclerInventory.setLayoutManager(new LinearLayoutManager(this));

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInventoryData();
    }

    private void setupListeners() {
        etSearchInventory.addTextChangedListener(new TextWatcher() {
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

        btnAddInventory.setOnClickListener(v -> {
            Intent intent = new Intent(AdminInventoryActivity.this, ManageInventoryActivity.class);
            intent.putExtra("BRANCH_ID", currentBranchId);
            startActivity(intent);
        });

        View.OnClickListener tabClickListener = v -> {
            TextView clickedTab = (TextView) v;
            currentCategory = clickedTab.getText().toString();
            updateTabStyles();
            filterAndCalculateStats();
        };

        tabAll.setOnClickListener(tabClickListener);
        tabTools.setOnClickListener(tabClickListener);
        tabEquipment.setOnClickListener(tabClickListener);
        tabConsumables.setOnClickListener(tabClickListener);
    }

    private void updateTabStyles() {
        TextView[] tabs = {tabAll, tabTools, tabEquipment, tabConsumables};
        for (TextView tab : tabs) {
            tab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#161618")));
            tab.setTextColor(Color.parseColor("#888888"));
            tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        TextView activeTab = tabAll;
        if (currentCategory.equals("Tools")) activeTab = tabTools;
        else if (currentCategory.equals("Equipment")) activeTab = tabEquipment;
        else if (currentCategory.equals("Consumables")) activeTab = tabConsumables;

        activeTab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#BB86FC")));
        activeTab.setTextColor(Color.parseColor("#000000"));
        activeTab.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void loadInventoryData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            masterInventoryList = inventoryDao.getAllInventory();
            runOnUiThread(this::filterAndCalculateStats);
        });
    }

    private void filterAndCalculateStats() {
        List<InventoryEntity> currentBranchList = new ArrayList<>();
        List<InventoryEntity> displayList = new ArrayList<>();

        int totalItems = 0;
        int goodCondition = 0;
        int needsAttention = 0;

        for (InventoryEntity item : masterInventoryList) {
            if (item.branchId == currentBranchId) {
                currentBranchList.add(item);
                totalItems++;

                String status = item.status != null ? item.status : "Good";
                if (status.equalsIgnoreCase("Good")) {
                    goodCondition++;
                } else if (status.equalsIgnoreCase("Fair") || status.equalsIgnoreCase("Needs Attention")) {
                    needsAttention++;
                }
            }
        }

        tvStatTotalItems.setText(String.valueOf(totalItems));
        tvStatGoodCondition.setText(String.valueOf(goodCondition));
        tvStatNeedsAttention.setText(String.valueOf(needsAttention));

        for (InventoryEntity item : currentBranchList) {
            boolean matchesCategory = currentCategory.equals("All") || (item.category != null && item.category.equalsIgnoreCase(currentCategory));
            boolean matchesSearch = currentSearchQuery.isEmpty() || (item.itemName != null && item.itemName.toLowerCase().contains(currentSearchQuery));

            if (matchesCategory && matchesSearch) {
                displayList.add(item);
            }
        }

        adapter = new AdminInventoryAdapter(displayList, new AdminInventoryAdapter.InventoryActionListener() {
            @Override
            public void onEditClick(InventoryEntity item) {
                Intent intent = new Intent(AdminInventoryActivity.this, ManageInventoryActivity.class);
                intent.putExtra("ITEM_ID", item.id);
                intent.putExtra("BRANCH_ID", currentBranchId);
                intent.putExtra("ITEM_NAME", item.itemName);
                intent.putExtra("ITEM_CATEGORY", item.category);
                intent.putExtra("ITEM_QTY", item.quantity);
                intent.putExtra("ITEM_STATUS", item.status);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(InventoryEntity item) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    inventoryDao.deleteInventory(item);
                    runOnUiThread(() -> {
                        Toast.makeText(AdminInventoryActivity.this, "Deleted " + item.itemName, Toast.LENGTH_SHORT).show();
                        loadInventoryData();
                    });
                });
            }
        });
        recyclerInventory.setAdapter(adapter);
    }

    private static class AdminInventoryAdapter extends RecyclerView.Adapter<AdminInventoryAdapter.ViewHolder> {
        private List<InventoryEntity> list;
        private InventoryActionListener listener;

        public interface InventoryActionListener {
            void onEditClick(InventoryEntity item);
            void onDeleteClick(InventoryEntity item);
        }

        public AdminInventoryAdapter(List<InventoryEntity> list, InventoryActionListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_inventory, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            InventoryEntity item = list.get(position);

            holder.tvItemName.setText(item.itemName);
            holder.tvItemQty.setText(String.valueOf(item.quantity));
            holder.tvItemValue.setText("$" + (int) item.value);

            String branchName = item.branchId == 2 ? "TechFix - Galle Branch" : "TechFix - Colombo Branch";
            holder.tvItemCategoryBranch.setText((item.category != null ? item.category : "Uncategorized") + " · " + branchName);

            String status = item.status != null ? item.status : "Good";
            holder.tvItemStatus.setText(status);

            if (status.equalsIgnoreCase("Good")) {
                holder.tvItemStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.tvItemStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A4CAF50")));
            } else {
                holder.tvItemStatus.setTextColor(Color.parseColor("#FFB74D"));
                holder.tvItemStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFFB74D")));
            }

            holder.btnEditItem.setOnClickListener(v -> listener.onEditClick(item));
            holder.btnDeleteItem.setOnClickListener(v -> listener.onDeleteClick(item));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName, tvItemStatus, tvItemCategoryBranch, tvItemQty, tvItemValue;
            MaterialCardView btnEditItem, btnDeleteItem;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvItemStatus = itemView.findViewById(R.id.tvItemStatus);
                tvItemCategoryBranch = itemView.findViewById(R.id.tvItemCategoryBranch);
                tvItemQty = itemView.findViewById(R.id.tvItemQty);
                tvItemValue = itemView.findViewById(R.id.tvItemValue);
                btnEditItem = itemView.findViewById(R.id.btnEditItem);
                btnDeleteItem = itemView.findViewById(R.id.btnDeleteItem);
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