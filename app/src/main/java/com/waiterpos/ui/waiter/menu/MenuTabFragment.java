package com.waiterpos.ui.waiter.menu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.waiterpos.databinding.FragmentMenuTabBinding;

import java.util.ArrayList;

public class MenuTabFragment extends Fragment {
    private String categoryName;
    private String tax;
    private MenuViewModel viewModel;

    public static MenuTabFragment newInstance(String categoryName, String tax) {
        Log.i("MenuTabFragment", "categoryName: " + categoryName + " tax: " + tax);
        MenuTabFragment fragment = new MenuTabFragment();
        Bundle args = new Bundle();
        args.putString("CATEGORY_NAME", categoryName);
        args.putString("CATEGORY_TAX", tax);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryName = getArguments().getString("CATEGORY_NAME");
            tax = getArguments().getString("CATEGORY_TAX");
        }

        viewModel = new ViewModelProvider(requireActivity()).get(MenuViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentMenuTabBinding binding = FragmentMenuTabBinding.inflate(inflater, container, false);
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ProgressBar progressBar = binding.progressBar;

        MenuItemAdapter adapter = new MenuItemAdapter(new ArrayList<>(), tax, (itemId, quantity, totalPrice) -> {
            viewModel.updateItemTotal(itemId, quantity, totalPrice);
        });
        recyclerView.setAdapter(adapter);

        viewModel.getItemsForCategory(categoryName).observe(getViewLifecycleOwner(), items -> {
            progressBar.setVisibility(View.GONE);
            if (items != null) {
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateData(items);
            } else {
                recyclerView.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load items", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error Occurred", Toast.LENGTH_LONG).show();
                for (String er : error) {
                    Log.e("MainFragment", "Error: " + er);
                }
                viewModel.clearError();
            }
        });

        return binding.getRoot();
    }
}