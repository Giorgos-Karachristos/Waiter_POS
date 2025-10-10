package com.waiterpos.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.waiterpos.databinding.FragmentMainBinding;
import com.waiterpos.ui.qrscanner.QrScannerActivity;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private int tablesNumber = 0;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        mainViewModel.getRestaurantName().observe(getViewLifecycleOwner(), name -> binding.restaurantNameTextView.setText(name));

        mainViewModel.getTablesNumber().observe(getViewLifecycleOwner(), tables -> tablesNumber = tables);

        mainViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Failed to load restaurant info.", Toast.LENGTH_SHORT).show();
                Log.e("MainFragment", "Error: " + error);
            }
        });

        mainViewModel.fetchRestaurantInfo();

        binding.scanQrButton.setOnClickListener(v -> openQRScanner());
    }

    private void openQRScanner() {
        Intent intent = new Intent(requireContext(), QrScannerActivity.class);
        intent.putExtra("TABLES_COUNT", tablesNumber);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}