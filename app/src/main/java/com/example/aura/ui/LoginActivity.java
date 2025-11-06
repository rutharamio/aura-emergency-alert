package com.example.aura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aura.MainActivity;
import com.example.aura.R;
import com.example.aura.core.AuthManager;
import com.example.aura.core.PasswordHasher;
import com.example.aura.core.Prefs;
import com.example.aura.data.AppDatabase;
import com.example.aura.data.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        db = AppDatabase.getInstance(this);

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Por favor, complete todos los campos");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Correo electrónico inválido");
            return;
        }

        // ✅ Paso 1: Intentar login con Firebase
        AuthManager.login(email, password, this, () -> {
            // Si Firebase confirma el login, sincronizamos con Room
            syncLocalUser(email, password);

        }, () -> {
            // Si Firebase falla (por ejemplo, sin conexión), probamos login offline
            loginOffline(email, password);
        });
    }

    // 🔹 Si Firebase tuvo éxito, sincronizamos localmente
    private void syncLocalUser(String email, String password) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                User localUser = db.userDao().findByEmail(email);
                if (localUser == null) {
                    // Si el usuario no existe localmente, lo guardamos para acceso offline
                    User newUser = new User();
                    newUser.name = email.split("@")[0]; // usamos parte del email como nombre
                    newUser.email = email;
                    newUser.passwordHash = PasswordHasher.hashPassword(password);
                    long id = db.userDao().insert(newUser);
                    Prefs.saveUserSession(this, (int) id, newUser.name);
                } else {
                    Prefs.saveUserSession(this, localUser.id, localUser.name);
                }

                showToast("Inicio de sesión exitoso");
                goToMain();

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error al sincronizar usuario local");
            }
        });
    }

    // 🔹 Si no hay conexión, intentamos login localmente con Room
    private void loginOffline(String email, String password) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                User user = db.userDao().findByEmail(email);
                if (user == null) {
                    showToast("Usuario no encontrado localmente");
                    return;
                }

                // Verificamos contraseña
                String hashed = PasswordHasher.hashPassword(password);
                if (!hashed.equals(user.passwordHash)) {
                    showToast("Contraseña incorrecta");
                    return;
                }

                Prefs.saveUserSession(this, user.id, user.name);
                showToast("Inicio de sesión offline");
                goToMain();

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error al iniciar sesión offline");
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}
