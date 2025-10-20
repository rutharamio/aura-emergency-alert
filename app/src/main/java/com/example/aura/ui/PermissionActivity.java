package com.example.aura.ui;

import android.Manifest;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

public class PermissionActivity extends AppCompatActivity {
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean sms = result.getOrDefault(Manifest.permission.SEND_SMS, false);
                    if (Boolean.TRUE.equals(fine) && Boolean.TRUE.equals(sms)) {
                        Toast.makeText(this, "Permisos otorgados", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Faltan permisos, algunas funciones no funcionarán", Toast.LENGTH_LONG).show();
                    }
                    finish();
                });
        requestPermissionLauncher.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
        });
    }
}

