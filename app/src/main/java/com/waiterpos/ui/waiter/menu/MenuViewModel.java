package com.waiterpos.ui.waiter.menu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.waiterpos.entities.Category;
import com.waiterpos.entities.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuViewModel extends ViewModel {
    private final MenuRepository menuRepository = new MenuRepository();

    private LiveData<List<Category>> categoriesLiveData;

    public LiveData<List<Category>> getCategories() {
        if (categoriesLiveData == null) {
            categoriesLiveData = menuRepository.getCategoryList();
        }
        return categoriesLiveData;
    }

    private final Map<String, LiveData<List<Item>>> itemsCache = new HashMap<>();

    public LiveData<List<Item>> getItemsForCategory(String category) {
        if (!itemsCache.containsKey(category)) {
            LiveData<List<Item>> liveData = menuRepository.getItemsList(category);
            itemsCache.put(category, liveData);
        }
        return itemsCache.get(category);
    }

    public LiveData<String> insertOrderToKitchen(String name, double subtotal, String type) {
        return menuRepository.insertOrderToKitchen(name, subtotal, type);
    }

    public LiveData<String> insertItemToOrder(int itemId, int quantity, double totalPrice) {
        return menuRepository.insertItemToOrder(itemId, quantity, totalPrice);
    }

    //Error Handling
    public LiveData<List<String>> getErrorLiveData() {
        return menuRepository.getErrorLiveData();
    }

    public void clearError() {
        menuRepository.clearError();
    }

    //Subtotal Logic
    private final MutableLiveData<Double> subtotalMLD = new MutableLiveData<>(0.0);
    public LiveData<Double> subtotalLiveData = subtotalMLD;

    private final Map<String, Double> itemTotals = new HashMap<>();
    private final Map<String, Integer> itemQuantities = new HashMap<>();

    public void updateItemTotal(String itemId, int quantity, double totalPrice) {
        if (quantity > 0) {
            itemQuantities.put(itemId, quantity);
            itemTotals.put(itemId, totalPrice);
        } else {
            itemQuantities.remove(itemId);
            itemTotals.remove(itemId);
        }
        double newSubtotal = 0;
        for (double price : itemTotals.values()) {
            newSubtotal += price;
        }
        subtotalMLD.postValue(newSubtotal);
    }

    public void resetSubtotal() {
        itemTotals.clear();
        itemQuantities.clear();
        subtotalMLD.postValue(0.0);
    }

    public Map<String, Double> getItemTotals() {
        return new HashMap<>(itemTotals);
    }

    public Map<String, Integer> getItemQuantities() {
        return new HashMap<>(itemQuantities);
    }

    private final MutableLiveData<Integer> tablesMLD = new MutableLiveData<>();
    public LiveData<Integer> tablesLiveData = tablesMLD;

    public void setTablesMLD(int order) {
        tablesMLD.setValue(order);
    }
}