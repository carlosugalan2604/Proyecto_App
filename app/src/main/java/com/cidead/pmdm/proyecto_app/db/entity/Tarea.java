package com.cidead.pmdm.proyecto_app.db.entity;

// Entidad Room que representa una tarea asignada a un trabajador.
// Las tareas siguen un ciclo de tres estados: PENDIENTE → EN_PROGRESO → COMPLETADA.
// La clave foránea a Usuario garantiza que la tarea siempre tiene un responsable válido.
// onDelete = CASCADE: si se borra el trabajador, se borran también sus tareas.

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "tarea",
    foreignKeys = @ForeignKey(
        entity = Usuario.class,
        parentColumns = "id_usuario",
        childColumns  = "id_usuario",
        onDelete      = ForeignKey.CASCADE
    ),
    // El índice sobre id_usuario acelera la búsqueda de tareas por trabajador
    indices = {@Index("id_usuario")}
)
public class Tarea {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_tarea")
    public int idTarea;

    @ColumnInfo(name = "titulo")
    public String titulo;

    @ColumnInfo(name = "descripcion")
    public String descripcion;

    /** Fecha en formato "dd/MM/yyyy" (se guarda como texto porque no se necesita operar con ella) */
    @ColumnInfo(name = "fecha_asignacion")
    public String fechaAsignacion;

    /** Estado actual de la tarea. Valores: "PENDIENTE", "EN_PROGRESO", "COMPLETADA" */
    @ColumnInfo(name = "estado")
    public String estado;

    /** Id del trabajador al que está asignada esta tarea */
    @ColumnInfo(name = "id_usuario")
    public int idUsuario;

    // Constructor que inicializa todos los campos excepto el id (se genera automáticamente)
    public Tarea(String titulo, String descripcion, String fechaAsignacion, String estado, int idUsuario) {
        this.titulo          = titulo;
        this.descripcion     = descripcion;
        this.fechaAsignacion = fechaAsignacion;
        this.estado          = estado;
        this.idUsuario       = idUsuario;
    }
}
