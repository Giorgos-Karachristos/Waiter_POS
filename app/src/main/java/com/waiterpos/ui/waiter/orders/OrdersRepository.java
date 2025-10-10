package com.waiterpos.ui.waiter.orders;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.waiterpos.api.ApiClient;
import com.waiterpos.entities.Kitchen;
import com.waiterpos.entities.Orders;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersRepository {
    private final MutableLiveData<List<String>> errorLiveData = new MutableLiveData<>();

    public LiveData<List<Orders>> getActiveOrdersList() {
        MutableLiveData<List<Orders>> activeOrdersLiveData = new MutableLiveData<>();
        Call<List<Orders>> call = ApiClient.getApiService().getActiveOrdersList();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Orders>> call, @NonNull Response<List<Orders>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activeOrdersLiveData.postValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Orders>> call, @NonNull Throwable t) {
                postError(t.getMessage());
            }
        });
        return activeOrdersLiveData;
    }

    public LiveData<List<Kitchen>> getKitchenList(String orderId) {
        MutableLiveData<List<Kitchen>> kitchenItemsLiveData = new MutableLiveData<>();
        Call<List<Kitchen>> call = ApiClient.getApiService().getKitchenList(orderId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Kitchen>> call, @NonNull Response<List<Kitchen>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    kitchenItemsLiveData.postValue(response.body());
                } else {
                    postError("No items found for the order id: " + orderId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Kitchen>> call, @NonNull Throwable t) {
                postError(t.getMessage());
            }
        });
        return kitchenItemsLiveData;
    }

    public LiveData<String> voidKitchen(int orderID, String itemName) {
        MutableLiveData<String> voidItemLiveData = new MutableLiveData<>();
        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("ORDER_ID", orderID);
            jsonPayload.put("NAME", itemName);
        } catch (JSONException e) {
            Log.e("OrdersRepository voidKitchen", "Invalid order payload" + e.getMessage());
            postError("Invalid kitchen payload");
            return voidItemLiveData;
        }
        RequestBody requestBody = RequestBody.create(jsonPayload.toString(), MediaType.parse("application/json"));

        Call<String> call = ApiClient.getApiService().voidKitchen(requestBody);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    voidItemLiveData.postValue(response.body());
                } else {
                    postError("Failed to void item");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                postError(t.getMessage());
            }
        });

        return voidItemLiveData;
    }

    public LiveData<String> cancelOrder(int orderID) {
        MutableLiveData<String> cancelOrderLiveData = new MutableLiveData<>();
        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("ORDER_ID", orderID);
        } catch (JSONException e) {
            Log.e("OrdersRepository voidKitchen", "Invalid order payload" + e.getMessage());
            postError("Invalid order payload");
            return cancelOrderLiveData;
        }
        RequestBody requestBody = RequestBody.create(jsonPayload.toString(), MediaType.parse("application/json"));

        Call<String> call = ApiClient.getApiService().cancelOrder(requestBody);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    cancelOrderLiveData.postValue(response.body());
                } else {
                    postError("Failed to cancel the order");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                postError(t.getMessage());
            }
        });

        return cancelOrderLiveData;
    }

    private void postError(String message) {
        List<String> currentErrors = errorLiveData.getValue();
        if (currentErrors == null) currentErrors = new ArrayList<>();
        currentErrors.add(message);
        errorLiveData.postValue(new ArrayList<>(currentErrors));
    }

    public LiveData<List<String>> getErrorLiveData() {
        return errorLiveData;
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }
}