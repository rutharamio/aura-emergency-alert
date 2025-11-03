package com.example.aura.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.example.aura.services.EmergencyService;

public class PowerButtonReceiver extends BroadcastReceiver {

    private static final String TAG = "PowerButtonReceiver";

    // Contador de eventos de power (screen on/off)
    private static int powerEventCount = 0;

    // Ventana de tiempo para detectar el patrón
    private static final long WINDOW_MS = 2000; // 2 segundos
    private static final Handler handler = new Handler();
    private static final Runnable resetCounter = () -> {
        Log.d(TAG, "⏱️ Se reinició el contador");
        powerEventCount = 0;
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "➡️ Acción recibida: " + action);

        if (action.equals(Intent.ACTION_SCREEN_OFF) || action.equals(Intent.ACTION_SCREEN_ON)) {

            powerEventCount++;
            Log.d(TAG, "🔄 Conteo de presiones: " + powerEventCount);

            // Reiniciar la ventana de detección
            handler.removeCallbacks(resetCounter);
            handler.postDelayed(resetCounter, WINDOW_MS);

            // Si llegó a 4 eventos → significa power 2 veces (off/on/off/on)
            if (powerEventCount >= 4) {
                Log.d(TAG, "🚨 Doble power detectado → Iniciando EmergencyService");

                Intent svc = new Intent(context, EmergencyService.class);
                try {
                    context.startForegroundService(svc);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error iniciando servicio", e);
                }

                powerEventCount = 0; // Reset
            }
        }
    }
}
