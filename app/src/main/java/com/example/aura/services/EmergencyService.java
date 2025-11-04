package com.example.aura.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.aura.R;
import com.example.aura.core.Prefs;
import com.example.aura.data.AppDatabase;
import com.example.aura.data.entities.Contact;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmergencyService extends Service {

    public static final String TAG = "EmergencyService";
    public static final String CHANNEL_ID = "aura_emergency_channel";
    private static final int NOTIF_ID = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private ExecutorService executor;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        executor = Executors.newSingleThreadExecutor();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startForeground(NOTIF_ID, buildNotification("Emergencia activada", "Obteniendo ubicación..."));

        fetchLocationAndSendAlerts();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Aura - Servicio de emergencia",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification(String title, String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_alert) // Usa tu icono si tenés uno propio
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String title, String text) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null)
            nm.notify(NOTIF_ID, buildNotification(title, text));
    }

    private void fetchLocationAndSendAlerts() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            updateNotification("Permiso requerido", "Activa el permiso de ubicación.");
            stopSelf();
            return;
        }

        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> t) {
                double lat = 0;
                double lon = 0;
                boolean gotLocation = false;

                if (t.isSuccessful() && t.getResult() != null) {
                    Location location = t.getResult();
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    gotLocation = true;
                }

                final double finalLat = lat;
                final double finalLon = lon;
                final boolean finalGotLocation = gotLocation;

                executor.execute(() -> sendAlertToAllContacts(finalLat, finalLon, finalGotLocation));
            }
        });
    }

    private void sendAlertToAllContacts(double lat, double lon, boolean gotLocation) {
        AppDatabase db = AppDatabase.getInstance(this);
        List<Contact> contacts = db.contactDao().getAllContacts();

        if (contacts == null || contacts.isEmpty()) {
            Log.w(TAG, "No hay contactos de emergencia guardados.");
            updateNotification("Sin contactos", "No se enviaron mensajes.");
            stopSelf();
            return;
        }

        String nombreUsuario = Prefs.getLoggedInUserName(this);
        if (nombreUsuario.isEmpty()) nombreUsuario = "Un usuario de Aura";

        String mensaje = gotLocation
                ? "🚨 ¡ALERTA DE EMERGENCIA! " + nombreUsuario +
                " necesita ayuda. Esta es su ubicación aproximada: " +
                "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon
                : "🚨 ¡ALERTA DE EMERGENCIA! " + nombreUsuario +
                " necesita ayuda, pero no se pudo obtener su ubicación.";

        SmsManager smsManager = SmsManager.getDefault();
        int enviados = 0;

        for (Contact c : contacts) {
            String phone = c.phone;
            if (phone == null || phone.isEmpty()) continue;

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permiso SEND_SMS no concedido.");
                continue;
            }

            try {
                smsManager.sendTextMessage(phone, null, mensaje, null, null);
                enviados++;
                Log.d(TAG, "SMS enviado a " + phone);
            } catch (Exception e) {
                Log.e(TAG, "Error al enviar SMS a " + phone, e);
            }
        }

        updateNotification("Alerta enviada", "Mensajes enviados a " + enviados + " contacto(s).");
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Servicio finalizado");
        executor.shutdown();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
