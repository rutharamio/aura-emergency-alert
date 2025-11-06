package com.example.aura.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Definimos la tabla "users" y nos aseguramos de que el email sea único
@Entity(tableName = "users", indices = {@Index(value = "email", unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "email")
    public String email;

    // Guardaremos el hash de la contraseña, no la contraseña en texto plano
    @ColumnInfo(name = "password_hash")
    public String passwordHash;
}
