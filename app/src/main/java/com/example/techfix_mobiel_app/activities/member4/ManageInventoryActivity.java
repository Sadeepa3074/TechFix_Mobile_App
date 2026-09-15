package com.example.techfix_mobiel_app.activities.member4;

import android.graphics.Color;
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
import com.example.techfix_mobiel_app.database.AppDatabase;
import com.example.techfix_mobiel_app.activities.member4.database.entities.InventoryEntity;

import java.util.concurrent.Executors;

public class ManageInventoryActivity extends AppCompatActivity {

    private EditText etItemName, etQuantity;
    private AutoCompleteTextView autoCompleteCategory, autoCompleteLocation;
    private TextView btnConditionGood, btnConditionFair, btnConditionPoor;
    private MaterialButton btnSaveInventory;
    private ImageView btnClose;

    private int branchId = 1;
    private int itemId = -1;
    private boolean isEditMode = false;
    private String selectedCondition = "Good";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_inventory);

        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        autoCompleteLocation = findViewById(R.id.autoCompleteLocation);

        btnConditionGood = findViewById(R.id.btnConditionGood);
        btnConditionFair = findViewById(R.id.btnConditionFair);
        btnConditionPoor = findViewById(R.id.btnConditionPoor);

        btnSaveInventory = findViewById(R.id.btnSaveInventory);
        btnClose = findViewById(R.id.btnClose);

        setupDropdowns();

        // Retrieve Intent Data
        branchId = getIntent().getIntExtra("BRANCH_ID", 1);
        itemId = getIntent().getIntExtra("ITEM_ID", -1);
        String incomingName = getIntent().getStringExtra("ITEM_NAME");
        String incomingCategory = getIntent().getStringExtra("ITEM_CATEGORY");
        int incomingQty = getIntent().getIntExtra("ITEM_QTY", -1);
        String incomingStatus = getIntent().getStringExtra("ITEM_STATUS");

        // Set Location
        if (branchId == 2) {
            autoCompleteLocation.setText("TechFix - Galle Branch", false);
        } else {
            autoCompleteLocation.setText("TechFix - Colombo Branch", false);
        }

        // If Editing, Pre-fill fields with live data
        if (itemId != -1) {
            isEditMode = true;
            btnSaveInventory.setText("Update Item");

            if (incomingName != null) {
                etItemName.setText(incomingName);
            }
            if (incomingCategory != null) {
                autoCompleteCategory.setText(incomingCategory, false);
            }
            if (incomingQty != -1) {
                etQuantity.setText(String.valueOf(incomingQty));
            }
            if (incomingStatus != null) {
                setCondition(incomingStatus);
            }
        }

        setupListeners();
    }

    private void setupDropdowns() {
        String[] categories = new String[]{"Tools", "Equipment", "Consumables"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown, categories);
        autoCompleteCategory.setAdapter(categoryAdapter);

        String[] locations = new String[]{"TechFix - Colombo Branch", "TechFix - Galle Branch"};
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown, locations);
        autoCompleteLocation.setAdapter(locationAdapter);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());

        btnConditionGood.setOnClickListener(v -> setCondition("Good"));
        btnConditionFair.setOnClickListener(v -> setCondition("Fair"));
        btnConditionPoor.setOnClickListener(v -> setCondition("Poor"));

        btnSaveInventory.setOnClickListener(v -> saveInventoryToDatabase());
    }

    private void setCondition(String condition) {
        selectedCondition = condition;

        btnConditionGood.setTextColor(Color.parseColor("#888888"));
        btnConditionFair.setTextColor(Color.parseColor("#888888"));
        btnConditionPoor.setTextColor(Color.parseColor("#888888"));

        if (condition.equalsIgnoreCase("Good")) {
            btnConditionGood.setTextColor(Color.parseColor("#4CAF50"));
        } else if (condition.equalsIgnoreCase("Fair")) {
            btnConditionFair.setTextColor(Color.parseColor("#FFB74D"));
        } else if (condition.equalsIgnoreCase("Poor")) {
            btnConditionPoor.setTextColor(Color.parseColor("#E53935"));
        }
    }

    private void saveInventoryToDatabase() {
        String name = etItemName.getText().toString().trim();
        String category = autoCompleteCategory.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();
        String locationSelection = autoCompleteLocation.getText().toString().trim();

        if (name.isEmpty() || qtyStr.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty = Integer.parseInt(qtyStr);

        if (locationSelection.contains("Galle")) {
            branchId = 2;
        } else {
            branchId = 1;
        }

        InventoryEntity item = new InventoryEntity();
        if (isEditMode) {
            item.id = itemId;
        }
        item.itemName = name;
        item.category = category.isEmpty() ? "Tools" : category;
        item.branchId = branchId;
        item.quantity = qty;
        item.value = 0.0;
        item.status = selectedCondition;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (isEditMode) {
                db.inventoryDao().update(item);
            } else {
                db.inventoryDao().insertInventory(item);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, isEditMode ? "Item updated successfully!" : "Inventory item saved successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}