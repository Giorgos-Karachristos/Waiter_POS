package com.waiterpos.ui.qrscanner;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.waiterpos.api.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrScannerRepository {
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> qrScannerLiveData = new MutableLiveData<>();

    public LiveData<String> validateQrCode(String qrCodeContent) {
        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("TOKEN", qrCodeContent);
        } catch (JSONException e) {
            Log.e("QrScannerRepository", "Invalid QR payload" + e.getMessage());
            errorLiveData.postValue("Invalid QR payload");
            return errorLiveData;
        }

        RequestBody requestBody = RequestBody.create(jsonPayload.toString(), MediaType.parse("application/json"));
        Call<String> call = ApiClient.getApiService().isValid(requestBody);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    qrScannerLiveData.postValue(response.body());
                } else {
                    errorLiveData.postValue("No info found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                errorLiveData.postValue(t.getMessage());
            }
        });

        return qrScannerLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }
}