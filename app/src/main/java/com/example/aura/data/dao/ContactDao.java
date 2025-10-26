package com.example.aura.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.aura.data.entities.Contact;
import java.util.List;

// DAO (Data Access Object): define las operaciones sobre la base de datos.
@Dao
public interface ContactDao {

    @Insert
    void insert(Contact contact);

    // 🔹 Obtener todos los contactos de un usuario específico
    @Query("SELECT * FROM contacts WHERE user_id = :userId")
    List<Contact> getContactsForUser(int userId);

    @Delete
    void delete(Contact contact);

    // 🔹 Obtener solo los contactos con prioridad alta de ese usuario
    @Query("SELECT * FROM contacts WHERE (priority = '1' OR priority = 'Alta') AND user_id = :userId ORDER BY id ASC LIMIT 3")
    List<Contact> getContactsForAlert(int userId);
}
