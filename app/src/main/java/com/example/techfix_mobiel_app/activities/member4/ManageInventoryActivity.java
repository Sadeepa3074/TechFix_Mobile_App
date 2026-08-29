package com.example.techfix_mobiel_app.activities.member4;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.adapters.InventoryAdapter;
import com.example.techfix_mobiel_app.database.AppDatabase;
import com.example.techfix_mobiel_app.database.entities.SparePartEntity;

import java.util.List;
import java.util.concurrent.Executors;

public class ManageInventoryActivity extends AppCompatActivity {

    private EditText etName, etBranch, etQty, etPrice;
    private Button btnAdd;
    private RecyclerView rvInventory;
    private InventoryAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_inventory);

        db = AppDatabase.getInstance(this);

        etName = findViewById(R.id.etPartName);
        etBranch = findViewById(R.id.etBranch);
        etQty = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
        btnAdd = findViewById(R.id.btnAddPart);
        rvInventory = findViewById(R.id.rvInventory);

        rvInventory.setLayoutManager(new LinearLayoutManager(this));
        loadInventoryData();

        btnAdd.setOnClickListener(v -> savePartToDatabase());
    }

    private void savePartToDatabase() {
        String name = etName.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String qtyStr = etQty.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (name.isEmpty() || branch.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty = Integer.parseInt(qtyStr);
        double price = Double.parseDouble(priceStr);

        SparePartEntity newPart = new SparePartEntity(name, branch, qty, price);

        Executors.newSingleThreadExecutor().execute(() -> {
            db.inventoryDao().insertPart(newPart);
            runOnUiThread(() -> {
                Toast.makeText(ManageInventoryActivity.this, "Saved locally in SQLite!", Toast.LENGTH_SHORT).show();
                clearInputs();
                loadInventoryData();
            });
        });
    }

    private void loadInventoryData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SparePartEntity> list = db.inventoryDao().getAllParts();
            runOnUiThread(() -> {
                if (adapter == null) {
                    adapter = new InventoryAdapter(list, part -> {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.inventoryDao().deletePart(part);
                            runOnUiThread(() -> {
                                Toast.makeText(ManageInventoryActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                                loadInventoryData();
                            });
                        });
                    });
                    rvInventory.setAdapter(adapter);
                } else {
                    adapter.setPartList(list);
                }
            });
        });
    }

    private void clearInputs() {
        etName.setText("");
        etBranch.setText("");
        etQty.setText("");
        etPrice.setText("");
    }
}