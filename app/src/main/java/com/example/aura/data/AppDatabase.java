package com.example.aura.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Se eliminan las importaciones de Contact y ContactDao ya que no se usarán más en Room.
// import com.example.aura.data.dao.ContactDao;
import com.example.aura.data.dao.ReportDao;
import com.example.aura.data.dao.UserDao;
// import com.example.aura.data.entities.Contact;
import com.example.aura.data.entities.ReportEntity;
import com.example.aura.data.entities.User;

/**
 * Base de datos unificada de Aura.
 * Contiene:
 *  - Usuarios del sistema
 *  - Reportes de zonas inseguras
 *  (Se elimina la referencia a Contactos de emergencia de la descripción)
 */
@Database(
        // ================== CAMBIO APLICADO AQUÍ ==================
        // Se elimina "Contact.class" de la lista de entidades.
        entities = {ReportEntity.class, User.class},
        // ========================================================
        version = 3, // Puedes mantener o incrementar la versión, no hay problema.
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // DAOs de cada módulo
    // ================== CAMBIO APLICADO AQUÍ ==================
    // Se elimina el método abstracto para ContactDao.
    // public abstract ContactDao contactDao();
    // ========================================================
    public abstract ReportDao reportDao();
    public abstract UserDao userDao();

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
