package com.waiterpos.ui.waiter.orders;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.waiterpos.R;
import com.waiterpos.databinding.FragmentOrdersBinding;
import com.waiterpos.entities.Kitchen;
import com.waiterpos.entities.Orders;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OrdersFragment extends Fragment {
    private FragmentOrdersBinding binding;
    private Spinner orderSpinner;
    private OrdersItemAdapter itemAdapter;
    private OrdersViewModel ordersViewModel;
    private int lastSelectedPosition = 0;
    private boolean keepSpinnerPosition = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ordersViewModel = new ViewModelProvider(requireActivity()).get(OrdersViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);

        orderSpinner = binding.orderSpinner;

        setupRecyclerView();
        setupObservers();

        refreshOrders();

        SwipeRefreshLayout swipeRefreshLayout = binding.swipeRefreshLayout;

        swipeRefreshLayout.setOnRefreshListener(() -> {
            keepSpinnerPosition = true;
            refreshOrders();

            swipeRefreshLayout.setRefreshing(false);
        });
        binding.voidButton.setOnClickListener(v -> voidOrder());
        binding.cancelButton.setOnClickListener(v -> cancelOrder());
        binding.payButton.setOnClickListener(v -> payOrder());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        RecyclerView orderRecyclerView = binding.orderRecyclerView;
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        itemAdapter = new OrdersItemAdapter(new ArrayList<>(), item -> ordersViewModel.setSelectedItemLD(item));
        orderRecyclerView.setAdapter(itemAdapter);
    }

    private void setupObservers() {
        ordersViewModel.getActiveOrders().observe(getViewLifecycleOwner(), ordersList -> {
            if (ordersList == null) {
                Toast.makeText(getContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ordersList.isEmpty()) {
                orderSpinner.setAdapter(null);

                binding.valueSubtotalTextView.setText("");
                binding.valueTypeTextView.setText("");
                binding.valueDateTimeTextView.setText("");

                itemAdapter.updateItems(Collections.emptyList());
                return;
            }

            List<String> orderNames = new ArrayList<>();
            for (Orders order : ordersList) {
                orderNames.add("Order " + order.getOrderID() + " " + order.getStatus());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, orderNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            orderSpinner.setAdapter(adapter);

            if (keepSpinnerPosition && lastSelectedPosition < ordersList.size()) {
                orderSpinner.setSelection(lastSelectedPosition);
            } else {
                orderSpinner.setSelection(0);
            }
            keepSpinnerPosition = false;

            orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    lastSelectedPosition = position;
                    Orders selectedOrder = ordersList.get(position);
                    ordersViewModel.setSelectedItemLD(null);
                    ordersViewModel.setSelectedOrderLD(selectedOrder);
                    int orderID = Integer.parseInt(selectedOrder.getOrderID());
                    fetchKitchenData(orderID);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    itemAdapter.updateItems(new ArrayList<>());
                }
            });
        });

        // Observe selected order
        ordersViewModel.selectedOrderLD.observe(getViewLifecycleOwner(), order -> {
            if (order != null) {
                binding.valueSubtotalTextView.setText(order.getSubtotal());
                binding.valueTypeTextView.setText(order.getType());
                binding.valueDateTimeTextView.setText(order.getOrderDate());
            }
        });

        // Repository errors
        ordersViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error Occurred", Toast.LENGTH_LONG).show();
                for (String er : error) {
                    Log.e("OrdersFragment", "Error: " + er);
                }
                ordersViewModel.clearError();
            }
        });
    }

    private void fetchKitchenData(int orderId) {
        ordersViewModel.getKitchenItems(orderId).observe(getViewLifecycleOwner(), kitchens -> {
            if (kitchens != null) {
                itemAdapter.updateItems(kitchens);
            } else {
                Toast.makeText(getContext(), "Failed to load kitchen data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void voidOrder() {
        Orders selectedOrder = ordersViewModel.selectedOrderLD.getValue();
        Kitchen selectedItem = ordersViewModel.selectedItemLD.getValue();

        if (selectedOrder == null || selectedItem == null) {
            Toast.makeText(getContext(), "Select order and item", Toast.LENGTH_SHORT).show();
            return;
        }
        int orderID = Integer.parseInt(selectedOrder.getOrderID());
        Log.i("voidOrder", "voidOrder: " + selectedOrder.getOrderID() + " selectedItem: " + selectedItem.getName());

        ordersViewModel.voidKitchen(orderID, selectedItem.getName()).observe(getViewLifecycleOwner(), response -> {
            Toast.makeText(getContext(), Objects.requireNonNullElse(response, "Failed to void item"), Toast.LENGTH_SHORT).show();
            ordersViewModel.setSelectedItemLD(null);
            keepSpinnerPosition = true;
            refreshOrders();
        });
    }

    private void cancelOrder() {
        Orders selectedOrder = ordersViewModel.selectedOrderLD.getValue();
        if (selectedOrder == null) {
            Toast.makeText(getContext(), "Select order first", Toast.LENGTH_SHORT).show();
            return;
        }
        int orderID = Integer.parseInt(selectedOrder.getOrderID());
        Log.i("cancelOrder", "cancelOrder: " + selectedOrder.getOrderID());

        ordersViewModel.cancelOrder(orderID).observe(getViewLifecycleOwner(), response -> {
            Toast.makeText(getContext(), Objects.requireNonNullElse(response, "Failed to cancel order"), Toast.LENGTH_SHORT).show();
            ordersViewModel.setSelectedOrderLD(null);
            ordersViewModel.setSelectedItemLD(null);
            refreshOrders();
        });
    }

    private void refreshOrders() {
        ordersViewModel.refreshOrders();
    }

    private void payOrder() {
        Orders selectedOrder = ordersViewModel.selectedOrderLD.getValue();
        if (selectedOrder == null) {
            Toast.makeText(getContext(), "No order selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString("ORDER_ID", selectedOrder.getOrderID());
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.nav_register, args);

        NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        menu.findItem(R.id.nav_menu).setEnabled(false);
        menu.findItem(R.id.nav_orders).setEnabled(false);
    }
}