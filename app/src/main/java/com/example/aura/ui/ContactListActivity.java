package com.example.aura.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.aura.data.AppDatabaseSingleton;
import com.example.aura.data.entities.Contact;
import com.example.aura.databinding.ActivityContactListBinding;
import com.example.aura.ui.adapters.ContactAdapter;
import java.util.List;

public class ContactListActivity extends AppCompatActivity {

    private ActivityContactListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar lista
        var db = AppDatabaseSingleton.getInstance(this);
        List<Contact> contactList = db.contactDao().getAllContacts();

        binding.recyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerContacts.setAdapter(new ContactAdapter(contactList));

        // Mostrar texto si no hay contactos
        updateEmptyState(contactList);

        // Botón para agregar contacto
        binding.fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddContactActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar lista al volver desde AddContactActivity
        var db = AppDatabaseSingleton.getInstance(this);
        List<Contact> updatedList = db.contactDao().getAllContacts();
        binding.recyclerContacts.setAdapter(new ContactAdapter(updatedList));
        updateEmptyState(updatedList);
    }

    private void updateEmptyState(List<Contact> list) {
        if (list == null || list.isEmpty()) {
            binding.emptyText.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.emptyText.setVisibility(android.view.View.GONE);
        }
    }
}
