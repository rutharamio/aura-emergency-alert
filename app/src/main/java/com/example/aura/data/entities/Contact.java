package com.example.aura.data.entities;

// No se necesitan imports de Room, así que se pueden borrar si los tenías.

// Se eliminan TODAS las anotaciones de Room como:
// @Entity, @PrimaryKey, @ColumnInfo, etc.

public class Contact {

    // Firebase usará estos nombres de variables como las "claves" en la base de datos.
    public String id;       // Para guardar la clave única que Firebase genera para cada contacto.
    public String name;
    public String phone;
    public String relation; // Este campo es opcional, pero útil.

    /**
     * Constructor vacío.
     * ESTO ES OBLIGATORIO para que Firebase pueda leer los datos
     * y reconstruir el objeto Contact.
     */
    public Contact() {
    }

    /**
     * Constructor útil para crear nuevos objetos Contact fácilmente desde nuestro código.
     */
    public Contact(String name, String phone, String relation) {
        this.name = name;
        this.phone = phone;
        this.relation = relation;
    }

    // Aunque los campos son públicos, es una buena práctica tener getters y setters.
    // Firebase puede usarlos si los encuentra.
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
