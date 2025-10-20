package com.example.aura.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class PowerButtonReceiver extends BroadcastReceiver {
    private static final String TAG = "PowerButtonReceiver";
    private static long lastPressTime = 0;
    private static int pressCount = 0;
    private static final long WINDOW_MS = 1500; // 1.5s para agrupar presiones

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action) || Intent.ACTION_SCREEN_ON.equals(action)) {
            long now = System.currentTimeMillis();

            if (now - lastPressTime <= WINDOW_MS) {
                pressCount++;
            } else {
                pressCount = 1;
            }
            lastPressTime = now;

            Log.d(TAG, "Power press detected. count=" + pressCount);

            if (pressCount >= 3) {
                pressCount = 0;
                // Lanzar el servicio de emergencia
                Intent serviceIntent = new Intent(context, com.example.aura.services.EmergencyService.class);
                try {
                    // Start foreground service on Android O+
                    context.startForegroundService(serviceIntent);
                } catch (Exception e) {
                    context.startService(serviceIntent);
                }
            }

            // reset por seguridad después de la ventana
            new Handler(Looper.getMainLooper()).postDelayed(() -> pressCount = 0, WINDOW_MS + 100);
        }
    }
}

