package com.cidead.pmdm.proyecto_app.db.entity;

// Entidad Room que representa la tabla intermedia entre Pedido y Producto.
// Resuelve la relación N:M (un pedido puede tener varios productos y un producto
// puede aparecer en varios pedidos). La clave primaria es compuesta: (id_pedido, id_producto).
// El campo "cantidad" indica cuántas unidades de ese producto se pidieron en ese pedido.

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName  = "pedido_producto",
    // Clave primaria compuesta: un producto solo puede aparecer una vez por pedido
    primaryKeys = {"id_pedido", "id_producto"},
    foreignKeys = {
        @ForeignKey(
            entity        = Pedido.class,
            parentColumns = "id_pedido",
            childColumns  = "id_pedido",
            onDelete      = ForeignKey.CASCADE  // Si se borra el pedido, se borran sus líneas
        ),
        @ForeignKey(
            entity        = Producto.class,
            parentColumns = "id_producto",
            childColumns  = "id_producto",
            onDelete      = ForeignKey.CASCADE  // Si se borra el producto, se borran las líneas que lo usen
        )
    },
    // El índice sobre id_producto evita el full scan al consultar en qué pedidos aparece un producto
    indices = {@Index("id_producto")}
)
public class PedidoProducto {

    @ColumnInfo(name = "id_pedido")
    public int idPedido;

    @ColumnInfo(name = "id_producto")
    public int idProducto;

    /** Número de unidades de este producto incluidas en el pedido */
    @ColumnInfo(name = "cantidad")
    public int cantidad;

    // Constructor que inicializa los tres campos que forman una línea de pedido
    public PedidoProducto(int idPedido, int idProducto, int cantidad) {
        this.idPedido   = idPedido;
        this.idProducto = idProducto;
        this.cantidad   = cantidad;
    }
}
