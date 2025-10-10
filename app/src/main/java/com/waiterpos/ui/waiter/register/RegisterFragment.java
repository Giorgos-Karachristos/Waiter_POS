package com.waiterpos.ui.waiter.register;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.waiterpos.R;
import com.waiterpos.databinding.FragmentRegisterBinding;
import com.waiterpos.entities.Register;
import com.waiterpos.ui.waiter.orders.OrdersViewModel;
import com.google.android.material.navigation.NavigationView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private EditText subtotalEditText, tipEditText, discountEditText, totalEditText, receivedEditText;
    private RecyclerView orderRecyclerView;
    private String orderID;
    private RegisterViewModel registerViewModel;
    private OrdersViewModel ordersViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);

        orderRecyclerView = binding.recyclerView2;
        subtotalEditText = binding.subtotalEditTextNumberDecimal;
        tipEditText = binding.tipEditTextNumberDecimal;
        discountEditText = binding.discountEditTextNumberDecimal4;
        totalEditText = binding.totalEditTextNumberDecimal;
        receivedEditText = binding.receivedAmountEditTextNumberDecimal;

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        ordersViewModel = new ViewModelProvider(requireActivity()).get(OrdersViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            orderID = args.getString("ORDER_ID");
            binding.orderIDTextView.setText("Order ID: " + orderID);
        }

        binding.payOrderButton.setOnClickListener(v -> payOrder());
        binding.cancelOrderButton.setOnClickListener(v -> cancelOrder());

        registerViewModel.getRegisterList().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                setupRecyclerView(list);
            } else {
                Log.e("RegisterFragment", "Failed to load register list");
            }
        });

        registerViewModel.getCompleteOrderResponse().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                Toast.makeText(getContext(), response, Toast.LENGTH_LONG).show();
                Log.i("RegisterFragment", "payOrder response: " + response);
            } else {
                Toast.makeText(getContext(), "Error completing order", Toast.LENGTH_LONG).show();
                Log.e("RegisterFragment", "payOrder response is null");
            }
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.nav_menu);
        });

        registerViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error Occurred", Toast.LENGTH_LONG).show();
                for (String er : error) {
                    Log.e("MainFragment", "Error: " + er);
                }
                registerViewModel.clearError();
            }
        });

        if (orderID != null) {
            registerViewModel.fetchRegisterList(orderID);
        }

        subtotalEditText.addTextChangedListener(totalCalculatorListener());
        tipEditText.addTextChangedListener(totalCalculatorListener());
        discountEditText.addTextChangedListener(totalCalculatorListener());
        receivedEditText.addTextChangedListener(changeCalculatorListener());

        return binding.getRoot();
    }

    private void setupRecyclerView(List<Register> registerList) {
        RegisterItemAdapter itemAdapter = new RegisterItemAdapter(registerList);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderRecyclerView.setAdapter(itemAdapter);

        // Calculate subtotal
        double subtotal = itemAdapter.getTotalPrice();
        BigDecimal bd = new BigDecimal(subtotal).setScale(2, RoundingMode.HALF_UP);
        subtotalEditText.setText(String.valueOf(bd.doubleValue()));
    }

    private void payOrder() {
        if (orderID == null) {
            Toast.makeText(getContext(), "No order selected", Toast.LENGTH_LONG).show();
            return;
        }
        String subtotal = ((!subtotalEditText.getText().toString().isEmpty())
                ? subtotalEditText.getText().toString() : "0.0");
        String discount = ((!discountEditText.getText().toString().isEmpty())
                ? discountEditText.getText().toString() : "0.0");
        String tip = ((!tipEditText.getText().toString().isEmpty())
                ? tipEditText.getText().toString() : "0.0");
        String total = ((!totalEditText.getText().toString().isEmpty())
                ? totalEditText.getText().toString() : "0.0");

        if ("0.0".equals(subtotal) || "0.0".equals(total)) {
            Toast.makeText(getContext(), "Subtotal/Total must be greater than zero", Toast.LENGTH_LONG).show();
            return;
        }

        registerViewModel.completeOrder(orderID, subtotal, discount, tip, total);
    }

    private void cancelOrder() {
        if (orderID == null) {
            Toast.makeText(getContext(), "No order selected", Toast.LENGTH_SHORT).show();
            return;
        }
        int orderIdInt = Integer.parseInt(orderID);
        Log.i("cancelOrder", "cancelOrder: " + orderID);
        ordersViewModel.cancelOrder(orderIdInt).observe(getViewLifecycleOwner(), response -> {
            Toast.makeText(getContext(), Objects.requireNonNullElse(response, "Failed to cancel order"), Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.nav_menu);
        });
    }

    private TextWatcher totalCalculatorListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                calculateTotal();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateTotal();
            }

            public void calculateTotal() {
                double subtotal = 0;
                double tip = 0;
                double discount = 0;
                String subtotalText = subtotalEditText.getText().toString().isBlank() ? "0"
                        : subtotalEditText.getText().toString();
                String discountText = discountEditText.getText().toString().isBlank() ? "0"
                        : discountEditText.getText().toString();
                String tipText = tipEditText.getText().toString().isBlank() ? "0"
                        : tipEditText.getText().toString();
                if (isValid(subtotalText, "Subtotal") && isValid(discountText, "Discount") && isValid(tipText, "Tip")) {
                    subtotal = Double.parseDouble(subtotalText);
                    tip = Double.parseDouble(tipText);
                    discount = Double.parseDouble(discountText);
                }
                double total = subtotal + tip - discount;

                BigDecimal bd = new BigDecimal(total);
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                double roundedValue = bd.doubleValue();

                totalEditText.setText(String.valueOf(roundedValue));
            }
        };
    }

    private TextWatcher changeCalculatorListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                calculateChange();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateChange();
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateChange();
            }

            public void calculateChange() {
                double total = 0;
                double received = 0;
                String totalText = totalEditText.getText().toString().isBlank() ? "0"
                        : totalEditText.getText().toString();
                String receivedText = receivedEditText.getText().toString().isBlank() ? "0"
                        : receivedEditText.getText().toString();
                if (isValid(totalText, "Total") && isValid(receivedText, "Received")) {
                    total = Double.parseDouble(totalText);
                    received = Double.parseDouble(receivedText);
                }
                double change = received - total;

                BigDecimal bd = new BigDecimal(change);
                bd = bd.setScale(2, RoundingMode.HALF_UP);
                double roundedValue = bd.doubleValue();

                binding.changeValueTextView.setText(String.valueOf(roundedValue));
            }
        };
    }

    private boolean isValid(String price, String type) {
        if (price.isBlank()) {
            Toast.makeText(getContext(), type + " must be not empty", Toast.LENGTH_LONG).show();
            return false;
        }
        double doublePrice;
        try {
            doublePrice = Double.parseDouble(price);
            if (doublePrice < 0) {
                Toast.makeText(getContext(), type + " must be more than zero", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (NumberFormatException exception) {
            Toast.makeText(getContext(), type + " must be number", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        NavigationView navigationView = requireActivity().findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_menu).setEnabled(true);
        menu.findItem(R.id.nav_orders).setEnabled(true);
    }
}