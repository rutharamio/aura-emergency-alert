package com.example.aura.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat; // Import para iniciar el servicio de forma segura

import com.example.aura.services.PowerButtonService;

// ================== CORRECCIÓN DEL NOMBRE DE LA CLASE ==================
// Se elimina la "r" extra para que coincida con el nombre del archivo "BootReceiver.java"
public class BootReceiver extends BroadcastReceiver {
// ====================================================================

    private static final String TAG = "BootReceiver"; // Este es el TAG que SÍ queremos ver

    @Override
    public void onReceive(Context context, Intent intent) {
        // Comprueba si la acción recibida es la de arranque completado
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "✅ Evento BOOT_COMPLETED recibido por NUESTRA APP. Iniciando el servicio guardián...");

            // Creamos la intención para iniciar nuestro servicio guardián
            Intent serviceIntent = new Intent(context, PowerButtonService.class);

            // En versiones modernas de Android, es más seguro iniciar servicios en segundo plano así
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
