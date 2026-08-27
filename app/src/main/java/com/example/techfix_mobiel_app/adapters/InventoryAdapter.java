package com.example.techfix_mobiel_app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.techfix_mobiel_app.R;
import com.example.techfix_mobiel_app.database.entities.SparePartEntity;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {
    private List<SparePartEntity> partList;

    public InventoryAdapter(List<SparePartEntity> partList) {
        this.partList = partList;
    }

    public void setPartList(List<SparePartEntity> partList) {
        this.partList = partList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spare_part, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SparePartEntity part = partList.get(position);
        holder.tvName.setText(part.partName + " (" + part.branchName + ")");
        holder.tvDetails.setText("Qty: " + part.stockQuantity + " | Price: $" + part.unitPrice);
    }

    @Override
    public int getItemCount() {
        return partList != null ? partList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPartName);
            tvDetails = itemView.findViewById(R.id.tvPartDetails);
        }
    }
}