package com.cidead.pmdm.proyecto_app;

// Esta pantalla es para el gerente y muestra todas las tareas de todos los trabajadores.
// Permite ver el estado de cada tarea y cambiarla pulsando sobre ella.
// También tiene un botón para crear nuevas tareas.

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cidead.pmdm.proyecto_app.adapter.TareaAdapter;
import com.cidead.pmdm.proyecto_app.db.entity.Tarea;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TareasEquipoActivity extends AppCompatActivity {

    // El adapter se encarga de mostrar cada tarea como una fila en la lista
    private TareaAdapter adapter;

    // Se ejecuta al crear la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tareas_equipo);

        // Barra superior con botón de volver
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configuramos la lista de tareas
        RecyclerView rv = findViewById(R.id.rvTareas);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Creamos el adapter con lista vacía y le pasamos el método que cambia el estado de una tarea
        // "this::cambiarEstadoTarea" es una forma corta de pasar el método como parámetro
        adapter = new TareaAdapter(new ArrayList<>(), this::cambiarEstadoTarea);
        rv.setAdapter(adapter);

        // El botón flotante "+" abre la pantalla para registrar una tarea nueva
        FloatingActionButton fab = findViewById(R.id.fabNuevaTarea);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, RegistrarTareaActivity.class)));
    }

    // Se ejecuta al volver a esta pantalla, por ejemplo después de crear una tarea nueva
    @Override
    protected void onResume() {
        super.onResume();
        cargarTareas();
    }

    // Pide todas las tareas del sistema a la API y las muestra en la lista
    private void cargarTareas() {
        ApiClient.getTareas(new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    // Convertimos el JSON a un array para recorrerlo
                    JSONArray array = new JSONArray(respuesta);
                    List<Tarea> lista = new ArrayList<>();
                    // Creamos un objeto Tarea por cada elemento del JSON
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Tarea t = new Tarea(
                            obj.getString("titulo"),
                            obj.optString("descripcion", ""),
                            obj.getString("fechaAsignacion"),
                            obj.getString("estado"),
                            obj.getInt("idUsuario")
                        );
                        // Guardamos el id para poder cambiar el estado después
                        t.idTarea = obj.getInt("idTarea");
                        lista.add(t);
                    }
                    // Enviamos la lista al adapter para que refresque la pantalla
                    runOnUiThread(() -> adapter.setLista(lista));
                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(TareasEquipoActivity.this, "Error al cargar tareas", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(TareasEquipoActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Cambia el estado de una tarea al siguiente del ciclo cuando el usuario la pulsa
    private void cambiarEstadoTarea(Tarea tarea) {
        // El estado sigue este ciclo: PENDIENTE → EN_PROGRESO → COMPLETADA → PENDIENTE (vuelve a empezar)
        String nuevoEstado;
        switch (tarea.estado) {
            case "PENDIENTE":   nuevoEstado = "EN_PROGRESO"; break;
            case "EN_PROGRESO": nuevoEstado = "COMPLETADA";  break;
            default:            nuevoEstado = "PENDIENTE";   break; // Si está COMPLETADA vuelve a PENDIENTE
        }

        // Enviamos el cambio de estado a la API
        ApiClient.cambiarEstadoTarea(tarea.idTarea, nuevoEstado, new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                // Si se actualizó bien, recargamos la lista para que se vea el nuevo estado
                runOnUiThread(() -> cargarTareas());
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(TareasEquipoActivity.this, "Error al cambiar estado", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Cierra esta pantalla cuando se pulsa la flecha de volver en la barra superior
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
