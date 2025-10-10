package com.waiterpos.ui.waiter.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.waiterpos.entities.Register;

import java.util.List;

public class RegisterViewModel extends ViewModel {

    private final RegisterRepository registerRepository = new RegisterRepository();

    private final MutableLiveData<String> registerListTrigger = new MutableLiveData<>();

    public LiveData<List<Register>> getRegisterList() {
        return Transformations.switchMap(registerListTrigger, orderId -> {
            if (orderId != null && !orderId.isEmpty()) {
                return registerRepository.getRegisterList(orderId);
            } else {
                return new MutableLiveData<>(null);
            }
        });
    }

    public void fetchRegisterList(String orderId) {
        registerListTrigger.setValue(orderId);
    }

    private final MutableLiveData<String[]> completeOrderTrigger = new MutableLiveData<>();

    public void completeOrder(String orderId, String subtotal, String discount, String tip, String total) {
        completeOrderTrigger.setValue(new String[]{orderId, subtotal, discount, tip, total});
    }

    public LiveData<String> getCompleteOrderResponse() {
        return Transformations.switchMap(completeOrderTrigger, params -> {
            if (params != null && params.length == 5) {
                int orderId = Integer.parseInt(params[0]);
                double subtotal = Double.parseDouble(params[1]);
                double discount = Double.parseDouble(params[2]);
                double tip = Double.parseDouble(params[3]);
                double total = Double.parseDouble(params[4]);
                return registerRepository.completeOrder(orderId, subtotal, discount, tip, total);
            }
            return new MutableLiveData<>(null);
        });
    }

    public LiveData<List<String>> getErrorLiveData() {
        return registerRepository.getErrorLiveData();
    }

    public void clearError() {
        registerRepository.clearError();
    }
}