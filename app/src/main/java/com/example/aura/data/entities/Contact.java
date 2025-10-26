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

    // 🔗 Clave foránea: el ID del usuario al que pertenece este contacto
    @ColumnInfo(name = "user_id")
    public int userId;

    // Constructor
    public Contact(String name, String phone, String relation, String priority, int userId) {
        this.name = name;
        this.phone = phone;
        this.relation = relation;
        this.priority = priority;
        this.userId = userId;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
