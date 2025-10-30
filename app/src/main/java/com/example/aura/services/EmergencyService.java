package com.example.aura.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.aura.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.location.Location;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public class EmergencyService extends Service {
    public static final String TAG = "EmergencyService";
    public static final String CHANNEL_ID = "aura_emergency_channel";
    private static final int NOTIF_ID = 1001;

    private FusedLocationProviderClient fusedLocationClient;

    // Cambia por el número de prueba al que quieras enviar el SMS (incluye código de país si es necesario)
    private static final String EMERGENCY_NUMBER = "+59581992548";

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // notificación inicial
        Notification notification = buildNotification("Emergencia activada", "Servicio en primer plano activo");
        startForeground(NOTIF_ID, notification);

        Log.d(TAG, "onCreate - servicio creado");
        // Obtener ubicación y enviar SMS una sola vez al iniciar
        fetchLocationAndSendSmsOnce();
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

    private Notification buildNotification(String title, String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void fetchLocationAndSendSmsOnce() {
        // Comprobar permisos de ubicación en runtime
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "Permisos de ubicación no concedidos. No se puede obtener la ubicación.");
            // Actualizamos la notificación con el error
            NotificationManager nm = (NotificationManager) getSystemService(NotificationManager.class);
            if (nm != null) nm.notify(NOTIF_ID, buildNotification("Emergencia activada", "Permiso de ubicación no concedido"));
            return;
        }

        // Obtener última ubicación conocida
        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                String message;
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    message = "¡EMERGENCIA! Mi ubicación: https://maps.google.com/?q=" + lat + "," + lon;
                    Log.d(TAG, "Ubicación obtenida: " + lat + ", " + lon);
                } else {
                    // fallback: si no hay ubicación, enviar mensaje sin coords
                    message = "¡EMERGENCIA! No se pudo obtener ubicación en este momento. Por favor revisáme.";
                    Log.e(TAG, "No se obtuvo ubicación (getLastLocation returned null or failed)");
                }

                // Enviar SMS
                sendSms(message);
            }
        });
    }

    private void sendSms(String message) {
        // Comprobar permiso SEND_SMS
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permiso SEND_SMS no concedido. No se puede enviar SMS.");
            NotificationManager nm = (NotificationManager) getSystemService(NotificationManager.class);
            if (nm != null) nm.notify(NOTIF_ID, buildNotification("Emergencia activada", "Permiso SEND_SMS no concedido"));
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(EMERGENCY_NUMBER, null, message, null, null);
            Log.d(TAG, "SMS enviado a " + EMERGENCY_NUMBER + " -> " + message);

            // Actualizar notificación diciendo que la alerta fue enviada
            NotificationManager nm = (NotificationManager) getSystemService(NotificationManager.class);
            if (nm != null) nm.notify(NOTIF_ID, buildNotification("Alerta enviada", "SMS de emergencia enviado"));

            // Si querés, podés finalizar el servicio después del envío:
            // stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "Error al enviar SMS: " + e.getMessage(), e);
            NotificationManager nm = (NotificationManager) getSystemService(NotificationManager.class);
            if (nm != null) nm.notify(NOTIF_ID, buildNotification("Error al enviar alerta", "Revisá logs"));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: iniciando foreground");
        // Ya iniciamos foreground en onCreate; devolvemos sticky para permanecer
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy - servicio detenido");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
