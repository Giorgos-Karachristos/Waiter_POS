package com.waiterpos.ui.qrscanner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class QrScannerViewModel extends ViewModel {
    private final QrScannerRepository qrScannerRepository = new QrScannerRepository();

    private final MutableLiveData<String> qrCodeTrigger = new MutableLiveData<>();

    public LiveData<String> getQrValidationResult() {
        return Transformations.switchMap(qrCodeTrigger, qrScannerRepository::validateQrCode);
    }

    public void validateQrCode(String qrCodeContent) {
        qrCodeTrigger.setValue(qrCodeContent);
    }

    public LiveData<String> getErrorLiveData() {
        return qrScannerRepository.getErrorLiveData();
    }

    public void clearError() {
        qrScannerRepository.clearError();
    }
}