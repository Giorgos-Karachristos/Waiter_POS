package com.waiterpos.ui.waiter.menu;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.waiterpos.databinding.FragmentMenuBinding;
import com.waiterpos.entities.Category;
import com.waiterpos.ui.waiter.WaiterActivity;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuFragment extends Fragment {
    private FragmentMenuBinding menuBinding;
    private MenuViewModel menuViewModel;
    private Double roundSubtotal = 0.0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        menuViewModel = new ViewModelProvider(requireActivity()).get(MenuViewModel.class);
        if (getArguments() != null) {
            int tables = getArguments().getInt("TABLES_KEY", 0);
            menuViewModel.setTablesMLD(tables);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        menuBinding = FragmentMenuBinding.inflate(inflater, container, false);
        ProgressBar progressBar = menuBinding.progressBar2;

        // Observe categories
        menuViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                progressBar.setVisibility(View.GONE);
                setupViewPager(categories);
            } else {
                Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_LONG).show();
            }
        });

        menuViewModel.tablesLiveData.observe(getViewLifecycleOwner(), this::setupSpinner);

        // Observe subtotal updates
        menuViewModel.subtotalLiveData.observe(getViewLifecycleOwner(), subtotal -> {
            roundSubtotal = Math.round(subtotal * 100.0) / 100.0;
            menuBinding.numberSubtotalTextView.setText(roundSubtotal.toString());
        });

        // Error observer
        menuViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error Occurred", Toast.LENGTH_LONG).show();
                for (String er : error) {
                    Log.e("MainFragment", "Error: " + er);
                }
                menuViewModel.clearError();
            }
        });

        return menuBinding.getRoot();
    }

    private void setupViewPager(List<Category> categories) {
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return MenuTabFragment.newInstance(categories.get(position).getName(), categories.get(position).getTax());
            }

            @Override
            public int getItemCount() {
                return categories.size();
            }
        };

        menuBinding.categoryViewPager.setAdapter(adapter);

        new TabLayoutMediator(menuBinding.categoryTabLayout, menuBinding.categoryViewPager, (tab, position) -> {
            try {
                int color = Color.parseColor(categories.get(position).getColor());
                tab.view.setBackgroundColor(color);
            } catch (IllegalArgumentException e) {
                Log.e("MenuFragment", "Invalid color: " + categories.get(position).getColor());
            }
            tab.setText(categories.get(position).getName());
        }).attach();

        menuBinding.kitchenButton.setOnClickListener(v -> kitchenButtonActionPerformed());
    }

    private void kitchenButtonActionPerformed() {
        WaiterActivity waiterActivity = (WaiterActivity) getActivity();
        if (waiterActivity == null) return;

        String name = waiterActivity.getNavigationHeaderText();

        if (roundSubtotal == 0) {
            Toast.makeText(getContext(), "Please add items to the order", Toast.LENGTH_LONG).show();
            return;
        }

        Spinner tablesSpinner = menuBinding.tableSpinner;
        if (tablesSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please select a table", Toast.LENGTH_LONG).show();
            return;
        }
        String type = tablesSpinner.getSelectedItem().toString();

        Log.i("kitchenButton", "name: " + name + " subtotal: " + roundSubtotal + " type: " + type);

        // Observe order insert response
        menuViewModel.insertOrderToKitchen(name, roundSubtotal, type).observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.contains("Insertion failed:")) {
                Toast.makeText(getContext(), response, Toast.LENGTH_LONG).show();
            } else if (response != null) {
                Toast.makeText(getContext(), "Order inserted successfully", Toast.LENGTH_LONG).show();
                insertKitchenItems();
            }
        });
    }

    private int success = 0;

    private void insertKitchenItems() {
        Map<String, Double> totals = menuViewModel.getItemTotals();
        Map<String, Integer> quantities = menuViewModel.getItemQuantities();

        success = 0;

        for (String itemId : quantities.keySet()) {
            int qty = quantities.get(itemId);
            double totalPrice = totals.get(itemId);

            Log.i("insertKitchenItems", "idItem: " + itemId + " quantity: " + qty + " totalPrice: " + totalPrice);
            menuViewModel.insertItemToOrder(Integer.parseInt(itemId), qty, totalPrice).observe(getViewLifecycleOwner(), response -> {
                if (response != null) {
                    Log.i("insertKitchenItems", "response: " + response);
                    success++;
                }
                if (success == quantities.size()) {
                    Toast.makeText(getContext(), "Successfully inserted: " + success + "/" + quantities.size(), Toast.LENGTH_SHORT).show();
                    menuViewModel.resetSubtotal();
                    menuBinding.numberSubtotalTextView.setText("0.0");
                    menuBinding.tableSpinner.setSelection(0);
                    setupViewPager(menuViewModel.getCategories().getValue());
                }
            });
        }
    }

    private void setupSpinner(int tables) {
        Spinner tablesSpinner = menuBinding.tableSpinner;
        List<String> items = new ArrayList<>();
        items.add("Select a table");
        for (int i = 1; i <= tables; i++) {
            items.add("Table " + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tablesSpinner.setAdapter(adapter);
        tablesSpinner.setSelection(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        menuBinding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        menuViewModel.resetSubtotal();
        menuBinding.tableSpinner.setSelection(0);
    }
}