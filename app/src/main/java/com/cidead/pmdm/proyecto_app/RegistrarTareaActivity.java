package com.cidead.pmdm.proyecto_app;

// Esta pantalla permite crear una tarea nueva.
// El usuario escribe el título y la descripción, y la tarea se asigna automáticamente
// al trabajador que esté conectado en ese momento.

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistrarTareaActivity extends AppCompatActivity {

    // Campos donde el usuario escribe el título y la descripción de la tarea
    private EditText etTitulo, etDescripcion;
    // Para leer el id del usuario conectado y asignarle la tarea
    private SessionManager session;

    // Se ejecuta al crear la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_tarea);

        // Barra superior con botón de volver
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Iniciamos el gestor de sesión para saber quién está conectado
        session       = new SessionManager(this);
        etTitulo      = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        // Al pulsar el botón, intentamos guardar la tarea
        btnRegistrar.setOnClickListener(v -> registrarTarea());
    }

    // Recoge los datos del formulario y los envía a la API para crear la tarea
    private void registrarTarea() {
        String titulo      = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        // El título es obligatorio; sin él no tiene sentido crear la tarea
        if (titulo.isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generamos la fecha de hoy automáticamente en formato dd/MM/yyyy
        // así el usuario no tiene que escribirla manualmente
        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // Enviamos la tarea a la API asignándola al usuario que está conectado ahora mismo
        ApiClient.crearTarea(titulo, descripcion, fecha, session.getIdUsuario(), new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                runOnUiThread(() -> {
                    Toast.makeText(RegistrarTareaActivity.this, "Tarea registrada correctamente", Toast.LENGTH_SHORT).show();
                    // Cerramos el formulario y volvemos a la lista de tareas
                    finish();
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(RegistrarTareaActivity.this, "Error al registrar tarea", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Cierra el formulario cuando se pulsa la flecha de volver en la barra superior
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
