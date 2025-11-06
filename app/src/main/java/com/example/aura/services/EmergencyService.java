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
// Se elimina la importación de Prefs si solo se usaba para el nombre de usuario aquí.
import com.example.aura.data.entities.Contact;

// ===== NUEVOS IMPORTS PARA FIREBASE =====
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
// ======================================

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

// Ya no se necesitan AppDatabase, List, ExecutorService aquí.

public class EmergencyService extends Service {

    public static final String TAG = "EmergencyService";
    public static final String CHANNEL_ID = "aura_emergency_channel";
    private static final int NOTIF_ID = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    // Ya no se necesita el ExecutorService, Firebase maneja sus propios hilos.

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startForeground(NOTIF_ID, buildNotification("Emergencia activada", "Obteniendo ubicación..."));

        fetchLocationAndSendAlerts();
    }

    // ... (los métodos createNotificationChannel, buildNotification, updateNotification se quedan igual) ...
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
                .setSmallIcon(R.drawable.ic_alert)
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
        task.addOnCompleteListener(t -> { // Usando lambda para un código más limpio
            double lat = 0;
            double lon = 0;
            boolean gotLocation = false;

            if (t.isSuccessful() && t.getResult() != null) {
                Location location = t.getResult();
                lat = location.getLatitude();
                lon = location.getLongitude();
                gotLocation = true;
                Log.d(TAG, "Ubicación obtenida: " + lat + ", " + lon);
            } else {
                Log.w(TAG, "No se pudo obtener la ubicación.");
            }

            // Llamamos al método que ahora usa Firebase
            sendAlertToAllContacts(lat, lon, gotLocation);
        });
    }

    // ===== MÉTODO COMPLETAMENTE REESCRITO PARA USAR FIREBASE =====
    private void sendAlertToAllContacts(double lat, double lon, boolean gotLocation) {
        Log.d(TAG, "Iniciando proceso sendAlertToAllContacts con Firebase...");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "¡FALLO CRÍTICO! No hay usuario de Firebase logueado. No se pueden obtener contactos.");
            updateNotification("Error de Sesión", "Inicia sesión para usar la alerta.");
            stopSelf();
            return;
        }

        // Apuntamos a la "carpeta" de contactos del usuario actual en Firebase
        DatabaseReference userContactsRef = FirebaseDatabase.getInstance().getReference("contacts").child(currentUser.getUid());

        // Leemos los contactos UNA SOLA VEZ desde Firebase
        userContactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    Log.w(TAG, "¡PROBLEMA! No se encontraron contactos de emergencia en Firebase.");
                    updateNotification("Sin contactos", "No hay contactos para notificar.");
                    stopSelf();
                    return;
                }

                long totalContacts = snapshot.getChildrenCount();
                Log.d(TAG, "Contactos encontrados en Firebase: " + totalContacts);

                // Obtenemos el nombre del usuario de su perfil de Firebase
                String nombreUsuario = currentUser.getDisplayName();
                if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                    nombreUsuario = "Un usuario de Aura"; // Fallback
                }

                String mensaje = gotLocation
                        ? "🚨 ¡ALERTA DE EMERGENCIA! " + nombreUsuario +
                        " necesita ayuda. Esta es su ubicación aproximada: " +
                        "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon
                        : "🚨 ¡ALERTA DE EMERGENCIA! " + nombreUsuario +
                        " necesita ayuda, pero no se pudo obtener su ubicación.";

                Log.d(TAG, "Mensaje a enviar: " + mensaje);

                SmsManager smsManager = SmsManager.getDefault();
                int enviados = 0;

                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    Contact contact = contactSnapshot.getValue(Contact.class);
                    if (contact != null && contact.phone != null && !contact.phone.isEmpty()) {
                        String phone = contact.phone;

                        // Tu lógica de normalización de número (¡está perfecta!)
                        phone = phone.trim().replaceAll("\\s+", "");
                        if (phone.startsWith("0") && !phone.startsWith("+595")) {
                            phone = "+595" + phone.substring(1);
                        }

                        // ... (Tu lógica de comprobación de permisos y envío de SMS se queda igual) ...
                        if (ContextCompat.checkSelfPermission(EmergencyService.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "¡FALLO CRÍTICO! Permiso SEND_SMS no concedido. Imposible enviar.");
                            // ...
                            return;
                        }
                        try {
                            Log.d(TAG, "✅ Intentando enviar SMS a: " + contact.name + " (" + phone + ")");
                            smsManager.sendTextMessage(phone, null, mensaje, null, null);
                            enviados++;
                            Log.i(TAG, "✅✅ ¡SMS puesto en cola para ser enviado a " + phone + "!");
                        } catch (Exception e) {
                            Log.e(TAG, "❌❌ ¡ERROR EXCEPCIÓN! Fallo al enviar SMS a " + phone, e);
                        }
                    }
                }

                Log.i(TAG, "Proceso de envío finalizado. Total de mensajes intentados: " + enviados);
                updateNotification("Alerta enviada", "Mensajes enviados a " + enviados + " de " + totalContacts + " contacto(s).");
                stopSelf();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al obtener contactos desde Firebase.", error.toException());
                updateNotification("Error de Red", "No se pudo acceder a los contactos.");
                stopSelf();
            }
        });
    }


    // ... (El resto de tu clase: onStartCommand, onDestroy, onBind, se quedan igual) ...
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Servicio finalizado");
        // executor.shutdown(); // Ya no se necesita el executor
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
