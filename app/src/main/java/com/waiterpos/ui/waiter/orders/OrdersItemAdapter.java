package com.waiterpos.ui.waiter.orders;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.waiterpos.databinding.OrdersItemAdapterBinding;
import com.waiterpos.entities.Kitchen;

import java.util.List;

public class OrdersItemAdapter extends RecyclerView.Adapter<OrdersItemAdapter.OrderViewHolder> {
    private final List<Kitchen> items;
    private final OnItemClickListener listener;
    private int selectedItemPosition = RecyclerView.NO_POSITION;

    public OrdersItemAdapter(List<Kitchen> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OrdersItemAdapterBinding binding = OrdersItemAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrdersItemAdapter.OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<Kitchen> newItems) {
        items.clear();
        items.addAll(newItems);
        selectedItemPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Kitchen item);
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final OrdersItemAdapterBinding binding;

        public OrderViewHolder(OrdersItemAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Kitchen item, int position) {
            binding.valueItemNameTextView.setText(item.getName());
            binding.valueQuantityTextView.setText(item.getQuantity());
            binding.valueStatusTextView.setText(item.getStatus());

            itemView.setBackgroundColor(position == selectedItemPosition ? Color.LTGRAY : Color.TRANSPARENT);

            itemView.setOnClickListener(v -> {
                int previousPosition = selectedItemPosition;
                selectedItemPosition = getAdapterPosition();
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedItemPosition);
                listener.onItemClick(item);
            });
        }
    }
}