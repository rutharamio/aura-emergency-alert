package com.example.aura.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.aura.data.AppDatabase;
import com.example.aura.databinding.ActivityContactListBinding;
import com.example.aura.ui.adapters.ContactAdapter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactListActivity extends AppCompatActivity {

    private ActivityContactListBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this); // usar singleton
        binding.recyclerContacts.setLayoutManager(new LinearLayoutManager(this));

        // Executor para correr la consulta en un hilo secundario
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            var contactList = db.contactDao().getAllContacts();

            // Actualizar la UI en el hilo principal
            runOnUiThread(() -> {
                binding.recyclerContacts.setAdapter(new ContactAdapter(contactList));
            });
        });
    }
}
