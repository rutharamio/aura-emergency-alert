package com.example.aura.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import android.content.Context;

import com.example.aura.utils.WhatsAppUtils;

public class EmergencyService extends Service {
    private static final String TAG = "EmergencyService";
    private static final String CHANNEL_ID = "aura_emergency_channel";
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
        startForeground(1, buildNotification("Activando alerta discreta..."));
        fetchLocationAndSend();
    }

    private void fetchLocationAndSend() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    Log.d(TAG, "Location: " + lat + "," + lon);

                    // TODO: reemplazar por lista de contactos desde Room (cuando Ruth lo exponga)
                    String phone = "+595971323111";
                    WhatsAppUtils.sendAlert(getApplicationContext(), phone, lat, lon);

                    // TODO: guardar reporte en DB usando ReportRepository cuando Ruth lo tenga
                    Log.d(TAG, "Reporte guardado (simulado) - lat: " + lat + " lon: " + lon);
                } else {
                    Log.w(TAG, "Ubicación nula: revisar permisos o solicitar ubicación activa.");
                }
                stopSelf();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error obteniendo ubicación: " + e.getMessage(), e);
                stopSelf();
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Permiso de ubicación faltante", e);
            stopSelf();
        }
    }

    private Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Aura - Alerta discreta")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Aura emergency";
            String description = "Canal para alertas discretas de Aura";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // ya inicializamos en onCreate
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}

