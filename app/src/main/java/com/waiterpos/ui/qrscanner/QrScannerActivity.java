package com.waiterpos.ui.qrscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.waiterpos.ui.main.MainActivity;
import com.waiterpos.ui.waiter.WaiterActivity;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

public class QrScannerActivity extends AppCompatActivity {

    private QrScannerViewModel viewModel;
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(QrScannerViewModel.class);

        viewModel.getQrValidationResult().observe(this, result -> {
            if (!"No ok".equals(result)) {
                navigateToWaiterActivity(result);
            } else {
                Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                goBackToMainActivity();
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Network error occurred", Toast.LENGTH_SHORT).show();
                Log.e("QrScannerActivity", "Error: " + error);
                viewModel.clearError();
                goBackToMainActivity();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
            goBackToMainActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    scanQRCode(imageBitmap);
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                    goBackToMainActivity();
                }
            }
        }
    }

    private void scanQRCode(Bitmap imageBitmap) {
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        BarcodeScanning.getClient().process(image).addOnSuccessListener(barcodes -> {
            if (barcodes.isEmpty()) {
                Toast.makeText(this, "No QR code found", Toast.LENGTH_SHORT).show();
                goBackToMainActivity();
                return;
            }
            for (Barcode barcode : barcodes) {
                String qrValue = barcode.getRawValue();
                //viewModel.validateQrCode(qrValue);
                //TODO: remove this:
                Log.i("QrCode Validation", "qrValue " + qrValue);
                if ("http://en.m.wikipedia.org".equals(qrValue)) {
                    navigateToWaiterActivity("George");
                } else {
                    viewModel.validateQrCode(qrValue);
                }//*/
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(QrScannerActivity.this, "Failed to scan QR code.", Toast.LENGTH_SHORT).show();
            goBackToMainActivity();
        });
    }

    private void navigateToWaiterActivity(String headerText) {
        Log.i("QrScannerActivity", "Tables Number:" + getIntent().getIntExtra("TABLES_COUNT", 0) + " Name: " + headerText);
        Intent intent = new Intent(QrScannerActivity.this, WaiterActivity.class);
        intent.putExtra("HEADER_TEXT", headerText);
        intent.putExtra("TABLES_COUNT", getIntent().getIntExtra("TABLES_COUNT", 0));
        startActivity(intent);
        finish();
    }

    private void goBackToMainActivity() {
        Intent intent = new Intent(QrScannerActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}