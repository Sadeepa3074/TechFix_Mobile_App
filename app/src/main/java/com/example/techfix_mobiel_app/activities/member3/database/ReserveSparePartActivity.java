package com.example.techfix_mobiel_app.activities.member3.database;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.techfix_mobiel_app.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReserveSparePartActivity extends AppCompatActivity {

    private TextView tvModalPartName, tvModalPartCategory, tvModalUnitPrice, tvModalMaxStock;
    private TextView tvSelectedQty, tvTotalPrice;
    private MaterialCardView btnMinus, btnPlus;
    private MaterialButton btnAddToCart;
    private ImageView btnClose;

    private int availableStock = 1;
    private double unitPrice = 0.0;
    private int selectedQty = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_spare_part);

        tvModalPartName = findViewById(R.id.tvModalPartName);
        tvModalPartCategory = findViewById(R.id.tvModalPartCategory);
        tvModalUnitPrice = findViewById(R.id.tvModalUnitPrice);
        tvModalMaxStock = findViewById(R.id.tvModalMaxStock);
        tvSelectedQty = findViewById(R.id.tvSelectedQty);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnClose = findViewById(R.id.btnClose);

        // Read intent extras passed from Customer UI
        String name = getIntent().getStringExtra("PART_NAME");
        String category = getIntent().getStringExtra("PART_CATEGORY");
        unitPrice = getIntent().getDoubleExtra("PART_PRICE", 0.0);
        availableStock = getIntent().getIntExtra("PART_QTY", 1);

        tvModalPartName.setText(name != null ? name : "Spare Part");
        tvModalPartCategory.setText(category != null ? category : "General");
        tvModalUnitPrice.setText("$" + (int) unitPrice + " per unit");
        tvModalMaxStock.setText("Available: " + availableStock);

        updatePriceAndQty();

        btnMinus.setOnClickListener(v -> {
            if (selectedQty > 1) {
                selectedQty--;
                updatePriceAndQty();
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (selectedQty < availableStock) {
                selectedQty++;
                updatePriceAndQty();
            } else {
                Toast.makeText(this, "Cannot select more than available stock", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose.setOnClickListener(v -> finish());

        btnAddToCart.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
            String cartJson = prefs.getString("CART_ITEMS_JSON", "[]");

            try {
                JSONArray array = new JSONArray(cartJson);
                boolean foundExisting = false;

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    if (obj.getInt("id") == getIntent().getIntExtra("PART_ID", -1)) {
                        int existingQty = obj.getInt("qty");
                        obj.put("qty", existingQty + selectedQty);
                        foundExisting = true;
                        break;
                    }
                }

                if (!foundExisting) {
                    JSONObject newObj = new JSONObject();
                    newObj.put("id", getIntent().getIntExtra("PART_ID", -1));
                    newObj.put("name", getIntent().getStringExtra("PART_NAME"));
                    newObj.put("category", getIntent().getStringExtra("PART_CATEGORY"));
                    newObj.put("price", unitPrice);
                    newObj.put("qty", selectedQty);
                    newObj.put("maxStock", availableStock);
                    array.put(newObj);
                }

                int currentCartCount = prefs.getInt("CART_COUNT", 0);
                prefs.edit()
                        .putString("CART_ITEMS_JSON", array.toString())
                        .putInt("CART_COUNT", currentCartCount + selectedQty)
                        .apply();

                Toast.makeText(this, "Added " + selectedQty + " item(s) to Cart!", Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updatePriceAndQty() {
        tvSelectedQty.setText(String.valueOf(selectedQty));
        double total = selectedQty * unitPrice;
        tvTotalPrice.setText("$" + (int) total);
    }
}