package com.example.aura.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.aura.R;

public class PermissionActivity extends AppCompatActivity {

    private static final int REQ_PERMS = 101;

    private String[] REQUIRED_PERMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        // permisos base
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ may require other fine-grained handling; mantengamos ACCESS_FINE_LOCATION + SEND_SMS
        }
        REQUIRED_PERMS = new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.FOREGROUND_SERVICE // necesario en manifest, runtime no siempre requerido
        };

        Button btnRequest = findViewById(R.id.btnRequestPermissions);
        btnRequest.setOnClickListener(v -> {
            if (!hasAllPermissions()) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMS, REQ_PERMS);
            } else {
                Toast.makeText(this, "Permisos ya concedidos", Toast.LENGTH_SHORT).show();
                finish(); // o devolverse al flujo
            }
        });
    }

    private boolean hasAllPermissions() {
        for (String p : REQUIRED_PERMS) {
            if (ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_PERMS) {
            boolean ok = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Permisos no concedidos. Algunas funciones pueden no funcionar.", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}


