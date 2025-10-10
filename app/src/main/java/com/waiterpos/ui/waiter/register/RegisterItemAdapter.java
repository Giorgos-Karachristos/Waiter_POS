package com.waiterpos.ui.waiter.register;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.waiterpos.databinding.RegisterItemAdapterBinding;
import com.waiterpos.entities.Register;

import java.util.List;

public class RegisterItemAdapter extends RecyclerView.Adapter<RegisterItemAdapter.ItemViewHolder> {

    private final List<Register> items;

    public RegisterItemAdapter(List<Register> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RegisterItemAdapterBinding binding = RegisterItemAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public double getTotalPrice() {
        double total = 0;
        for (Register item : items) {
            double totalPrice = Double.parseDouble(item.getTotalPrice());
            total += totalPrice;
        }
        return total;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final RegisterItemAdapterBinding binding;

        public ItemViewHolder(RegisterItemAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Register item) {
            binding.itemValueTextView.setText(item.getName());
            binding.qtyValueTextView.setText(String.valueOf(item.getQuantity()));
            binding.taxValueTextView.setText(item.getTax() + " %");
            binding.priceValueTextView.setText(String.valueOf(item.getTotalPrice()));
        }
    }
}