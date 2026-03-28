package com.cidead.pmdm.proyecto_app;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;



public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarLogin();
            }
        });
    }

    private void realizarLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        validarCredencialesAPI(email, password);
    }

    private void validarCredencialesAPI(String email, String password) {


        boolean loginExitoso = true;
        String rolObtenido = "GERENTE";

        if (loginExitoso) {
            redirigirSegunRol(rolObtenido);
        } else {
            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirigirSegunRol(String rol) {
        Intent intent;

        switch (rol) {
            case "GERENTE":
                intent = new Intent(LoginActivity.this, DashboardGerenteActivity.class);
                break;
            case "TRABAJADOR":
                intent = new Intent(LoginActivity.this, PanelTareasActivity.class);
                break;
            case "CLIENTE":
                intent = new Intent(LoginActivity.this, HistorialPedidosActivity.class);
                break;
            default:
                Toast.makeText(this, "Rol no reconocido", Toast.LENGTH_SHORT).show();
                return;
        }

        startActivity(intent);
        finish(); // Destruimos el LoginActivity para que el usuario no pueda volver atrás con el botón de retroceso
    }
}