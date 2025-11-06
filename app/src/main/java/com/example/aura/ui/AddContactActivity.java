package com.example.aura.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// ===== NUEVOS IMPORTS PARA FIREBASE =====
import com.example.aura.data.entities.Contact; // Asegúrate que este es tu Contact.java modificado
import com.example.aura.databinding.ActivityAddContactBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Ya no se necesitan AppDatabase, ExecutorService, etc.

public class AddContactActivity extends AppCompatActivity {

    private ActivityAddContactBinding binding;
    // Creamos una referencia a la base de datos de Firebase para los contactos del usuario
    private DatabaseReference userContactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos tu View Binding, ¡está perfecto!
        binding = ActivityAddContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Obtenemos el usuario actual de Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Si por alguna razón no hay usuario, no se puede guardar nada.
            Toast.makeText(this, "Error: Debes iniciar sesión para agregar contactos.", Toast.LENGTH_LONG).show();
            finish(); // Cierra la actividad
            return;
        }

        // 2. Creamos la referencia a la "carpeta" de contactos específica de este usuario en Firebase
        // La ruta será: /contacts/{ID_DEL_USUARIO}
        userContactsRef = FirebaseDatabase.getInstance().getReference("contacts").child(currentUser.getUid());

        // 3. Asignamos la nueva lógica al botón de guardar
        binding.saveButton.setOnClickListener(v -> saveContactToFirebase());
    }

    private void saveContactToFirebase() {
        // Obtenemos los datos desde el View Binding, como ya lo hacías
        String name = binding.nameInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();
        String relation = binding.relationInput.getText().toString().trim();
        // El campo 'priority' no lo incluimos en el nuevo Contact.java, pero puedes añadirlo si quieres.

        // Validamos que los campos obligatorios no estén vacíos
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Completá los campos obligatorios (nombre y teléfono).", Toast.LENGTH_SHORT).show();
            return;
        }

        // LÓGICA PARA LIMITAR A 5 CONTACTOS
        // Leemos los datos UNA SOLA VEZ desde Firebase para contar cuántos contactos hay.
        binding.saveButton.setEnabled(false); // Deshabilitamos el botón para evitar dobles clics
        userContactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // snapshot.getChildrenCount() nos da el número de contactos que ya existen.
                if (snapshot.getChildrenCount() >= 5) {
                    // Si ya hay 5 o más, mostramos un error y no hacemos nada más.
                    Toast.makeText(AddContactActivity.this, "Límite alcanzado. No puedes agregar más de 5 contactos.", Toast.LENGTH_LONG).show();
                    binding.saveButton.setEnabled(true); // Rehabilitamos el botón
                } else {
                    // Si hay espacio, procedemos a guardar el nuevo contacto.
                    performSave(name, phone, relation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // En caso de que no se pueda leer la base de datos por algún error.
                Toast.makeText(AddContactActivity.this, "Error al verificar contactos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                binding.saveButton.setEnabled(true); // Rehabilitamos el botón
            }
        });
    }

    private void performSave(String name, String phone, String relation) {
        // Generamos una clave/ID único para el nuevo contacto usando push().getKey()
        String contactId = userContactsRef.push().getKey();

        // Creamos nuestro nuevo objeto Contact
        Contact newContact = new Contact(name, phone, relation);

        if (contactId != null) {
            // Guardamos el objeto Contact completo en Firebase bajo el nuevo ID
            userContactsRef.child(contactId).setValue(newContact)
                    .addOnSuccessListener(aVoid -> {
                        // Si se guarda con éxito
                        Toast.makeText(AddContactActivity.this, "Contacto guardado exitosamente.", Toast.LENGTH_SHORT).show();
                        finish(); // Cierra la actividad y vuelve a la anterior
                    })
                    .addOnFailureListener(e -> {
                        // Si hay un error al guardar en Firebase
                        Toast.makeText(AddContactActivity.this, "Error al guardar el contacto.", Toast.LENGTH_SHORT).show();
                        binding.saveButton.setEnabled(true); // Rehabilitamos el botón si falla
                    });
        } else {
            binding.saveButton.setEnabled(true);
        }
    }
}
