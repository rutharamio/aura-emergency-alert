package com.example.aura.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.example.aura.R;  // ✅ debe existir

import androidx.core.app.NotificationCompat;

public class EmergencyService extends Service {
    public static final String TAG = "EmergencyService";
    public static final String CHANNEL_ID = "aura_emergency_channel";
    private static final int NOTIF_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Emergencia activada")
                .setContentText("Servicio en primer plano activo")
                .setSmallIcon(R.drawable.ic_alert)
                .build();

        startForeground(1, notification);
        createChannel();
        Log.d(TAG, "Servicio de emergencia iniciado en onCreate");
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Aura - Servicio de emergencia",
                    NotificationManager.IMPORTANCE_LOW
            );
            ch.setDescription("Servicio en primer plano usado para enviar alertas y obtener ubicación");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Emergencia activada")
                .setContentText("Servicio en primer plano activo")
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // poné tu propio icono si tenés
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: iniciando foreground");
        Log.d(TAG, "Servicio de emergencia iniciado en onStartCommand"); // <- PONÉ ESTO

        startForeground(NOTIF_ID, buildNotification());

        // TODO: arrancar tracking de ubicación o lógica de alerta
        // por ejemplo: startLocationUpdates();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

