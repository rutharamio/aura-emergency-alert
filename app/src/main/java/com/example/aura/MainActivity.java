package com.example.aura;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aura.databinding.ActivityMainBinding;
import com.example.aura.ui.AddContactActivity;
import com.example.aura.ui.ContactListActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Botón para abrir la pantalla de agregar contacto
        binding.btnAddContact.setOnClickListener(v -> {
            startActivity(new Intent(this, AddContactActivity.class));
        });

        // Botón para abrir la lista de contactos
        binding.btnViewContacts.setOnClickListener(v -> {
            startActivity(new Intent(this, ContactListActivity.class));
        });
    }
}
