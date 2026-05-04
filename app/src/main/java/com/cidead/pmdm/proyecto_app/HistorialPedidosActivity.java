package com.cidead.pmdm.proyecto_app;

// Esta pantalla es la vista principal del cliente. Muestra todos sus pedidos
// con su estado, fecha e importe. Al pulsar un pedido se pueden ver los productos que contiene.

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cidead.pmdm.proyecto_app.adapter.PedidoAdapter;
import com.cidead.pmdm.proyecto_app.db.entity.Pedido;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistorialPedidosActivity extends AppCompatActivity {

    // El adapter que gestiona cómo se muestra cada pedido en la lista
    private PedidoAdapter adapter;
    // Para obtener el id del cliente conectado y pedir solo sus pedidos
    private SessionManager session;

    // Se ejecuta al crear la pantalla
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_pedidos);

        session = new SessionManager(this);

        // Mostramos solo el primer nombre del cliente en el saludo
        TextView tvSaludo = findViewById(R.id.tvSaludo);
        tvSaludo.setText("Hola, " + session.getNombre().split(" ")[0]);

        // Al pulsar el botón de logout, cerramos la sesión
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> cerrarSesion());

        // Configuramos la lista de pedidos
        RecyclerView rv = findViewById(R.id.rvPedidos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        // El adapter necesita el contexto (this) para poder abrir la pantalla de detalle al pulsar
        adapter = new PedidoAdapter(this, new ArrayList<>());
        rv.setAdapter(adapter);
    }

    // Se ejecuta al volver a esta pantalla, actualizamos la lista por si cambió algo
    @Override
    protected void onResume() {
        super.onResume();
        cargarPedidos();
    }

    // Pide los pedidos del cliente conectado a la API y los muestra en la lista
    private void cargarPedidos() {
        // Usamos el id del usuario conectado para filtrar solo sus pedidos
        ApiClient.getPedidosPorUsuario(session.getIdUsuario(), new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    // Convertimos el JSON a un array para recorrerlo
                    JSONArray array = new JSONArray(respuesta);
                    List<Pedido> lista = new ArrayList<>();
                    // Creamos un objeto Pedido por cada elemento del JSON
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Pedido p = new Pedido(
                            obj.getString("fechaPedido"),
                            obj.getString("estado"),
                            obj.getDouble("importeTotal"),
                            obj.getInt("idUsuario")
                        );
                        // Guardamos el id para poder abrir el detalle al pulsar el pedido
                        p.idPedido = obj.getInt("idPedido");
                        lista.add(p);
                    }
                    // Le pasamos la lista al adapter para que la muestre en pantalla
                    runOnUiThread(() -> adapter.setLista(lista));
                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(HistorialPedidosActivity.this, "Error al cargar pedidos", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(HistorialPedidosActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Borra los datos de sesión y lleva al usuario a la pantalla de login
    private void cerrarSesion() {
        session.cerrarSesion();
        Intent intent = new Intent(this, LoginActivity.class);
        // Limpiamos el historial de navegación para que no se pueda volver atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
