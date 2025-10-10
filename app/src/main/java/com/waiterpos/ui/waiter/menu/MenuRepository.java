package com.waiterpos.ui.waiter.menu;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.waiterpos.api.ApiClient;
import com.waiterpos.entities.Category;
import com.waiterpos.entities.Item;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuRepository {
    private final MutableLiveData<List<String>> errorLiveData = new MutableLiveData<>();

    public LiveData<List<Category>> getCategoryList() {
        MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();
        Call<List<Category>> call = ApiClient.getApiService().getCategoryList();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    categoriesLiveData.postValue(response.body());
                } else {
                    postError("Failed to load categories");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                postError(t.getMessage());
            }
        });

        return categoriesLiveData;
    }

    public LiveData<List<Item>> getItemsList(String category) {
        MutableLiveData<List<Item>> itemsLiveData = new MutableLiveData<>();
        Call<List<Item>> call = ApiClient.getApiService().getItemsListFromCategory(category);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Item>> call, @NonNull Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    itemsLiveData.postValue(response.body());
                } else {
                    postError("No items found for category: " + category);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Item>> call, @NonNull Throwable t) {
                postError(t.getMessage());
            }
        });

        return itemsLiveData;
    }

    public LiveData<String> insertOrderToKitchen(String name, Double subtotal, String type) {
        MutableLiveData<String> insertOrderLiveData = new MutableLiveData<>();
        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("NAME", name);
            jsonPayload.put("SUBTOTAL", subtotal);
            jsonPayload.put("TYPE", type);
        } catch (JSONException e) {
            Log.e("MenuRepository insertOrderToKitchen", "Invalid order payload" + e.getMessage());
            postError("Invalid order payload");
            return insertOrderLiveData;
        }

        RequestBody requestBody = RequestBody.create(jsonPayload.toString(), MediaType.parse("application/json"));
        Call<String> call = ApiClient.getApiService().insertOrderToKitchen(requestBody);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    insertOrderLiveData.postValue(response.body());
                } else {
                    postError("Failed to insert order");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                postError(t.getMessage());
            }
        });

        return insertOrderLiveData;
    }

    public LiveData<String> insertItemToOrder(int item, int quantity, Double totalPrice) {
        MutableLiveData<String> insertItemLiveData = new MutableLiveData<>();
        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("ITEM_ID", item);
            jsonPayload.put("QUANTITY", quantity);
            jsonPayload.put("TOTAL_PRICE", totalPrice);
        } catch (JSONException e) {
            Log.e("MenuRepository insertItemToOrder", "Invalid order item payload" + e.getMessage());
            postError("Invalid order item payload");
            return insertItemLiveData;
        }

        RequestBody requestBody = RequestBody.create(jsonPayload.toString(), MediaType.parse("application/json"));
        Call<String> call = ApiClient.getApiService().insertItemToOrder(requestBody);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    insertItemLiveData.postValue(response.body());
                } else {
                    postError("Failed to insert item");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                postError(t.getMessage());
            }
        });

        return insertItemLiveData;
    }

    private void postError(String message) {
        List<String> currentErrors = errorLiveData.getValue();
        if (currentErrors == null) {
            currentErrors = new ArrayList<>();
        }
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