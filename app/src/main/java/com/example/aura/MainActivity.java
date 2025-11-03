package com.example.aura;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aura.services.PowerButtonService;
import com.example.aura.ui.AddContactActivity;
import com.example.aura.ui.ContactListActivity;
import com.example.aura.ui.EmergencyModuleActivity;


public class MainActivity extends AppCompatActivity {

    private Button btnAddContact;
    private Button btnViewContacts;
    private Button btnEmergency;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Asegurate de tener el layout correcto

        // Iniciar el servicio para detectar el botón de encendido
        Intent powerButtonServiceIntent = new Intent(this, PowerButtonService.class);
        startService(powerButtonServiceIntent);

        // Vinculamos los botones del XML
        btnAddContact = findViewById(R.id.btnAddContact);
        btnViewContacts = findViewById(R.id.btnViewContacts);

        // Ir a la pantalla para agregar contacto
        btnAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
            startActivity(intent);
        });

        // Ir a la lista de contactos
        btnViewContacts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ContactListActivity.class);
            startActivity(intent);
        });
        btnEmergency = findViewById(R.id.btnEmergency);

        btnEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EmergencyModuleActivity.class);
            startActivity(intent);
        });

    }
}
