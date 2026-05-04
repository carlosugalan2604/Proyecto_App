package com.cidead.pmdm.proyecto_app.db.entity;

// Entidad Room que representa un usuario del sistema (gerente, trabajador o cliente).
// Las anotaciones @Entity, @PrimaryKey y @ColumnInfo son de Room y definen la estructura
// de la tabla "usuario" en la base de datos local del dispositivo.
// En esta app los datos vienen de la API, pero la entidad sirve también como modelo de datos.

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "usuario",
    // El email debe ser único en toda la tabla: no puede haber dos usuarios con el mismo email
    indices = {@Index(value = "email", unique = true)}
)
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario")
    public int idUsuario;

    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password")
    public String password;

    /** Rol del usuario en el sistema. Valores posibles: "GERENTE", "TRABAJADOR", "CLIENTE" */
    @ColumnInfo(name = "rol")
    public String rol;

    /** Si es false, el usuario no puede iniciar sesión (cuenta desactivada por el gerente) */
    @ColumnInfo(name = "activo")
    public boolean activo;

    // Constructor que inicializa todos los campos excepto el id (se genera automáticamente)
    public Usuario(String nombre, String email, String password, String rol, boolean activo) {
        this.nombre   = nombre;
        this.email    = email;
        this.password = password;
        this.rol      = rol;
        this.activo   = activo;
    }
}
