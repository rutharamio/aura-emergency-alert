package com.example.aura.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

// ===== NUEVOS IMPORTS PARA FIREBASE =====
import com.example.aura.data.entities.Contact;
import com.example.aura.databinding.ActivityContactListBinding;
import com.example.aura.ui.adapters.ContactAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// Ya no se necesitan AppDatabase ni ExecutorService

public class ContactListActivity extends AppCompatActivity {

    private ActivityContactListBinding binding;
    private DatabaseReference userContactsRef; // Referencia a la "carpeta" de contactos del usuario
    private ContactAdapter adapter;
    private List<Contact> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Configuración inicial del RecyclerView
        binding.recyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        contactList = new ArrayList<>();
        adapter = new ContactAdapter(contactList);
        binding.recyclerContacts.setAdapter(adapter);

        // 2. Obtenemos el usuario actual de Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: Debes iniciar sesión para ver tus contactos.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 3. Creamos la referencia a la "carpeta" de contactos específica de este usuario
        userContactsRef = FirebaseDatabase.getInstance().getReference("contacts").child(currentUser.getUid());

        // 4. Implementamos la funcionalidad de ELIMINAR con un clic largo
        setupLongClickListener();

        // 5. Cargamos los contactos desde Firebase
        loadContactsFromFirebase();
    }

    private void loadContactsFromFirebase() {
        binding.progressBar.setVisibility(View.VISIBLE); // Mostramos una barra de progreso mientras carga

        // addValueEventListener se mantiene escuchando cambios en tiempo real.
        // Si añades o eliminas un contacto, la lista se actualizará automáticamente.
        userContactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear(); // Limpiamos la lista para no duplicar datos en cada actualización
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    Contact contact = contactSnapshot.getValue(Contact.class);
                    if (contact != null) {
                        // Guardamos la clave única de Firebase en nuestro objeto. Es crucial para poder eliminarlo.
                        contact.setId(contactSnapshot.getKey());
                        contactList.add(contact);
                    }
                }
                adapter.notifyDataSetChanged(); // Notificamos al RecyclerView que los datos han cambiado
                binding.progressBar.setVisibility(View.GONE); // Ocultamos la barra de progreso
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ContactListActivity.this, "Error al cargar los contactos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLongClickListener() {
        // Asumimos que tu ContactAdapter tiene un método para configurar este listener.
        // Si no lo tiene, deberás añadirlo.
        adapter.setOnItemLongClickListener(contact -> {
            new AlertDialog.Builder(ContactListActivity.this)
                    .setTitle("Eliminar Contacto")
                    .setMessage("¿Estás seguro de que deseas eliminar a " + contact.getName() + "?")
                    .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                        deleteContactFromFirebase(contact);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void deleteContactFromFirebase(Contact contact) {
        if (contact.getId() == null || contact.getId().isEmpty()) {
            Toast.makeText(this, "No se puede eliminar el contacto (ID no encontrado).", Toast.LENGTH_SHORT).show();
            return;
        }

        // Usamos el ID del contacto para eliminarlo de la "carpeta" del usuario en Firebase
        userContactsRef.child(contact.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ContactListActivity.this, "Contacto eliminado.", Toast.LENGTH_SHORT).show();
                    // No es necesario actualizar la lista manualmente, el ValueEventListener lo hará por nosotros.
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ContactListActivity.this, "Error al eliminar el contacto.", Toast.LENGTH_SHORT).show();
                });
    }
}
