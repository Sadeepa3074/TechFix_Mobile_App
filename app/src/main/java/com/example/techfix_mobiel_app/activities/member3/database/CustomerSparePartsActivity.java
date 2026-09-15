package com.example.techfix_mobiel_app.activities.member3.database;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.network.ApiService;
import com.example.techfix_mobiel_app.network.RetrofitClient;
import com.example.techfix_mobiel_app.activities.member4.database.entities.SparePartEntity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerSparePartsActivity extends AppCompatActivity {

    private EditText etSearchParts;
    private RelativeLayout btnCartContainer;
    private TextView tvCartBadge;
    private RecyclerView recyclerCustomerParts;

    private TextView chipAll, chipPhones, chipLaptops, chipTablets, chipAccessories;

    private List<SparePartEntity> masterSparePartsList = new ArrayList<>();
    private String currentSearchQuery = "";
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_spare_parts);

        etSearchParts = findViewById(R.id.etSearchParts);
        btnCartContainer = findViewById(R.id.btnCartContainer);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        recyclerCustomerParts = findViewById(R.id.recyclerCustomerParts);
        recyclerCustomerParts.setLayoutManager(new LinearLayoutManager(this));

        chipAll = findViewById(R.id.chipAll);
        chipPhones = findViewById(R.id.chipPhones);
        chipLaptops = findViewById(R.id.chipLaptops);
        chipTablets = findViewById(R.id.chipTablets);
        chipAccessories = findViewById(R.id.chipAccessories);

        setupListeners();
    }

    private void setupListeners() {
        findViewById(R.id.btnBackCustomerParts).setOnClickListener(v -> finish());

        etSearchParts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                filterAndRender();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCartContainer.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSparePartsActivity.this, CartActivity.class);
            startActivity(intent);
        });

        View.OnClickListener categoryClickListener = v -> {
            int id = v.getId();
            if (id == R.id.chipAll) {
                selectedCategory = "All";
            } else if (id == R.id.chipPhones) {
                selectedCategory = "Phones";
            } else if (id == R.id.chipLaptops) {
                selectedCategory = "Laptops";
            } else if (id == R.id.chipTablets) {
                selectedCategory = "Tablets";
            } else if (id == R.id.chipAccessories) {
                selectedCategory = "Accessories";
            }

            updateChipStyles();
            filterAndRender();
        };

        chipAll.setOnClickListener(categoryClickListener);
        chipPhones.setOnClickListener(categoryClickListener);
        chipLaptops.setOnClickListener(categoryClickListener);
        chipTablets.setOnClickListener(categoryClickListener);
        chipAccessories.setOnClickListener(categoryClickListener);
    }

    private void updateChipStyles() {
        setChipState(chipAll, selectedCategory.equalsIgnoreCase("All"));
        setChipState(chipPhones, selectedCategory.equalsIgnoreCase("Phones"));
        setChipState(chipLaptops, selectedCategory.equalsIgnoreCase("Laptops"));
        setChipState(chipTablets, selectedCategory.equalsIgnoreCase("Tablets"));
        setChipState(chipAccessories, selectedCategory.equalsIgnoreCase("Accessories"));
    }

    private void setChipState(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BB86FC")));
            chip.setTextColor(Color.parseColor("#000000"));
        } else {
            chip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#141416")));
            chip.setTextColor(Color.parseColor("#888888"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        loadSpareParts();
    }

    private void updateCartBadge() {
        int cartCount = getSharedPreferences("TechFixPrefs", MODE_PRIVATE).getInt("CART_COUNT", 0);
        if (cartCount > 0) {
            tvCartBadge.setText(String.valueOf(cartCount));
            tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void loadSpareParts() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getSpareParts().enqueue(new Callback<List<SparePartEntity>>() {
            @Override
            public void onResponse(Call<List<SparePartEntity>> call, Response<List<SparePartEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    masterSparePartsList = response.body();
                    filterAndRender();
                } else {
                    Toast.makeText(CustomerSparePartsActivity.this, "Failed to load spare parts from server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SparePartEntity>> call, Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(CustomerSparePartsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndRender() {
        List<SparePartEntity> displayList = new ArrayList<>();
        for (SparePartEntity item : masterSparePartsList) {
            boolean matchesCategory = selectedCategory.equalsIgnoreCase("All") ||
                    (item.category != null && item.category.equalsIgnoreCase(selectedCategory));

            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    (item.partName != null && item.partName.toLowerCase().contains(currentSearchQuery)) ||
                    (item.category != null && item.category.toLowerCase().contains(currentSearchQuery));

            if (matchesCategory && matchesSearch) {
                displayList.add(item);
            }
        }

        CustomerSparePartsAdapter adapter = new CustomerSparePartsAdapter(displayList, item -> {
            Intent intent = new Intent(CustomerSparePartsActivity.this, ReserveSparePartActivity.class);
            intent.putExtra("PART_ID", item.id);
            intent.putExtra("PART_NAME", item.partName);
            intent.putExtra("PART_CATEGORY", item.category);
            intent.putExtra("PART_PRICE", item.price);
            intent.putExtra("PART_QTY", item.quantity);
            startActivity(intent);
        });
        recyclerCustomerParts.setAdapter(adapter);
    }

    private static class CustomerSparePartsAdapter extends RecyclerView.Adapter<CustomerSparePartsAdapter.ViewHolder> {
        private final List<SparePartEntity> list;
        private final OnReserveClickListener listener;

        public interface OnReserveClickListener {
            void onReserveClick(SparePartEntity item);
        }

        public CustomerSparePartsAdapter(List<SparePartEntity> list, OnReserveClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_spare_part, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SparePartEntity item = list.get(position);

            holder.tvPartName.setText(item.partName);
            holder.tvPartCategory.setText(item.category != null ? item.category : "General");
            holder.tvPartPrice.setText("$" + (int) item.price);
            holder.tvStockLeft.setText(item.quantity + " left");

            if (item.quantity == 0) {
                holder.tvPartStatus.setText("Out of Stock");
                holder.tvPartStatus.setTextColor(Color.parseColor("#FF5252"));
                holder.tvPartStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFF5252")));
                holder.btnReservePart.setEnabled(false);
                holder.btnReservePart.setAlpha(0.4f);
            } else if (item.quantity < 10) {
                holder.tvPartStatus.setText("Low Stock");
                holder.tvPartStatus.setTextColor(Color.parseColor("#FFB74D"));
                holder.tvPartStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFFB74D")));
                holder.btnReservePart.setEnabled(true);
                holder.btnReservePart.setAlpha(1.0f);
            } else {
                holder.tvPartStatus.setText("In Stock");
                holder.tvPartStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.tvPartStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A4CAF50")));
                holder.btnReservePart.setEnabled(true);
                holder.btnReservePart.setAlpha(1.0f);
            }

            holder.btnReservePart.setOnClickListener(v -> listener.onReserveClick(item));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPartName, tvPartCategory, tvPartStatus, tvPartPrice, tvStockLeft;
            MaterialButton btnReservePart;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPartName = itemView.findViewById(R.id.tvPartName);
                tvPartCategory = itemView.findViewById(R.id.tvPartCategory);
                tvPartStatus = itemView.findViewById(R.id.tvPartStatus);
                tvPartPrice = itemView.findViewById(R.id.tvPartPrice);
                tvStockLeft = itemView.findViewById(R.id.tvStockLeft);
                btnReservePart = itemView.findViewById(R.id.btnReservePart);
            }
        }
    }
}