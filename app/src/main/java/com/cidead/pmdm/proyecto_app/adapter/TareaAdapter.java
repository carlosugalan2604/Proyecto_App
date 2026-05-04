package com.cidead.pmdm.proyecto_app.adapter;

// Adapter que conecta la lista de tareas con el RecyclerView que las muestra en pantalla.
// Cada fila tiene el título, descripción, fecha y un badge de estado que se puede pulsar
// para avanzar la tarea al siguiente estado del ciclo.

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cidead.pmdm.proyecto_app.R;
import com.cidead.pmdm.proyecto_app.db.entity.Tarea;

import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.ViewHolder> {

    /*
     * Interfaz de callback que permite a la Activity reaccionar cuando el usuario
     * pulsa el badge de estado de una tarea para cambiarla.
     * La Activity implementa este método y decide qué estado asignar a continuación.
     */
    public interface OnTareaClickListener {
        void onCambiarEstado(Tarea tarea);
    }

    // Lista de tareas que se muestra en pantalla
    private List<Tarea> lista;
    // Objeto que se llama cuando el usuario pulsa el badge de estado
    private final OnTareaClickListener listener;

    // Constructor: recibe la lista inicial y el listener de cambio de estado
    public TareaAdapter(List<Tarea> lista, OnTareaClickListener listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    // Reemplaza toda la lista y notifica al RecyclerView que debe redibujarse
    public void setLista(List<Tarea> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    // Infla el layout XML de una fila y crea el ViewHolder que la gestiona
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);
        return new ViewHolder(v);
    }

    // Rellena los datos de la fila en la posición indicada
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Tarea t = lista.get(position);

        // Colocamos los textos básicos en la fila
        h.tvTitulo.setText(t.titulo);
        h.tvDescripcion.setText(t.descripcion);
        h.tvFecha.setText(t.fechaAsignacion);

        // Cambiamos el texto y el color del badge según el estado actual de la tarea
        switch (t.estado) {
            case "PENDIENTE":
                h.tvEstado.setText("Pendiente");
                h.tvEstado.setBackgroundColor(Color.parseColor("#FF9800")); // naranja
                break;
            case "EN_PROGRESO":
                h.tvEstado.setText("En progreso");
                h.tvEstado.setBackgroundColor(Color.parseColor("#2196F3")); // azul
                break;
            case "COMPLETADA":
                h.tvEstado.setText("Completada");
                h.tvEstado.setBackgroundColor(Color.parseColor("#4CAF50")); // verde
                break;
        }

        // Al pulsar el badge, avisamos a la Activity para que cambie el estado
        h.tvEstado.setOnClickListener(v -> listener.onCambiarEstado(t));
    }

    // Devuelve cuántas filas hay que mostrar
    @Override
    public int getItemCount() {
        return lista == null ? 0 : lista.size();
    }

    // ViewHolder: guarda las referencias a los elementos visuales de cada fila
    // para no tener que buscarlos con findViewById en cada actualización
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvFecha, tvEstado;

        ViewHolder(View v) {
            super(v);
            tvTitulo      = v.findViewById(R.id.tvTituloTarea);
            tvDescripcion = v.findViewById(R.id.tvDescripcionTarea);
            tvFecha       = v.findViewById(R.id.tvFechaTarea);
            tvEstado      = v.findViewById(R.id.tvEstadoTarea);
        }
    }
}
