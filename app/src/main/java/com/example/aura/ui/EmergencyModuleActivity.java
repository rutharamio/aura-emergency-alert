package com.example.aura.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.aura.R;
import com.example.aura.services.EmergencyService;

public class EmergencyModuleActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private static final int REQUEST_SMS_PERMISSION = 200;
    private static final String TAG = "EmergencyModule";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_module);

        // ✅ Permiso de notificaciones (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }

        Button btnStartService = findViewById(R.id.btnStartService);
        btnStartService.setOnClickListener(v -> mostrarDialogoConfirmacion());
    }

    /** 🔔 Muestra el diálogo de confirmación antes de enviar la alerta */
    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Alerta de Emergencia")
                .setMessage("¿Estás seguro de que deseas enviar tu ubicación a tus contactos de confianza?")
                .setPositiveButton("SÍ, ENVIAR ALERTA", (dialog, which) -> {
                    Log.d(TAG, "Usuario confirmó la alerta 🚨");
                    solicitarPermisos();
                })
                .setNegativeButton("CANCELAR", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Alerta cancelada", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(true)
                .show();
    }

    /** 📍 Verifica permisos antes de iniciar el servicio */
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

    /** 🚨 Inicia el servicio de emergencia en primer plano */
    private void iniciarServicioEmergencia() {
        Log.d(TAG, "Permisos concedidos ✅ → Iniciando servicio");
        try {
            Intent serviceIntent = new Intent(this, EmergencyService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
            Toast.makeText(this, "🚨 Alerta enviada a tus contactos", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error al iniciar el servicio de emergencia", e);
            Toast.makeText(this, "Error al iniciar el servicio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                solicitarPermisos();
            } else {
                Log.e(TAG, "Permiso de ubicación DENEGADO ❌");
                Toast.makeText(this, "Se necesita permiso de ubicación para enviar la alerta", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                solicitarPermisos();
            } else {
                Log.e(TAG, "Permiso de SMS DENEGADO ❌");
                Toast.makeText(this, "Se necesita permiso de SMS para enviar la alerta", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
