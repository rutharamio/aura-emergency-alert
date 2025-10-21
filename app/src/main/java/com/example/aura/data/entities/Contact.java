package com.example.aura.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Esta clase representa la tabla de contactos dentro de la base de datos.
@Entity(tableName = "contacts")
public class Contact {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "relation")
    public String relation;

    @ColumnInfo(name = "priority")
    public String priority;
}
