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

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        db = AppDatabase.getInstance(this);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("Por favor, complete todos los campos");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Por favor, ingrese un correo válido");
            return;
        }

        if (password.length() < 6) {
            showToast("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        // ✅ Paso 1: Registrar en Firebase
        AuthManager.register(email, password, this, () -> {
            // Si Firebase lo registra correctamente → seguimos guardando localmente
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    // 2. Verificar si ya existe en Room
                    User existingUser = db.userDao().findByEmail(email);
                    if (existingUser != null) {
                        showToast("El correo electrónico ya está registrado localmente");
                        return;
                    }

                    // 3. Hashear la contraseña y guardar localmente
                    String passwordHash = PasswordHasher.hashPassword(password);

                    User newUser = new User();
                    newUser.name = name;
                    newUser.email = email;
                    newUser.passwordHash = passwordHash;
                    long newUserId = db.userDao().insert(newUser);

                    if (newUserId > 0) {
                        // 4. Guardar sesión local con Prefs
                        Prefs.saveUserSession(getApplicationContext(), (int) newUserId, newUser.name);
                        showToast("¡Bienvenido, " + name + "! Registro exitoso ✅");

                        // 5. Ir al mapa
                        goToMain();
                    } else {
                        showToast("Error al registrar localmente");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Error en el registro: " + e.getMessage());
                }
            });

        }, () -> {
            showToast("Error al registrar usuario en Firebase");
        });
    }

    private void goToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}
