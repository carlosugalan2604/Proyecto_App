package com.cidead.pmdm.proyecto_app.adapter;

// Adapter para la lista de productos en la pantalla "Nuevo Pedido".
// Cada fila muestra el nombre, precio y un control de cantidad (- N +).
// Cuando el usuario toca + o -, avisa a NuevoPedidoActivity para recalcular el total.

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cidead.pmdm.proyecto_app.R;

import java.util.List;

public class ProductoPedidoAdapter extends RecyclerView.Adapter<ProductoPedidoAdapter.ViewHolder> {

    // Datos de un producto que se muestra en esta pantalla
    public static class ItemProducto {
        public final int    idProducto;
        public final String nombre;
        public final double precio;
        public int          cantidad; // Empieza en 0, el usuario lo sube con los botones

        public ItemProducto(int idProducto, String nombre, double precio) {
            this.idProducto = idProducto;
            this.nombre     = nombre;
            this.precio     = precio;
            this.cantidad   = 0;
        }
    }

    private List<ItemProducto> lista;
    // Callback que se llama cada vez que cambia una cantidad, para actualizar el total
    private final Runnable onCantidadCambiada;

    public ProductoPedidoAdapter(List<ItemProducto> lista, Runnable onCantidadCambiada) {
        this.lista               = lista;
        this.onCantidadCambiada  = onCantidadCambiada;
    }

    // Reemplaza la lista y redibuja todo
    public void setLista(List<ItemProducto> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    // Devuelve la lista actual (la usa NuevoPedidoActivity para calcular el total y las líneas)
    public List<ItemProducto> getLista() {
        return lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto_pedido, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ItemProducto item = lista.get(position);

        h.tvNombre.setText(item.nombre);
        h.tvPrecio.setText(String.format("€ %.2f c/u", item.precio));
        h.tvCantidad.setText(String.valueOf(item.cantidad));

        // Botón menos: reduce la cantidad hasta un mínimo de 0
        h.btnMenos.setOnClickListener(v -> {
            if (item.cantidad > 0) {
                item.cantidad--;
                h.tvCantidad.setText(String.valueOf(item.cantidad));
                onCantidadCambiada.run(); // Avisamos para actualizar el total
            }
        });

        // Botón más: aumenta la cantidad sin límite práctico
        h.btnMas.setOnClickListener(v -> {
            item.cantidad++;
            h.tvCantidad.setText(String.valueOf(item.cantidad));
            onCantidadCambiada.run(); // Avisamos para actualizar el total
        });
    }

    @Override
    public int getItemCount() {
        return lista == null ? 0 : lista.size();
    }

    // ViewHolder con referencias a las vistas de cada fila
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrecio, tvCantidad;
        Button   btnMenos, btnMas;

        ViewHolder(View v) {
            super(v);
            tvNombre   = v.findViewById(R.id.tvNombreProducto);
            tvPrecio   = v.findViewById(R.id.tvPrecioProducto);
            tvCantidad = v.findViewById(R.id.tvCantidad);
            btnMenos   = v.findViewById(R.id.btnMenos);
            btnMas     = v.findViewById(R.id.btnMas);
        }
    }
}
