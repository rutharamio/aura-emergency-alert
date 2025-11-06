package com.example.aura.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Clase de utilidad para gestionar la sesión del usuario usando SharedPreferences.
 */
public class Prefs {

    private static final String FILE = "aura_prefs";
    private static final String KEY_LOGGED_IN_USER_ID = "logged_in_user_id";
    private static final String KEY_LOGGED_IN_USER_NAME = "logged_in_user_name";

    /**
     * Guarda los datos del usuario para crear una sesión.
     */
    public static void saveUserSession(Context ctx, int userId, String name) {
        SharedPreferences sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        sp.edit()
                .putInt(KEY_LOGGED_IN_USER_ID, userId)
                .putString(KEY_LOGGED_IN_USER_NAME, name)
                .apply();
    }

    /**
     * Verifica si hay una sesión de usuario activa.
     * @return true si el ID de usuario existe y es válido, false en caso contrario.
     */
    public static boolean isUserLoggedIn(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        // Si el ID guardado es -1 (valor por defecto), no hay sesión.
        return sp.getInt(KEY_LOGGED_IN_USER_ID, -1) != -1;
    }

    /**
     * Obtiene el nombre del usuario con sesión activa.
     */
    public static String getLoggedInUserName(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        return sp.getString(KEY_LOGGED_IN_USER_NAME, "");
    }

    /**
     * Obtiene el ID del usuario con sesión activa.
     */
    public static int getLoggedInUserId(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        return sp.getInt(KEY_LOGGED_IN_USER_ID, -1);
    }

    /**
     * Cierra la sesión del usuario, eliminando sus datos.
     */
    public static void clearSession(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        sp.edit()
            .remove(KEY_LOGGED_IN_USER_ID)
            .remove(KEY_LOGGED_IN_USER_NAME)
            .apply();
    }
}
