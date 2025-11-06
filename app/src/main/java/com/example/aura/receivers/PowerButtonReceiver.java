package com.example.aura.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper; // <-- IMPORT NECESARIO
import android.util.Log;

import androidx.core.content.ContextCompat; // Import para iniciar el servicio de forma segura
import com.example.aura.services.EmergencyService;

public class PowerButtonReceiver extends BroadcastReceiver {

    private static final String TAG = "PowerButtonReceiver";

    private static int powerEventCount = 0;
    private static final long WINDOW_MS = 5000; // 5 segundos, está perfecto.

    // ================== CORRECCIÓN APLICADA AQUÍ ==================
    // Especificamos que el Handler debe usar el Looper del hilo principal.
    // Esto lo hace más robusto y previene fallos en segundo plano.
    private static final Handler handler = new Handler(Looper.getMainLooper());
    // =============================================================

    private static final Runnable resetCounter = () -> {
        Log.d(TAG, "⏱️ Ventana de tiempo expiró. Se reinició el contador.");
        powerEventCount = 0;
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return; // Buena práctica para evitar NullPointerException

        Log.d(TAG, "➡️ Acción recibida: " + action);

        if (action.equals(Intent.ACTION_SCREEN_OFF) || action.equals(Intent.ACTION_SCREEN_ON)) {
            powerEventCount++;
            Log.d(TAG, "🔄 Conteo de presiones: " + powerEventCount);

            // Reiniciamos el temporizador
            handler.removeCallbacks(resetCounter);
            handler.postDelayed(resetCounter, WINDOW_MS);

            if (powerEventCount >= 4) {
                Log.d(TAG, "🚨 Patrón de pánico detectado → Iniciando EmergencyService");

                Intent svc = new Intent(context, EmergencyService.class);
                try {
                    // Usamos ContextCompat para iniciar el servicio en primer plano de forma segura
                    ContextCompat.startForegroundService(context, svc);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error iniciando el servicio de emergencia", e);
                }

                // Reseteamos el contador y el temporizador inmediatamente
                handler.removeCallbacks(resetCounter);
                powerEventCount = 0;
            }
        }
    }
}
