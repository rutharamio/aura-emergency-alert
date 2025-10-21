package com.example.aura.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.aura.data.AppDatabase;
import com.example.aura.data.entities.Contact;
import com.example.aura.databinding.ActivityAddContactBinding;

public class AddContactActivity extends AppCompatActivity {

    private ActivityAddContactBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 🧱 Crear o abrir la base de datos Room
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "contactdata")
                .allowMainThreadQueries() // solo para pruebas
                .fallbackToDestructiveMigration() // fuerza recreación si hay cambios
                .build();

        binding.saveButton.setOnClickListener(v -> {
            String name = binding.nameInput.getText().toString();
            String phone = binding.phoneInput.getText().toString();
            String relation = binding.relationInput.getText().toString();
            String priority = binding.priorityInput.getText().toString();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Completá los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Contact contact = new Contact();
                contact.name = name;
                contact.phone = phone;
                contact.relation = relation;
                contact.priority = priority;

                db.contactDao().insert(contact);

                // 🔍 Verificar si se insertó correctamente
                int total = db.contactDao().getAllContacts().size();
                Log.d("ROOM_TEST", "✅ Contacto insertado: " + name);
                Log.d("ROOM_TEST", "👥 Total de contactos en DB: " + total);

                Toast.makeText(this, "Contacto guardado correctamente", Toast.LENGTH_SHORT).show();
                finish();

            } catch (Exception e) {
                Log.e("ROOM_ERROR", "❌ Error al insertar contacto: " + e.getMessage());
                Toast.makeText(this, "Error al guardar contacto", Toast.LENGTH_SHORT).show();
            }
        });
    }
}