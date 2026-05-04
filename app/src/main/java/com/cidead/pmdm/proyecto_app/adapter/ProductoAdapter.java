package com.cidead.pmdm.proyecto_app.adapter;

// Adapter que conecta la lista de productos del inventario con el RecyclerView.
// Cada fila muestra el nombre, precio, stock actual y un badge de estado:
//   - "Óptimo"     → stock por encima del mínimo (verde)
//   - "Bajo Stock" → stock igual o menor al mínimo (naranja)
//   - "Agotado"    → stock a 0 (rojo)
// También tiene botones para editar o eliminar cada producto.

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cidead.pmdm.proyecto_app.R;
import com.cidead.pmdm.proyecto_app.db.entity.Producto;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ViewHolder> {

    /*
     * Interfaz de callback con dos métodos para que la Activity reaccione
     * cuando el usuario pulsa "Editar" o "Eliminar" en un producto.
     */
    public interface OnProductoClickListener {
        void onEditar(Producto producto);
        void onEliminar(Producto producto);
    }

    // Lista de productos que se muestra en pantalla
    private List<Producto> lista;
    // Objeto que se llama cuando el usuario pulsa editar o eliminar
    private final OnProductoClickListener listener;

    // Constructor: recibe la lista inicial y el listener de acciones
    public ProductoAdapter(List<Producto> lista, OnProductoClickListener listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    // Reemplaza toda la lista y notifica al RecyclerView que debe redibujarse
    public void setLista(List<Producto> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    // Infla el layout XML de una fila de producto y crea el ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ViewHolder(v);
    }

    // Rellena los datos de la fila en la posición indicada
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Producto p = lista.get(position);

        // Rellenamos los textos de nombre, precio y stock
        h.tvNombre.setText(p.nombre);
        h.tvPrecio.setText(String.format("€ %.2f", p.precio));
        h.tvStock.setText("Stock: " + p.stockActual);

        // Determinamos el badge de estado según el nivel de stock
        if (p.stockActual == 0) {
            // Sin unidades: peligro
            h.tvEstado.setText("Agotado");
            h.tvEstado.setBackgroundColor(Color.parseColor("#F44336")); // rojo
        } else if (p.stockActual <= p.stockMinimo) {
            // Stock por debajo del mínimo: aviso
            h.tvEstado.setText("Bajo Stock");
            h.tvEstado.setBackgroundColor(Color.parseColor("#FF9800")); // naranja
        } else {
            // Stock correcto
            h.tvEstado.setText("Óptimo");
            h.tvEstado.setBackgroundColor(Color.parseColor("#4CAF50")); // verde
        }

        // Conectamos los botones con las acciones de la Activity
        h.btnEditar.setOnClickListener(v -> listener.onEditar(p));
        h.btnEliminar.setOnClickListener(v -> listener.onEliminar(p));
    }

    // Devuelve cuántas filas hay que mostrar
    @Override
    public int getItemCount() {
        return lista == null ? 0 : lista.size();
    }

    // ViewHolder: guarda las referencias a los elementos visuales de cada fila
    // Los botones de editar y eliminar son TextViews con estilo de botón
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrecio, tvStock, tvEstado;
        TextView btnEditar, btnEliminar;

        ViewHolder(View v) {
            super(v);
            tvNombre    = v.findViewById(R.id.tvNombreProducto);
            tvPrecio    = v.findViewById(R.id.tvPrecio);
            tvStock     = v.findViewById(R.id.tvStock);
            tvEstado    = v.findViewById(R.id.tvEstadoStock);
            btnEditar   = v.findViewById(R.id.btnEditar);
            btnEliminar = v.findViewById(R.id.btnEliminar);
        }
    }
}
