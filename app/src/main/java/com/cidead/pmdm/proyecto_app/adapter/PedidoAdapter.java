package com.cidead.pmdm.proyecto_app.adapter;

// Adapter que conecta la lista de pedidos del cliente con el RecyclerView.
// Cada fila muestra el número de pedido, la fecha, el importe y un badge de estado.
// Al pulsar una fila se abre la pantalla de detalle del pedido (DetallePedidoActivity).

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cidead.pmdm.proyecto_app.DetallePedidoActivity;
import com.cidead.pmdm.proyecto_app.R;
import com.cidead.pmdm.proyecto_app.db.entity.Pedido;

import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.ViewHolder> {

    // Lista de pedidos que se muestra en pantalla
    private List<Pedido> lista;
    // Contexto necesario para poder lanzar el Intent de DetallePedidoActivity
    private final Context context;

    // Constructor: recibe el contexto y la lista inicial de pedidos
    public PedidoAdapter(Context context, List<Pedido> lista) {
        this.context = context;
        this.lista   = lista;
    }

    // Reemplaza toda la lista y notifica al RecyclerView que debe redibujarse
    public void setLista(List<Pedido> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    // Infla el layout XML de una fila de pedido y crea el ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new ViewHolder(v);
    }

    // Rellena los datos de la fila en la posición indicada
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Pedido p = lista.get(position);

        // Rellenamos los textos de la cabecera de la fila
        h.tvNumero.setText("Pedido #" + p.idPedido);
        h.tvFecha.setText("Fecha: " + p.fechaPedido);
        h.tvImporte.setText(String.format("Importe: € %.2f", p.importeTotal));

        // Cambiamos el color y texto del badge según el estado del pedido
        switch (p.estado) {
            case "ENTREGADO":
                h.tvEstado.setText("Entregado");
                h.tvEstado.setBackgroundColor(Color.parseColor("#4CAF50")); // verde
                break;
            case "EN_PROCESO":
                h.tvEstado.setText("En proceso");
                h.tvEstado.setBackgroundColor(Color.parseColor("#FF9800")); // naranja
                break;
            case "CANCELADO":
                h.tvEstado.setText("Cancelado");
                h.tvEstado.setBackgroundColor(Color.parseColor("#F44336")); // rojo
                break;
        }

        // Al pulsar la fila entera, abrimos DetallePedidoActivity pasándole los datos del pedido
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetallePedidoActivity.class);
            // Pasamos todos los datos necesarios para que el detalle no tenga que volver a pedir a la API
            intent.putExtra("id_pedido", p.idPedido);
            intent.putExtra("estado",    p.estado);
            intent.putExtra("fecha",     p.fechaPedido);
            intent.putExtra("importe",   p.importeTotal);
            context.startActivity(intent);
        });
    }

    // Devuelve cuántas filas hay que mostrar
    @Override
    public int getItemCount() {
        return lista == null ? 0 : lista.size();
    }

    // ViewHolder: guarda las referencias a los elementos visuales de cada fila
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero, tvFecha, tvImporte, tvEstado;

        ViewHolder(View v) {
            super(v);
            tvNumero  = v.findViewById(R.id.tvNumeroPedido);
            tvFecha   = v.findViewById(R.id.tvFechaPedido);
            tvImporte = v.findViewById(R.id.tvImportePedido);
            tvEstado  = v.findViewById(R.id.tvEstadoPedido);
        }
    }
}
