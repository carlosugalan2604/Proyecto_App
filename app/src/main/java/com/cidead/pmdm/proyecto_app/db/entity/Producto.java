package com.cidead.pmdm.proyecto_app.db.entity;

// Entidad Room que representa un producto del inventario.
// Guarda el nombre, precio, proveedor y los niveles de stock actual y mínimo.
// Cuando el stock actual es ≤ stockMinimo se considera "bajo", y si es 0, "agotado".

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "producto")
public class Producto {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_producto")
    public int idProducto;

    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "descripcion")
    public String descripcion;

    @ColumnInfo(name = "proveedor")
    public String proveedor;

    @ColumnInfo(name = "precio")
    public double precio;

    /** Unidades disponibles actualmente en el almacén */
    @ColumnInfo(name = "stock_actual")
    public int stockActual;

    /** Umbral mínimo: si el stock cae por debajo de este valor se genera una alerta */
    @ColumnInfo(name = "stock_minimo")
    public int stockMinimo;

    // Constructor que inicializa todos los campos excepto el id (se genera automáticamente)
    public Producto(String nombre, String descripcion, String proveedor, double precio, int stockActual, int stockMinimo) {
        this.nombre      = nombre;
        this.descripcion = descripcion;
        this.proveedor   = proveedor;
        this.precio      = precio;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }
}
