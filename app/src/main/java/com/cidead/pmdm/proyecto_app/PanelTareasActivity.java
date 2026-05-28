package com.cidead.pmdm.proyecto_app;

// Esta es la pantalla principal del trabajador. Muestra sus tareas, un reloj en tiempo real,
// un botón para fichar entrada/salida y acceso a crear nuevas tareas.

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cidead.pmdm.proyecto_app.adapter.TareaAdapter;
import com.cidead.pmdm.proyecto_app.db.entity.Tarea;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PanelTareasActivity extends AppCompatActivity {

    // Botón para registrar entrada o salida del turno
    private Button btnFichar;
    // TextView que muestra la hora actual
    private TextView tvHoraActual;
    // Texto de saludo con el nombre del trabajador
    private TextView tvSaludo;
    // Contador que muestra cuántas tareas tiene pendientes
    private TextView tvBadgeTareas;
    // Adapter que gestiona la lista de tareas en pantalla
    private TareaAdapter adapter;
    // Gestor de sesión para obtener los datos del usuario conectado
    private SessionManager session;
    // Handler para ejecutar el reloj de forma repetida cada cierto tiempo
    // Pasamos Looper.getMainLooper() para que las actualizaciones de la UI se hagan en el hilo principal
    private final Handler handler = new Handler(Looper.getMainLooper());
    // Variable que controla si el trabajador está fichado o no (entrada/salida)
    private boolean fichado = false;

    // Se ejecuta al crear la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_tareas);

        session = new SessionManager(this);

        // Conectamos los elementos del layout con sus variables
        tvSaludo      = findViewById(R.id.tvSaludo);
        tvHoraActual  = findViewById(R.id.tvHoraActual);
        tvBadgeTareas = findViewById(R.id.tvBadgeTareas);
        btnFichar     = findViewById(R.id.btnFichar);
        Button btnLogout    = findViewById(R.id.btnLogout);
        Button btnNuevaTarea = findViewById(R.id.btnNuevaTarea);
        Button btnEscanear  = findViewById(R.id.btnEscanear);

        // Saludamos al trabajador usando solo su primer nombre (split divide por espacios)
        tvSaludo.setText("Hola, " + session.getNombre().split(" ")[0]);

        // Preparamos la lista de tareas
        RecyclerView rv = findViewById(R.id.rvTareas);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TareaAdapter(new ArrayList<>(), this::cambiarEstado);
        rv.setAdapter(adapter);

        // Ponemos en marcha el reloj que actualiza la hora en pantalla
        actualizarReloj();

        // Al pulsar "Fichar", alternamos entre entrada y salida
        btnFichar.setOnClickListener(v -> {
            if (!fichado) {
                // Si no estaba fichado, registramos entrada
                fichado = true;
                btnFichar.setText("FICHAR SALIDA");
                Toast.makeText(this, "Entrada registrada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                // Si ya estaba fichado, registramos salida
                fichado = false;
                btnFichar.setText("FICHAR ENTRADA");
                Toast.makeText(this, "Salida registrada correctamente", Toast.LENGTH_SHORT).show();
            }
        });

        // Al pulsar "Nueva tarea", abrimos el formulario de registro de tarea
        btnNuevaTarea.setOnClickListener(v ->
                startActivity(new Intent(this, RegistrarTareaActivity.class)));

        // El escáner es una función que estará disponible en el futuro
        btnEscanear.setOnClickListener(v ->
                Toast.makeText(this, "Escáner — próximamente", Toast.LENGTH_SHORT).show());

        // Al pulsar logout, cerramos sesión y volvemos al login
        btnLogout.setOnClickListener(v -> cerrarSesion());
    }

    // Se ejecuta al volver a esta pantalla, por ejemplo tras crear una nueva tarea
    @Override
    protected void onResume() {
        super.onResume();
        cargarTareas();
    }

    // Pide las tareas del trabajador conectado y las muestra en la lista
    private void cargarTareas() {
        // Usamos el id del usuario conectado para obtener solo sus tareas
        ApiClient.getTareasPorUsuario(session.getIdUsuario(), new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    JSONArray array = new JSONArray(respuesta);
                    List<Tarea> lista = new ArrayList<>();
                    int pendientes = 0;
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Tarea t = new Tarea(
                            obj.getString("titulo"),
                            obj.optString("descripcion", ""),
                            obj.getString("fechaAsignacion"),
                            obj.getString("estado"),
                            obj.getInt("idUsuario")
                        );
                        t.idTarea = obj.getInt("idTarea");
                        // Solo mostramos tareas no completadas
                        if (!t.estado.equals("COMPLETADA")) {
                            lista.add(t);
                            pendientes++;
                        }
                    }
                    int finalPendientes = pendientes;
                    runOnUiThread(() -> {
                        adapter.setLista(lista);
                        // Actualizamos el contador de tareas pendientes con el texto correcto
                        // (usamos "Pendiente" o "Pendientes" dependiendo de si es 1 o más)
                        tvBadgeTareas.setText(" " + finalPendientes + " Pendiente" + (finalPendientes == 1 ? "" : "s") + " ");
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(PanelTareasActivity.this, "Error al cargar tareas", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(PanelTareasActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Cambia el estado de una tarea al pulsar sobre ella, siguiendo el ciclo de estados
    private void cambiarEstado(Tarea tarea) {
        // El estado avanza en este orden: PENDIENTE → EN_PROGRESO → COMPLETADA → PENDIENTE
        String nuevoEstado;
        switch (tarea.estado) {
            case "PENDIENTE":   nuevoEstado = "EN_PROGRESO"; break;
            case "EN_PROGRESO": nuevoEstado = "COMPLETADA";  break;
            default:            nuevoEstado = "PENDIENTE";   break;
        }

        // Enviamos el nuevo estado a la API para guardarlo en el servidor
        ApiClient.cambiarEstadoTarea(tarea.idTarea, nuevoEstado, new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                // Si el cambio fue bien, recargamos la lista para ver el estado actualizado
                runOnUiThread(() -> cargarTareas());
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(PanelTareasActivity.this, "Error al cambiar estado", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Pone en marcha el reloj que muestra la hora actual y la actualiza cada 30 segundos
    private void actualizarReloj() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Obtenemos la hora actual en formato HH:mm (ej: 09:35)
                String hora = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                tvHoraActual.setText(hora);
                // Programamos que este mismo código se vuelva a ejecutar en 30000ms (30 segundos)
                handler.postDelayed(this, 30000);
            }
        });
    }

    // Cierra la sesión y lleva al usuario de vuelta a la pantalla de login
    private void cerrarSesion() {
        // Paramos el reloj antes de salir para no dejar procesos corriendo en segundo plano
        handler.removeCallbacksAndMessages(null);
        session.cerrarSesion();
        Intent intent = new Intent(this, LoginActivity.class);
        // Limpiamos la pila de pantallas para que no se pueda volver atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Se llama cuando se destruye la pantalla (por ejemplo al cerrar la app)
    // Paramos el reloj aquí también para evitar fugas de memoria
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
