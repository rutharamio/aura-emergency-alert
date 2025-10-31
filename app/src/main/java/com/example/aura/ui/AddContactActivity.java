package com.example.aura.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aura.data.AppDatabase;
import com.example.aura.data.entities.Contact;
import com.example.aura.databinding.ActivityAddContactBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddContactActivity extends AppCompatActivity {

    private ActivityAddContactBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ Usar la instancia Singleton correcta de la base de datos
        db = AppDatabase.getInstance(this);

        binding.saveButton.setOnClickListener(v -> {
            String name = binding.nameInput.getText().toString();
            String phone = binding.phoneInput.getText().toString();
            String relation = binding.relationInput.getText().toString();
            String priority = binding.priorityInput.getText().toString();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Completá los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Ejecutar la inserción en un hilo secundario
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    Contact contact = new Contact();
                    contact.name = name;
                    contact.phone = phone;
                    contact.relation = relation;
                    contact.priority = priority;

                    db.contactDao().insert(contact);

                    // Volver al hilo principal para mostrar el Toast y cerrar la actividad
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Contacto guardado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    });

                } catch (Exception e) {
                    Log.e("ROOM_ERROR", "❌ Error al insertar contacto: " + e.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error al guardar contacto", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
}
