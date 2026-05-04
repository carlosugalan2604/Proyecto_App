package com.cidead.pmdm.proyecto_app.db.entity;

// Entidad Room que representa un pedido realizado por un cliente.
// Guarda la fecha, el estado del pedido y el importe total.
// La clave foránea a Usuario referencia al cliente que hizo el pedido.
// onDelete = CASCADE: si se borra el cliente, se borran también sus pedidos.

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "pedido",
    foreignKeys = @ForeignKey(
        entity    = Usuario.class,
        parentColumns = "id_usuario",
        childColumns  = "id_usuario",
        onDelete      = ForeignKey.CASCADE
    ),
    // El índice sobre id_usuario acelera la búsqueda de pedidos por cliente
    indices = {@Index("id_usuario")}
)
public class Pedido {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_pedido")
    public int idPedido;

    /** Fecha del pedido en formato "dd/MM/yyyy" */
    @ColumnInfo(name = "fecha_pedido")
    public String fechaPedido;

    /** Estado del pedido. Valores: "EN_PROCESO", "ENTREGADO", "CANCELADO" */
    @ColumnInfo(name = "estado")
    public String estado;

    /** Suma total de todos los productos del pedido (precio × cantidad de cada línea) */
    @ColumnInfo(name = "importe_total")
    public double importeTotal;

    /** Id del cliente que realizó este pedido */
    @ColumnInfo(name = "id_usuario")
    public int idUsuario;

    // Constructor que inicializa todos los campos excepto el id (se genera automáticamente)
    public Pedido(String fechaPedido, String estado, double importeTotal, int idUsuario) {
        this.fechaPedido  = fechaPedido;
        this.estado       = estado;
        this.importeTotal = importeTotal;
        this.idUsuario    = idUsuario;
    }
}
