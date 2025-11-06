package com.example.aura.core;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {

    private static final String TAG = "AuthManager";
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Registrar usuario
    public static void register(String email, String password, Activity activity,
                                Runnable onSuccess, Runnable onError) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Usuario registrado: " + mAuth.getCurrentUser().getEmail());
                        onSuccess.run();
                    } else {
                        Log.e(TAG, "❌ Error al registrar", task.getException());
                        onError.run();
                    }
                });
    }

    // Iniciar sesión
    public static void login(String email, String password, Activity activity,
                             Runnable onSuccess, Runnable onError) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "✅ Sesión iniciada: " + user.getEmail());
                        onSuccess.run();
                    } else {
                        Log.e(TAG, "❌ Error al iniciar sesión", task.getException());
                        onError.run();
                    }
                });
    }

    // Obtener usuario actual
    public static FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // Cerrar sesión
    public static void logout() {
        mAuth.signOut();
    }
}
