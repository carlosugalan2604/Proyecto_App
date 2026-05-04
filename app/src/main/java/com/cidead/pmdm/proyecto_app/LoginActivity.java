package com.cidead.pmdm.proyecto_app;

// Esta es la pantalla de inicio de sesión. Es lo primero que ve el usuario al abrir la app.
// Aquí se introduce el email y la contraseña para entrar.

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    // Campos de texto donde el usuario escribe su email y contraseña
    private EditText etEmail, etPassword;
    // Botón para pulsar y entrar
    private Button btnLogin;
    // Rueda de carga que se muestra mientras esperamos respuesta del servidor
    private ProgressBar progressBar;
    // Objeto que nos ayuda a guardar y leer los datos de la sesión activa
    private SessionManager session;

    // onCreate se ejecuta cuando se crea la pantalla por primera vez
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creamos el gestor de sesión para saber si ya hay alguien conectado
        session = new SessionManager(this);

        // Si el usuario ya inició sesión antes, no le hacemos pasar por el login otra vez
        // Le mandamos directamente a su pantalla según su rol
        if (session.haySesion()) {
            redirigirSegunRol(session.getRol());
            return; // Salimos del onCreate para no seguir configurando la pantalla de login
        }

        // Conectamos cada variable con su elemento visual del layout
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Cuando el usuario pulsa "Entrar", llamamos al método que gestiona el login
        btnLogin.setOnClickListener(v -> realizarLogin());
    }

    // Este método recoge los datos del formulario y llama a la API para verificarlos
    private void realizarLogin() {
        // .trim() elimina los espacios de delante y detrás por si el usuario los puso sin querer
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Si algún campo está vacío, avisamos y no hacemos la petición
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostramos la rueda de carga y desactivamos el botón para que no se pulse dos veces
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Llamamos a la API para que compruebe si el email y contraseña son correctos
        ApiClient.login(email, password, new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                // runOnUiThread es necesario porque la respuesta llega en un hilo secundario
                // y solo el hilo principal puede tocar la pantalla
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    try {
                        // Convertimos el texto JSON que llegó del servidor en un objeto para leer sus campos
                        JSONObject usuario = new JSONObject(respuesta);
                        // Extraemos cada dato del JSON que nos devolvió el servidor
                        int id     = usuario.getInt("idUsuario");
                        String nombre = usuario.getString("nombre");
                        String emailUsuario = usuario.getString("email");
                        String rol = usuario.getString("rol");

                        // Guardamos la sesión en el dispositivo para no tener que volver a hacer login
                        session.guardarSesion(id, nombre, emailUsuario, rol);
                        // Llevamos al usuario a la pantalla que le corresponde según su rol
                        redirigirSegunRol(rol);
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Error al leer respuesta", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                // Si el login falló (credenciales incorrectas o error de red), avisamos al usuario
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Según el rol del usuario, lo llevamos a una pantalla distinta
    private void redirigirSegunRol(String rol) {
        Intent intent;
        // Comprobamos el rol y preparamos la pantalla de destino
        switch (rol) {
            case "GERENTE":
                // El gerente va al panel principal con las estadísticas y el menú lateral
                intent = new Intent(this, DashboardGerenteActivity.class);
                break;
            case "TRABAJADOR":
                // El trabajador va a su panel de tareas para ver lo que tiene que hacer
                intent = new Intent(this, PanelTareasActivity.class);
                break;
            case "CLIENTE":
                // El cliente va a ver el historial de sus pedidos
                intent = new Intent(this, HistorialPedidosActivity.class);
                break;
            default:
                // Si el rol no existe o no es ninguno de los anteriores, avisamos
                Toast.makeText(this, "Rol no reconocido", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        // finish() cierra esta pantalla para que el usuario no pueda volver al login pulsando atrás
        finish();
    }
}
