package com.example.aura.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.aura.data.dao.ContactDao;
import com.example.aura.data.dao.ReportDao;
import com.example.aura.data.dao.UserDao;
import com.example.aura.data.entities.Contact;
import com.example.aura.data.entities.ReportEntity;
import com.example.aura.data.entities.User;

/**
 * Base de datos unificada de Aura.
 * Contiene:
 *  - Usuarios del sistema
 *  - Contactos de emergencia
 *  - Reportes de zonas inseguras
 */
@Database(
        entities = {Contact.class, ReportEntity.class, User.class},
        version = 3, // incrementamos la versión por el cambio en el esquema
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // DAOs de cada módulo
    public abstract ContactDao contactDao();
    public abstract ReportDao reportDao();
    public abstract UserDao userDao(); // Añadimos el DAO de usuario

    // Singleton para acceder a la DB
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "aura.db"
                            )
                            .fallbackToDestructiveMigration() // evita errores de versión
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
