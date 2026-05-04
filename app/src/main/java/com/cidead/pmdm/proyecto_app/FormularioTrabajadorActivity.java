package com.cidead.pmdm.proyecto_app;

// Esta pantalla contiene el formulario para añadir un trabajador nuevo a la empresa.
// El gerente introduce el nombre, email y contraseña y el sistema crea la cuenta automáticamente.

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class FormularioTrabajadorActivity extends AppCompatActivity {

    // Campos del formulario para los datos del nuevo trabajador
    private EditText etNombre, etEmail, etPassword;

    // Se ejecuta al crear la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_trabajador);

        // Barra superior con botón de volver
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Conectamos los campos del formulario con sus elementos del layout
        etNombre   = findViewById(R.id.etNombre);
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        // Al pulsar el botón guardar, intentamos crear el trabajador
        btnGuardar.setOnClickListener(v -> guardarTrabajador());
    }

    // Recoge los datos del formulario, los valida y los envía a la API para crear el trabajador
    private void guardarTrabajador() {
        // Recogemos los valores de los campos y eliminamos espacios sobrantes
        String nombre   = etNombre.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Todos los campos son obligatorios, si alguno está vacío no podemos continuar
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Llamamos a la API para crear el nuevo trabajador con los datos introducidos
        // El rol se asigna automáticamente como "TRABAJADOR" dentro de ApiClient
        ApiClient.crearTrabajador(nombre, email, password, new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                runOnUiThread(() -> {
                    Toast.makeText(FormularioTrabajadorActivity.this, "Trabajador añadido correctamente", Toast.LENGTH_SHORT).show();
                    // Cerramos el formulario y volvemos a la lista de trabajadores
                    finish();
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(FormularioTrabajadorActivity.this, "Error al añadir trabajador", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Cierra el formulario al pulsar la flecha de volver en la barra superior
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
