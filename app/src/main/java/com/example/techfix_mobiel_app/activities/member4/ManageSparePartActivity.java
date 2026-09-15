package com.example.techfix_mobiel_app.activities.member4;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.activities.member4.database.entities.SparePartEntity;
import com.example.techfix_mobiel_app.network.ApiService;
import com.example.techfix_mobiel_app.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageSparePartActivity extends AppCompatActivity {

    private TextView tvModalTitle;
    private EditText etPartName, etPartQty, etPartPrice;
    private AutoCompleteTextView autoCompleteCategory;
    private MaterialButton btnSaveSparePart;
    private ImageView btnClose;

    private int partId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_spare_part);

        tvModalTitle = findViewById(R.id.tvModalTitle);
        etPartName = findViewById(R.id.etPartName);
        etPartQty = findViewById(R.id.etPartQty);
        etPartPrice = findViewById(R.id.etPartPrice);
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        btnSaveSparePart = findViewById(R.id.btnSaveSparePart);
        btnClose = findViewById(R.id.btnClose);

        setupDropdown();

        // Get Intent Extras[cite: 17]
        partId = getIntent().getIntExtra("PART_ID", -1);
        String incomingName = getIntent().getStringExtra("PART_NAME");
        String incomingCategory = getIntent().getStringExtra("PART_CATEGORY");
        int incomingQty = getIntent().getIntExtra("PART_QTY", -1);
        double incomingPrice = getIntent().getDoubleExtra("PART_PRICE", -1.0);

        // Pre-fill fields if Editing[cite: 17]
        if (partId != -1) {
            isEditMode = true;
            tvModalTitle.setText("Edit Spare Part");
            btnSaveSparePart.setText("Update Part");

            if (incomingName != null) etPartName.setText(incomingName);
            if (incomingCategory != null) autoCompleteCategory.setText(incomingCategory, false);
            if (incomingQty != -1) etPartQty.setText(String.valueOf(incomingQty));
            if (incomingPrice != -1.0) etPartPrice.setText(String.valueOf((int) incomingPrice));
        }

        btnClose.setOnClickListener(v -> finish());
        btnSaveSparePart.setOnClickListener(v -> savePartToDatabase());
    }

    private void setupDropdown() {
        String[] categories = new String[]{"Phones", "Laptops", "Tablets", "Accessories"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown, categories);
        autoCompleteCategory.setAdapter(adapter);
    }

    private void savePartToDatabase() {
        String name = etPartName.getText().toString().trim();
        String category = autoCompleteCategory.getText().toString().trim();
        String qtyStr = etPartQty.getText().toString().trim();
        String priceStr = etPartPrice.getText().toString().trim();

        if (name.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty = Integer.parseInt(qtyStr);
        double price = Double.parseDouble(priceStr);

        SparePartEntity part = new SparePartEntity();
        if (isEditMode) {
            part.id = partId;
        }
        part.partName = name;
        part.category = category.isEmpty() ? "Phones" : category;
        part.quantity = qty;
        part.price = price;

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.saveSparePart(part).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    if ("success".equals(result.get("status"))) {
                        Toast.makeText(ManageSparePartActivity.this, isEditMode ? "Part updated!" : "Spare part added!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ManageSparePartActivity.this, "Server error saving part", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ManageSparePartActivity.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ManageSparePartActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}