package com.cidead.pmdm.proyecto_app;

// Esta pantalla muestra la lista de todos los trabajadores de la empresa.
// El gerente puede ver sus nombres y emails, y también añadir nuevos trabajadores.
// Incluye su propio adapter interno porque la lista es sencilla y no necesita uno separado.

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrabajadoresActivity extends AppCompatActivity {

    // El adapter que muestra cada trabajador como una fila en la lista
    private TrabajadorAdapter adapter;

    // Se ejecuta al crear la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trabajadores);

        // Barra superior con botón de volver
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configuramos la lista de trabajadores
        RecyclerView rv = findViewById(R.id.rvTrabajadores);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Creamos el adapter con la lista vacía; se llenará cuando llegue la respuesta de la API
        adapter = new TrabajadorAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        // El botón "+" abre el formulario para añadir un trabajador nuevo
        FloatingActionButton fab = findViewById(R.id.fabNuevoTrabajador);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, FormularioTrabajadorActivity.class)));
    }

    // Al volver a esta pantalla, recargamos la lista por si se añadió alguien nuevo
    @Override
    protected void onResume() {
        super.onResume();
        cargarTrabajadores();
    }

    // Pide la lista de trabajadores a la API y actualiza el adapter
    private void cargarTrabajadores() {
        ApiClient.getTrabajadores(new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    // Convertimos el JSON en un array
                    JSONArray array = new JSONArray(respuesta);
                    List<String[]> lista = new ArrayList<>();
                    // Guardamos solo el nombre y email de cada trabajador en un array de dos posiciones
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        // Cada elemento de la lista es un String[] con [nombre, email]
                        lista.add(new String[]{
                            obj.getString("nombre"),
                            obj.getString("email")
                        });
                    }
                    // Actualizamos la lista en pantalla desde el hilo principal
                    runOnUiThread(() -> adapter.setLista(lista));
                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(TrabajadoresActivity.this, "Error al cargar trabajadores", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(TrabajadoresActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Cierra esta pantalla al pulsar la flecha de volver en la barra superior
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Adapter interno para mostrar la lista de trabajadores.
    // Es "static" porque no necesita acceder a los datos de la actividad principal.
    // Cada item de la lista es un String[] con dos posiciones: [0]=nombre, [1]=email
    static class TrabajadorAdapter extends RecyclerView.Adapter<TrabajadorAdapter.VH> {
        // La lista de trabajadores que se muestra en pantalla
        private List<String[]> lista;

        // Constructor: recibe la lista inicial (puede estar vacía)
        TrabajadorAdapter(List<String[]> lista) { this.lista = lista; }

        // Reemplaza la lista completa y notifica al RecyclerView para que se redibuje
        void setLista(List<String[]> l) { this.lista = l; notifyDataSetChanged(); }

        // Crea la vista de cada fila inflando el layout XML del item
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_trabajador, parent, false);
            return new VH(v);
        }

        // Rellena los datos de una fila concreta (la que está en la posición "pos")
        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            String[] item = lista.get(pos);
            h.tvNombre.setText(item[0]); // nombre completo
            h.tvEmail.setText(item[1]);  // email
            // Mostramos la primera letra del nombre como avatar/inicial (en mayúscula)
            h.tvIniciales.setText(item[0].length() >= 1
                    ? String.valueOf(item[0].charAt(0)).toUpperCase() : "?");
        }

        // Devuelve cuántos elementos hay en la lista (para que el RecyclerView sepa cuántas filas crear)
        @Override
        public int getItemCount() { return lista == null ? 0 : lista.size(); }

        // ViewHolder: guarda las referencias a los elementos visuales de cada fila
        // para no tener que buscarlos con findViewById cada vez
        static class VH extends RecyclerView.ViewHolder {
            TextView tvIniciales, tvNombre, tvEmail;
            VH(View v) {
                super(v);
                tvIniciales = v.findViewById(R.id.tvIniciales);
                tvNombre    = v.findViewById(R.id.tvNombreTrabajador);
                tvEmail     = v.findViewById(R.id.tvEmailTrabajador);
            }
        }
    }
}
