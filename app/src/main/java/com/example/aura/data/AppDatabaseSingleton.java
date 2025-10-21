package com.example.aura.data;

import android.content.Context;
import androidx.room.Room;

// Esta clase garantiza que la base de datos Room se cree una sola vez (Singleton)
public class AppDatabaseSingleton {

    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "contactdata")
                            .allowMainThreadQueries() // solo para pruebas iniciales
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
