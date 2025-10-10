package com.waiterpos.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.waiterpos.api.ApiClient;
import com.waiterpos.entities.Restaurant;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainRepository {
    private final MutableLiveData<Restaurant> restaurantLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public LiveData<Restaurant> getRestaurantInfo() {
        Call<List<Restaurant>> call = ApiClient.getApiService().getRestaurantInfo();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Restaurant>> call, @NonNull Response<List<Restaurant>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    restaurantLiveData.postValue(response.body().get(0));
                } else {
                    errorLiveData.postValue("No restaurant info found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Restaurant>> call, @NonNull Throwable t) {
                errorLiveData.postValue(t.getMessage());
            }
        });
        return restaurantLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}