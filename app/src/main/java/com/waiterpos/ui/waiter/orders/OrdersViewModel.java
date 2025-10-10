package com.waiterpos.ui.waiter.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.waiterpos.entities.Kitchen;
import com.waiterpos.entities.Orders;

import java.util.List;

public class OrdersViewModel extends ViewModel {
    private final OrdersRepository ordersRepository = new OrdersRepository();

    private final MutableLiveData<Boolean> refreshTrigger = new MutableLiveData<>(false);

    public LiveData<List<Orders>> getActiveOrders() {
        return Transformations.switchMap(refreshTrigger, trigger -> {
            if (Boolean.TRUE.equals(trigger)) {
                return ordersRepository.getActiveOrdersList();
            } else {
                return new MutableLiveData<>(null);
            }
        });
    }

    public void refreshOrders() {
        refreshTrigger.setValue(true);
    }

    public LiveData<List<Kitchen>> getKitchenItems(int orderId) {
        return ordersRepository.getKitchenList(String.valueOf(orderId));
    }

    public LiveData<String> voidKitchen(int itemId, String itemName) {
        return ordersRepository.voidKitchen(itemId, itemName);
    }

    public LiveData<String> cancelOrder(int itemId) {
        return ordersRepository.cancelOrder(itemId);
    }

    //Error Handling
    public LiveData<List<String>> getErrorLiveData() {
        return ordersRepository.getErrorLiveData();
    }

    public void clearError() {
        ordersRepository.clearError();
    }

    private final MutableLiveData<Orders> selectedOrderMLD = new MutableLiveData<>();
    private final MutableLiveData<Kitchen> selectedItemMLD = new MutableLiveData<>();
    public LiveData<Orders> selectedOrderLD = selectedOrderMLD;
    public LiveData<Kitchen> selectedItemLD = selectedItemMLD;

    public void setSelectedOrderLD(Orders order) {
        selectedOrderMLD.setValue(order);
    }

    public void setSelectedItemLD(Kitchen item) {
        selectedItemMLD.setValue(item);
    }
}