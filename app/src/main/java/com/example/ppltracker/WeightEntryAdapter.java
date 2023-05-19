package com.example.ppltracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.graphics.Color;

public class WeightEntryAdapter extends RecyclerView.Adapter<WeightEntryAdapter.WeightEntryViewHolder> {

    private Context context;
    private List<WeightEntry> weightEntries;
    private OnItemClickListener listener;
    private int selectedPosition = -1; // -1 indicates no item is selected

    public WeightEntryAdapter(Context context, List<WeightEntry> weightEntries) {
        this.context = context;
        this.weightEntries = weightEntries;
    }
    public void updateData(List<WeightEntry> newWeightEntryList) {
        this.weightEntries = newWeightEntryList;
        notifyDataSetChanged();
    }
    public void setWeightEntryList(List<WeightEntry> newWeightEntryList) {
        this.weightEntries = newWeightEntryList;
    }

    public interface OnItemClickListener {
        void onItemClick(WeightEntry weightEntry);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WeightEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_weight_entry, parent, false);
        return new WeightEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightEntryViewHolder holder, int position) {
        WeightEntry weightEntry = weightEntries.get(position);
        holder.txtWeight.setText(String.valueOf(weightEntry.getWeight()));
        holder.txtReps.setText(String.valueOf(weightEntry.getReps()));
        holder.txtSets.setText(String.valueOf(weightEntry.getSets()));
        holder.txtDate.setText(weightEntry.getDate());

        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(weightEntry);
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });
        }

        // Set background color for selected item
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#D3D3D3")); // Light Gray
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#00000000")); // Transparent
        }
    }

    public void deselectCurrentItem() {
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return weightEntries.size();
    }

    public static class WeightEntryViewHolder extends RecyclerView.ViewHolder {

        TextView txtWeight;
        TextView txtReps;
        TextView txtSets;
        TextView txtDate;

        public WeightEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtWeight = itemView.findViewById(R.id.txtWeight);
            txtReps = itemView.findViewById(R.id.txtReps);
            txtSets = itemView.findViewById(R.id.txtSets);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}
