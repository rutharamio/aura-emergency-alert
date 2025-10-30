package com.example.aura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aura.MainActivity; // ¡Importante! Necesitamos la referencia a MainActivity
import com.example.aura.R;
import com.example.aura.core.PasswordHasher;
import com.example.aura.core.Prefs; // ¡Importante! Necesitamos Prefs para guardar la sesión
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

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 1. Verificar si el email ya existe
                User existingUser = db.userDao().findByEmail(email);
                if (existingUser != null) {
                    showToast("El correo electrónico ya está registrado");
                    return;
                }

                // 2. Hashear la contraseña
                String passwordHash = PasswordHasher.hashPassword(password);

                // 3. Crear y guardar el nuevo usuario
                User newUser = new User();
                newUser.name = name;
                newUser.email = email;
                newUser.passwordHash = passwordHash;
                // Obtenemos el ID del nuevo usuario insertado
                long newUserId = db.userDao().insert(newUser);

                // ===================== CORRECCIÓN APLICADA AQUÍ =====================

                // 4. Iniciar sesión automáticamente y redirigir a MainActivity
                if (newUserId > 0) { // Verificamos que el usuario se insertó correctamente
                    showToast("¡Bienvenido, " + newUser.name + "!");

                    // 4.1. Guardar la sesión del nuevo usuario
                    Prefs.saveUserSession(getApplicationContext(), (int) newUserId, newUser.name);

                    // 4.2. Redirigir DIRECTAMENTE a MainActivity (el mapa)
                    goToMain();

                } else {
                    showToast("Error: no se pudo completar el registro.");
                }
                // ======================= FIN DE LA CORRECCIÓN =======================

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error en el registro: " + e.getMessage());
            }
        });
    }

    // Método de utilidad para ir a la pantalla principal
    private void goToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        // Limpiamos el stack para que el usuario no pueda volver al registro o login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Método de utilidad para mostrar Toasts desde cualquier hilo
    private void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}
