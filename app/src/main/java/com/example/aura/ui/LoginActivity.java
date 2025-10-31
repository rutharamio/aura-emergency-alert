package com.example.aura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aura.MainActivity;
import com.example.aura.R;
import com.example.aura.core.PasswordHasher;
import com.example.aura.core.Prefs;
import com.example.aura.data.AppDatabase;
import com.example.aura.data.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Verificar si ya hay una sesión activa
        if (Prefs.isUserLoggedIn(this)) {
            goToMain();
            return; // Importante: salir del onCreate para no mostrar la UI de login
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        db = AppDatabase.getInstance(this);

        btnLogin.setOnClickListener(v -> loginUser());
        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Por favor, complete todos los campos");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 1. Buscar al usuario por email
                User user = db.userDao().findByEmail(email);

                if (user == null) {
                    showToast("Usuario no encontrado. Por favor, regístrese.");
                    return;
                }

                // 2. Verificar la contraseña hasheada
                if (PasswordHasher.verifyPassword(password, user.passwordHash)) {
                    // ¡Login exitoso!
                    showToast("¡Bienvenido, " + user.name + "!");

                    // 3. Guardar la sesión del usuario
                    Prefs.saveUserSession(this, user.id, user.name);

                    goToMain();
                } else {
                    showToast("Contraseña incorrecta");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error en el login: " + e.getMessage());
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        // Limpiar el stack de actividades para que el usuario no pueda volver al login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}
