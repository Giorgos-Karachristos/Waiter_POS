package com.waiterpos.ui.waiter.menu;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.waiterpos.databinding.MenuItemAdapterBinding;
import com.waiterpos.entities.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ItemViewHolder> {

    private final List<Item> items;
    private final Map<String, Integer> quantityMap = new HashMap<>();
    private final String tax;
    private final OnQuantityChangeListener listener;

    private int expandedPosition = -1;

    public interface OnQuantityChangeListener {
        void onQuantityChanged(String itemId, int quantity, double totalPrice);
    }

    public MenuItemAdapter(List<Item> items, String tax, OnQuantityChangeListener listener) {
        this.items = items;
        this.tax = tax;
        this.listener = listener;

        for (Item item : items) {
            quantityMap.put(item.getItemID(), 0);
        }
    }

    public void updateData(List<Item> newItems) {
        items.clear();
        items.addAll(newItems);
        quantityMap.clear();
        for (Item item : newItems) {
            quantityMap.put(item.getItemID(), 0);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MenuItemAdapterBinding binding = MenuItemAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MenuItemAdapter.ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item);

        if (item.getColor() != null) {
            try {
                holder.itemView.setBackgroundColor(Color.parseColor(item.getColor()));
            } catch (IllegalArgumentException e) {
                Log.e("MenuItemAdapter", "Invalid color: " + item.getColor());
            }
        }

        final boolean isExpanded = position == expandedPosition;
        holder.descriptionText.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Handle click event to toggle expansion
        holder.itemView.setOnClickListener(v -> {
            expandedPosition = isExpanded ? -1 : position;
            if (holder.descriptionText.getText().length() == 0) {
                expandedPosition = -1;
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final MenuItemAdapterBinding binding;
        private final TextView quantityText, priceText, totalPriceText, descriptionText;

        public ItemViewHolder(MenuItemAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            quantityText = binding.quantity;
            priceText = binding.price;
            totalPriceText = binding.totalPrice;
            descriptionText = binding.textDescription;
        }

        public void bind(Item item) {
            binding.itemId.setText(item.getItemID());
            binding.itemName.setText(item.getName());
            descriptionText.setText(item.getDescription());

            // Parse price string to double safely
            double price = 0;
            try {
                price = Double.parseDouble(item.getPrice());
            } catch (NumberFormatException e) {
                Log.e("MenuItemAdapter", "Invalid price: " + item.getPrice());
            }
            double taxNumber = Double.parseDouble(tax);
            double priceInclTax = price + price * taxNumber / 100;
            double roundPriceInclTax = Math.round(priceInclTax * 100.0) / 100.0;
            priceText.setText(String.format("%.2f", roundPriceInclTax));

            // Get quantity from map
            int quantity = quantityMap.getOrDefault(item.getItemID(), 0);
            quantityText.setText(String.valueOf(quantity));
            totalPriceText.setText(String.format("%.2f", roundPriceInclTax * quantity));

            binding.minusButton.setOnClickListener(v -> {
                int qty = quantityMap.getOrDefault(item.getItemID(), 0);
                if (qty > 0) {
                    qty--;
                    quantityMap.put(item.getItemID(), qty);
                    quantityText.setText(String.valueOf(qty));
                    totalPriceText.setText(String.format("%.2f", roundPriceInclTax * qty));
                    if (listener != null)
                        listener.onQuantityChanged(item.getItemID(), qty, roundPriceInclTax * qty);
                }
            });

            binding.plusButton.setOnClickListener(v -> {
                int qty = quantityMap.getOrDefault(item.getItemID(), 0);
                qty++;
                quantityMap.put(item.getItemID(), qty);
                quantityText.setText(String.valueOf(qty));
                totalPriceText.setText(String.format("%.2f", roundPriceInclTax * qty));
                if (listener != null)
                    listener.onQuantityChanged(item.getItemID(), qty, roundPriceInclTax * qty);
            });
        }
    }
}