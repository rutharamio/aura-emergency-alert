package com.example.aura.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.aura.data.entities.User;

@Dao
public interface UserDao {

    /**
     * Inserta un nuevo usuario en la base de datos y devuelve su ID.
     * Si el email ya existe, la operación fallará gracias al índice único.
     * @return El ID (rowId) del nuevo usuario insertado.
     */
    // ================== CORRECCIÓN AQUÍ ==================
    // Se cambia "void" por "long" para que el método devuelva el ID.
    @Insert
    long insert(User user);
    // ====================================================

    /**
     * Busca un usuario por su dirección de correo electrónico.
     * @param email El email del usuario a buscar.
     * @return El objeto User si se encuentra, o null si no existe.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);
}
