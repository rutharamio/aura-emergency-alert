package com.example.aura.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.aura.R;
import com.example.aura.receivers.PowerButtonReceiver;

// Nota: No es necesario que sea un AppCompatService, un Service normal está bien.
public class PowerButtonService extends android.app.Service {

    private static final String TAG = "PowerButtonService";
    private static final String CHANNEL_ID = "AURA_GUARDIAN_CHANNEL";
    private static final int NOTIFICATION_ID = 1; // El ID debe ser > 0

    private BroadcastReceiver powerButtonReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio Guardián Creado.");
        // Registramos nuestro receiver dinámicamente para escuchar los eventos del botón
        registerPowerButtonReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Servicio Guardián Iniciado.");

        // ================== CAMBIO CLAVE AQUÍ ==================
        // 1. Creamos el canal de notificación (necesario para Android 8+)
        createNotificationChannel();

        // 2. Creamos la notificación que será visible para el usuario
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Aura está protegiéndote")
                .setContentText("El detector del botón de pánico está activo.")
                .setSmallIcon(R.drawable.ic_alert) // Asegúrate de tener un ícono llamado 'ic_alert' en res/drawable
                .setPriority(NotificationCompat.PRIORITY_MIN) // Para que sea discreta y no moleste
                .build();

        // 3. Convertimos este servicio en un Servicio en Primer Plano
        startForeground(NOTIFICATION_ID, notification);
        // =======================================================

        // START_STICKY asegura que el sistema intente recrear el servicio si lo mata por falta de memoria.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Servicio Guardián Destruido. Des-registrando receiver.");
        // Es una buena práctica des-registrar el receiver cuando el servicio muere
        if (powerButtonReceiver != null) {
            unregisterReceiver(powerButtonReceiver);
            powerButtonReceiver = null;
        }
    }

    private void registerPowerButtonReceiver() {
        powerButtonReceiver = new PowerButtonReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // Registramos el receiver para que escuche los eventos
        registerReceiver(powerButtonReceiver, filter);
        Log.d(TAG, "PowerButtonReceiver registrado dinámicamente.");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Servicio de Protección Aura",
                    NotificationManager.IMPORTANCE_LOW // Poca importancia para que no vibre ni suene
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
