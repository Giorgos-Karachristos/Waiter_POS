package com.waiterpos.ui.waiter.register;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.waiterpos.api.ApiClient;
import com.waiterpos.entities.Register;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterRepository {

    private final MutableLiveData<List<String>> errorLiveData = new MutableLiveData<>();

    public LiveData<List<Register>> getRegisterList(String orderId) {
        MutableLiveData<List<Register>> data = new MutableLiveData<>();

        Call<List<Register>> call = ApiClient.getApiService().getRegisterList(orderId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Register>> call, @NonNull Response<List<Register>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    data.postValue(response.body());
                } else {
                    postError("No items found for the order id: " + orderId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Register>> call, @NonNull Throwable t) {
                postError(t.getMessage());
            }
        });

        return data;
    }

    public LiveData<String> completeOrder(int orderId, double subtotal, double discount, double tip, double total) {
        MutableLiveData<String> data = new MutableLiveData<>();

        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("ORDER_ID", orderId);
            jsonPayload.put("SUBTOTAL", subtotal);
            jsonPayload.put("DISCOUNT", discount);
            jsonPayload.put("TIP", tip);
            jsonPayload.put("TOTAL", total);
        } catch (JSONException e) {
            Log.e("RegisterRepository completeOrder", "Invalid order payload" + e.getMessage());
            postError("Invalid order payload");
            return data;
        }

        RequestBody requestBody = RequestBody.create(jsonPayload.toString(), MediaType.parse("application/json"));

        Call<String> call = ApiClient.getApiService().completeOrder(requestBody);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    data.postValue(response.body());
                } else {
                    postError("Failed to complete the order");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                postError(t.getMessage());
            }
        });

        return data;
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