package com.waiterpos.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.waiterpos.entities.Restaurant;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Boolean> triggerFetch = new MutableLiveData<>(false);
    private final LiveData<String> restaurantName;
    private final LiveData<Integer> tablesNumber;
    private final LiveData<String> errorLiveData;
    private final MainRepository mainRepository = new MainRepository();

    public MainViewModel() {
        // When triggerFetch changes, request new data
        LiveData<Restaurant> restaurantLiveData = Transformations.switchMap(triggerFetch, trigger ->
                trigger ? mainRepository.getRestaurantInfo() : new MutableLiveData<>(null));

        // Expose mapped values
        restaurantName = Transformations.map(restaurantLiveData, restaurant -> restaurant != null
                ? restaurant.getName() : "Unknown");

        tablesNumber = Transformations.map(restaurantLiveData, tables -> tables != null
                ? tables.getTable() : 0);

        errorLiveData = mainRepository.getErrorLiveData();
    }

    public LiveData<String> getRestaurantName() {
        return restaurantName;
    }

    public LiveData<Integer> getTablesNumber() {
        return tablesNumber;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void fetchRestaurantInfo() {
        triggerFetch.setValue(true);
    }
}