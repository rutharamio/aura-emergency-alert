
package com.example.aura.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import com.example.aura.receivers.PowerButtonReceiver;

public class PowerButtonService extends Service {

    private static final String TAG = "PowerButtonService";
    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio de botón de encendido creado.");
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new PowerButtonReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Servicio de botón de encendido iniciado.");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Servicio de botón de encendido destruido.");
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
