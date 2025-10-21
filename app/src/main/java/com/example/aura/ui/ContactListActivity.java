package com.example.aura.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.aura.data.AppDatabase;
import com.example.aura.data.AppDatabaseSingleton;
import com.example.aura.databinding.ActivityContactListBinding;
import com.example.aura.ui.adapters.ContactAdapter;

public class ContactListActivity extends AppCompatActivity {

    private ActivityContactListBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabaseSingleton.getInstance(this); // usar singleton
        binding.recyclerContacts.setLayoutManager(new LinearLayoutManager(this));

        var contactList = db.contactDao().getAllContacts();
        binding.recyclerContacts.setAdapter(new ContactAdapter(contactList));
    }
}
