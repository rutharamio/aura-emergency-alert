package com.example.aura.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.aura.R;
import com.example.aura.services.EmergencyService;

public class EmergencyModuleActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private static final int REQUEST_SMS_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_module);

        // ✅ Permiso de notificaciones (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }

        Button btnStartService = findViewById(R.id.btnStartService);
        btnStartService.setOnClickListener(v -> {
            Log.d("EmergencyModule", "Botón presionado → solicitando permisos");
            solicitarPermisos();
        });
    }

    private void solicitarPermisos() {
        boolean ubicacionOk = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean smsOk = ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;

        if (!ubicacionOk) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        if (!smsOk) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SMS_PERMISSION);
            return;
        }

        // ✅ Ambos permisos OK → iniciar servicio
        iniciarServicioEmergencia();
    }

    private void iniciarServicioEmergencia() {
        Log.d("EmergencyModule", "Permisos concedidos ✅ → Iniciando servicio");
        Intent serviceIntent = new Intent(this, EmergencyService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                solicitarPermisos();
            } else {
                Log.e("EmergencyModule", "Permiso de ubicación DENEGADO ❌");
            }
        }

        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                solicitarPermisos();
            } else {
                Log.e("EmergencyModule", "Permiso de SMS DENEGADO ❌");
            }
        }
    }
}

