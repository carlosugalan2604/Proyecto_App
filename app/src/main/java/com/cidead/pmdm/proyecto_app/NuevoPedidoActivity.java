package com.cidead.pmdm.proyecto_app;

// Pantalla para que el cliente cree un nuevo pedido.
// Muestra la lista completa de productos con controles de cantidad (+ / -).
// Al confirmar, calcula el total y envía el pedido a la API.

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cidead.pmdm.proyecto_app.adapter.ProductoPedidoAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NuevoPedidoActivity extends AppCompatActivity {

    // Adapter que gestiona la lista de productos con sus cantidades
    private ProductoPedidoAdapter adapter;
    // Sesión del cliente para saber su id al crear el pedido
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_pedido);

        // Barra superior con flecha para volver al historial
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(this);

        // Configuramos la lista de productos
        RecyclerView rv = findViewById(R.id.rvProductosNuevoPedido);
        rv.setLayoutManager(new LinearLayoutManager(this));
        // El adapter recibe un listener que se ejecuta cuando cambia cualquier cantidad
        adapter = new ProductoPedidoAdapter(new ArrayList<>(), this::actualizarTotal);
        rv.setAdapter(adapter);

        // Al pulsar "Confirmar pedido" enviamos el pedido a la API
        Button btnConfirmar = findViewById(R.id.btnConfirmarPedido);
        btnConfirmar.setOnClickListener(v -> confirmarPedido());

        // Cargamos los productos desde la API
        cargarProductos();
    }

    // Pide todos los productos al servidor y los pasa al adapter
    private void cargarProductos() {
        ApiClient.getProductos(new ApiClient.ApiCallback() {
            @Override
            public void onRespuesta(String respuesta) {
                try {
                    JSONArray array = new JSONArray(respuesta);
                    List<ProductoPedidoAdapter.ItemProducto> lista = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject p = array.getJSONObject(i);
                        lista.add(new ProductoPedidoAdapter.ItemProducto(
                            p.getInt("idProducto"),
                            p.getString("nombre"),
                            p.getDouble("precio")
                        ));
                    }
                    // Actualizamos la lista en el hilo principal (UI)
                    runOnUiThread(() -> adapter.setLista(lista));
                } catch (Exception e) {
                    runOnUiThread(() ->
                        Toast.makeText(NuevoPedidoActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(NuevoPedidoActivity.this, "Sin conexión con la API", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Recalcula el total del pedido sumando precio × cantidad de cada producto
    // y lo muestra en el TextView del pie de pantalla
    private void actualizarTotal() {
        double total = 0;
        for (ProductoPedidoAdapter.ItemProducto item : adapter.getLista()) {
            total += item.precio * item.cantidad;
        }
        TextView tvTotal = findViewById(R.id.tvTotalNuevoPedido);
        tvTotal.setText(String.format("€ %.2f", total));
    }

    // Recoge las líneas con cantidad > 0, las monta como JSON y las envía a la API
    private void confirmarPedido() {
        try {
            JSONArray lineas = new JSONArray();
            for (ProductoPedidoAdapter.ItemProducto item : adapter.getLista()) {
                if (item.cantidad > 0) {
                    JSONObject linea = new JSONObject();
                    linea.put("idProducto", item.idProducto);
                    linea.put("cantidad",   item.cantidad);
                    lineas.put(linea);
                }
            }

            if (lineas.length() == 0) {
                Toast.makeText(this, "Añade al menos un producto al pedido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enviamos el pedido a la API y volvemos al historial si tiene éxito
            ApiClient.crearPedido(session.getIdUsuario(), lineas.toString(), new ApiClient.ApiCallback() {
                @Override
                public void onRespuesta(String respuesta) {
                    runOnUiThread(() -> {
                        Toast.makeText(NuevoPedidoActivity.this, "Pedido realizado correctamente", Toast.LENGTH_SHORT).show();
                        finish(); // Volvemos a HistorialPedidosActivity, que recargará la lista
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() ->
                        Toast.makeText(NuevoPedidoActivity.this, "Error al enviar el pedido", Toast.LENGTH_SHORT).show());
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error al preparar el pedido", Toast.LENGTH_SHORT).show();
        }
    }

    // Cierra esta pantalla al pulsar la flecha de volver
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
