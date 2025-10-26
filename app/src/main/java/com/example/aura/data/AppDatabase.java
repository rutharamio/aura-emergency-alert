package com.example.aura.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.aura.data.dao.ContactDao;
import com.example.aura.data.dao.UserDao;
import com.example.aura.data.entities.Contact;
import com.example.aura.data.entities.User;

@Database(entities = {Contact.class, User.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ContactDao contactDao();
    public abstract UserDao userDao();
}
