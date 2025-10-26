package com.example.aura.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.aura.data.AppDatabase;
import com.example.aura.data.AppDatabaseSingleton;
import com.example.aura.data.SessionManager;
import com.example.aura.data.entities.Contact;
import com.example.aura.databinding.ActivityAddContactBinding;

import java.util.List;

public class AddContactActivity extends AppCompatActivity {

    private ActivityAddContactBinding binding;
    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabaseSingleton.getInstance(this);
        sessionManager = new SessionManager(this); // para saber qué usuario está logueado

        binding.saveButton.setOnClickListener(v -> {
            String name = binding.nameInput.getText().toString().trim();
            String phone = binding.phoneInput.getText().toString().trim();
            String relation = binding.relationInput.getText().toString().trim();
            String priority = binding.priorityInput.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Completá los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = sessionManager.getUserId(); // obtener ID del usuario actual

            // Confirmación antes de guardar
            new AlertDialog.Builder(this)
                    .setTitle("Guardar contacto")
                    .setMessage("¿Deseas guardar este contacto de emergencia?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Verificar cantidad de contactos de este usuario
                        List<Contact> userContacts = db.contactDao().getContactsForUser(userId);
                        if (userContacts.size() >= 5) {
                            Toast.makeText(this, "Solo se permiten 5 contactos de emergencia", Toast.LENGTH_LONG).show();
                            return;
                        }

                        try {
                            // Crear contacto vinculado al usuario logueado
                            Contact contact = new Contact(name, phone, relation, priority, userId);
                            db.contactDao().insert(contact);

                            Log.d("ROOM_TEST", "Contacto insertado: " + name);
                            Toast.makeText(this, "Contacto guardado correctamente", Toast.LENGTH_SHORT).show();
                            finish();

                        } catch (Exception e) {
                            Log.e("ROOM_ERROR", "Error al insertar contacto: " + e.getMessage());
                            Toast.makeText(this, "Error al guardar contacto", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }
}
