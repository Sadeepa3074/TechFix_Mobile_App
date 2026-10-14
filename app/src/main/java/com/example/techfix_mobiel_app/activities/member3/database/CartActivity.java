package com.example.techfix_mobiel_app.activities.member3.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.database.AppDatabase;
import com.example.techfix_mobiel_app.activities.member4.database.dao.SparePartDao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private ImageView btnBackCart;
    private TextView btnClearCart, tvCartTotalPrice;
    private LinearLayout layoutEmptyCart;
    private RecyclerView recyclerCart;
    private MaterialButton btnCheckout;

    private List<CartItem> cartList = new ArrayList<>();
    private CartAdapter adapter;
    private SparePartDao sparePartDao;

    public static class CartItem {
        public int id;
        public String name;
        public String category;
        public double price;
        public int qty;
        public int maxStock;

        public CartItem(int id, String name, String category, double price, int qty, int maxStock) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.qty = qty;
            this.maxStock = maxStock;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        sparePartDao = AppDatabase.getInstance(this).sparePartDao();

        btnBackCart = findViewById(R.id.btnBackCart);
        btnClearCart = findViewById(R.id.btnClearCart);
        tvCartTotalPrice = findViewById(R.id.tvCartTotalPrice);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        recyclerCart = findViewById(R.id.recyclerCart);
        btnCheckout = findViewById(R.id.btnCheckout);

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));

        btnBackCart.setOnClickListener(v -> finish());
        btnClearCart.setOnClickListener(v -> clearCart());
        btnCheckout.setOnClickListener(v -> processCheckout());

        loadCartData();
    }

    private void loadCartData() {
        cartList.clear();
        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
        String cartJson = prefs.getString("CART_ITEMS_JSON", "[]");

        try {
            JSONArray array = new JSONArray(cartJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                cartList.add(new CartItem(
                        obj.getInt("id"),
                        obj.getString("name"),
                        obj.optString("category", "General"),
                        obj.getDouble("price"),
                        obj.getInt("qty"),
                        obj.optInt("maxStock", 99)
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        renderCart();
    }

    private void saveCartToPrefs() {
        SharedPreferences prefs = getSharedPreferences("TechFixPrefs", MODE_PRIVATE);
        JSONArray array = new JSONArray();
        int totalQtyCount = 0;

        try {
            for (CartItem item : cartList) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("name", item.name);
                obj.put("category", item.category);
                obj.put("price", item.price);
                obj.put("qty", item.qty);
                obj.put("maxStock", item.maxStock);
                array.put(obj);

                totalQtyCount += item.qty;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        prefs.edit()
                .putString("CART_ITEMS_JSON", array.toString())
                .putInt("CART_COUNT", totalQtyCount)
                .apply();
    }

    private void renderCart() {
        if (cartList.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            recyclerCart.setVisibility(View.GONE);
            btnCheckout.setEnabled(false);
            btnCheckout.setAlpha(0.4f);
            tvCartTotalPrice.setText("$0");
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            recyclerCart.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(true);
            btnCheckout.setAlpha(1.0f);

            calculateTotal();

            adapter = new CartAdapter(cartList, new CartAdapter.OnCartActionListener() {
                @Override
                public void onQtyChanged(int position, int newQty) {
                    cartList.get(position).qty = newQty;
                    saveCartToPrefs();
                    calculateTotal();
                }

                @Override
                public void onItemRemoved(int position) {
                    cartList.remove(position);
                    saveCartToPrefs();
                    renderCart();
                }
            });
            recyclerCart.setAdapter(adapter);
        }
    }

    private void calculateTotal() {
        double total = 0.0;
        for (CartItem item : cartList) {
            total += (item.price * item.qty);
        }
        tvCartTotalPrice.setText("$" + (int) total);
    }

    private void clearCart() {
        cartList.clear();
        saveCartToPrefs();
        renderCart();
        Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show();
    }

    private void processCheckout() {
        if (cartList.isEmpty()) return;

        double total = 0.0;
        for (CartItem item : cartList) {
            total += (item.price * item.qty);
        }

        Intent intent = new Intent(CartActivity.this, PaymentGatewayActivity.class);
        intent.putExtra("TOTAL_AMOUNT", total);
        startActivity(intent);
    }

    private static class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
        private final List<CartItem> list;
        private final OnCartActionListener listener;

        public interface OnCartActionListener {
            void onQtyChanged(int position, int newQty);
            void onItemRemoved(int position);
        }

        public CartAdapter(List<CartItem> list, OnCartActionListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_part, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItem item = list.get(position);

            holder.tvCartPartName.setText(item.name);
            holder.tvCartPartCategory.setText(item.category);
            holder.tvCartPartPrice.setText("$" + (int) (item.price * item.qty));
            holder.tvCartQty.setText(String.valueOf(item.qty));

            holder.btnCartMinus.setOnClickListener(v -> {
                if (item.qty > 1) {
                    listener.onQtyChanged(position, item.qty - 1);
                    notifyItemChanged(position);
                }
            });

            holder.btnCartPlus.setOnClickListener(v -> {
                if (item.qty < item.maxStock) {
                    listener.onQtyChanged(position, item.qty + 1);
                    notifyItemChanged(position);
                }
            });

            holder.btnRemoveCartItem.setOnClickListener(v -> listener.onItemRemoved(position));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCartPartName, tvCartPartCategory, tvCartPartPrice, tvCartQty;
            ImageView btnRemoveCartItem;
            MaterialCardView btnCartMinus, btnCartPlus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCartPartName = itemView.findViewById(R.id.tvCartPartName);
                tvCartPartCategory = itemView.findViewById(R.id.tvCartPartCategory);
                tvCartPartPrice = itemView.findViewById(R.id.tvCartPartPrice);
                tvCartQty = itemView.findViewById(R.id.tvCartQty);
                btnRemoveCartItem = itemView.findViewById(R.id.btnRemoveCartItem);
                btnCartMinus = itemView.findViewById(R.id.btnCartMinus);
                btnCartPlus = itemView.findViewById(R.id.btnCartPlus);
            }
        }
    }
}