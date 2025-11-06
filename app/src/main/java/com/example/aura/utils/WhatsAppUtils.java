package com.example.aura.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class WhatsAppUtils {
    private static final String TAG = "WhatsAppUtils";

    public static void sendAlert(Context context, String phoneWithCountryCode, double lat, double lon) {
        String message = "¡ALERTA! Necesito ayuda. Mi ubicación es: https://maps.google.com/?q=" + lat + "," + lon;
        String url = "https://wa.me/" + phoneWithCountryCode.replace("+","") + "?text=" + Uri.encode(message);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.d(TAG, "Intent WhatsApp lanzado: " + url);
        } catch (Exception e) {
            Log.e(TAG, "Error lanzando WhatsApp intent", e);
        }
    }
}
