package com.example.aura.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.aura.data.dao.ContactDao;
import com.example.aura.data.entities.Contact;

@Database(entities = {Contact.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ContactDao contactDao();
}
